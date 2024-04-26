package org.gradle.api.experimental.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport.linkMainSourceSourceSetDependencies
import org.gradle.api.experimental.kmp.internal.KotlinPluginSupport.linkJavaVersion
import org.gradle.api.internal.plugins.software.SoftwareType
import org.gradle.kotlin.dsl.create
import org.scijava.ValidationLibrary

/**
 * Creates a declarative [KotlinJvmApplication] DSL model, applies the official Kotlin and application plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneKotlinJvmLibraryPlugin : Plugin<Project>, ValidationLibrary {
//    @get:SoftwareType(name = "kotlinJvmLibrary", modelPublicType = KotlinJvmLibrary::class)
//    abstract val library: KotlinJvmLibrary

    override val extensionName = "kotlinJvmLibrary"

    override fun apply(project: Project) {
        val library = project.extensions.create<KotlinJvmLibrary>(extensionName)
        val dslModel = library

        project.plugins.apply("org.jetbrains.kotlin.jvm")

        linkDslModelToPlugin(project, dslModel)

        project.validate()
    }

    private fun linkDslModelToPlugin(project: Project, dslModel: KotlinJvmLibrary) {
        linkJavaVersion(project, dslModel)
        linkMainSourceSourceSetDependencies(project, dslModel.dependencies)
    }
}
