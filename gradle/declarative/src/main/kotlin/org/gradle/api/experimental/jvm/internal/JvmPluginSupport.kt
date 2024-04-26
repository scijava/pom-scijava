package org.gradle.api.experimental.jvm.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.experimental.common.ApplicationDependencies
import org.gradle.api.experimental.common.LibraryDependencies
import org.gradle.api.experimental.jvm.HasJavaTarget
import org.gradle.api.experimental.jvm.HasJavaTargets
import org.gradle.api.experimental.jvm.HasJvmApplication
import org.gradle.api.experimental.jvm.JavaTarget
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.internal.JavaPluginHelper
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*

object JvmPluginSupport {
    fun linkSourceSetToDependencies(project: Project, sourceSet: SourceSet, dependencies: LibraryDependencies) {
        project.configurations[sourceSet.implementationConfigurationName]
            .dependencies.addAllLater(dependencies.implementation.dependencies)
        project.configurations[sourceSet.compileOnlyConfigurationName]
            .dependencies.addAllLater(dependencies.compileOnly.dependencies)
        project.configurations[sourceSet.runtimeOnlyConfigurationName]
            .dependencies.addAllLater(dependencies.runtimeOnly.dependencies)
        project.configurations[sourceSet.apiConfigurationName]
            .dependencies.addAllLater(dependencies.api.dependencies)
    }

    fun linkSourceSetToDependencies(project: Project, sourceSet: SourceSet, dependencies: ApplicationDependencies) {
        project.configurations[sourceSet.implementationConfigurationName]
            .dependencies.addAllLater(dependencies.implementation.dependencies)
        project.configurations[sourceSet.compileOnlyConfigurationName]
            .dependencies.addAllLater(dependencies.compileOnly.dependencies)
        project.configurations[sourceSet.runtimeOnlyConfigurationName]
            .dependencies.addAllLater(dependencies.runtimeOnly.dependencies)
    }

    fun linkMainSourceSourceSetDependencies(project: Project, dependencies: LibraryDependencies) {
        val java = project.extensions.getByType<JavaPluginExtension>()
        linkSourceSetToDependencies(project, java.sourceSets["main"], dependencies)
    }

    fun linkMainSourceSourceSetDependencies(project: Project, dependencies: ApplicationDependencies) {
        val java = project.extensions.getByType<JavaPluginExtension>()
        linkSourceSetToDependencies(project, java.sourceSets["main"], dependencies)
    }

    fun linkJavaVersion(project: Project, dslModel: HasJavaTarget) {
        val java = project.extensions.getByType<JavaPluginExtension>()
        java.toolchain.languageVersion = dslModel.javaVersion.map(JavaLanguageVersion::of)
    }

    fun linkJavaVersion(project: Project, dslModel: HasJavaTargets) {
        val java = project.extensions.getByType<JavaPluginExtension>()
        java.toolchain.languageVersion = project.provider { JavaLanguageVersion.of(dslModel.targets.withType<JavaTarget>().minOf(JavaTarget::javaVersion)) }
    }

    fun linkApplicationMainClass(project: Project, application: HasJvmApplication) {
        val app = project.extensions.getByType<JavaApplication>()
        app.mainClass = application.mainClass
    }

    fun setupCommonSourceSet(project: Project): SourceSet {
        val commonSources = JavaPluginHelper.getJavaComponent(project).mainFeature.sourceSet
        val srcDir = project.layout.projectDirectory.dir("src").dir("common").dir("java")
        commonSources.java.setSrcDirs(setOf(srcDir))
        return commonSources
    }

    fun createTargetSourceSet(project: Project,
                              target: JavaTarget,
                              commonSources: SourceSet,
                              javaToolchainService: JavaToolchainService): SourceSet {
        val java = project.extensions.getByType<JavaPluginExtension>()
        val sourceSet = java.sourceSets.create("java${target.javaVersion}")
        java.registerFeature("java" + target.javaVersion) { usingSourceSet(sourceSet) }

        // Link properties
        project.tasks.named<JavaCompile>(sourceSet.compileJavaTaskName) {
            javaCompiler = javaToolchainService.compilerFor { languageVersion = JavaLanguageVersion.of(target.javaVersion) }
        }

        // Depend on common sources
        project.configurations[sourceSet.implementationConfigurationName]
            .dependencies += project.dependencies.create(commonSources.output)

        // Extend common dependencies
        project.configurations[sourceSet.implementationConfigurationName]
            .extendsFrom(project.configurations[commonSources.implementationConfigurationName])
        project.configurations[sourceSet.compileOnlyConfigurationName]
            .extendsFrom(project.configurations[commonSources.compileOnlyConfigurationName])
        project.configurations[sourceSet.runtimeOnlyConfigurationName]
            .extendsFrom(project.configurations[commonSources.runtimeOnlyConfigurationName])

        // Assemble includes all targets
        project.tasks["assemble"].configure<Task> { dependsOn(sourceSet.output) }

        return sourceSet
    }
}
