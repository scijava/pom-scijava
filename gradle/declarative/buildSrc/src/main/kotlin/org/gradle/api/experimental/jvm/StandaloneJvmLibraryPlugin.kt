package org.gradle.api.experimental.jvm

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport.createTargetSourceSet
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport.setupCommonSourceSet
import org.gradle.api.internal.plugins.software.SoftwareType
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import javax.inject.Inject

/**
 * Creates a declarative [JvmLibrary] DSL model, applies the official Jvm plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneJvmLibraryPlugin : Plugin<Project> {
    @get:SoftwareType(name = "jvmLibrary", modelPublicType = JvmLibrary::class)
    abstract val jvmLibrary: JvmLibrary

    override fun apply(project: Project) {
        val dslModel = jvmLibrary

        project.plugins.apply(JavaLibraryPlugin::class.java)

        linkDslModelToPlugin(project, dslModel)
    }

    @get:Inject
    protected abstract val javaToolchainService: JavaToolchainService?

    private fun linkDslModelToPlugin(project: Project, dslModel: JvmLibrary) {
        val commonSources = setupCommonSourceSet(project)
        JvmPluginSupport.linkSourceSetToDependencies(project, commonSources, dslModel.dependencies)

        JvmPluginSupport.linkJavaVersion(project, dslModel)

        dslModel.targets.withType<JavaTarget>().all {
            val sourceSet = createTargetSourceSet(project, this, commonSources, javaToolchainService!!)
            // Link dependencies to DSL
            JvmPluginSupport.linkSourceSetToDependencies(project, sourceSet, dependencies)

            // Extend common dependencies
            project.configurations[sourceSet.apiConfigurationName]
                    .extendsFrom(project.configurations[commonSources.apiConfigurationName])
        }
    }
}
