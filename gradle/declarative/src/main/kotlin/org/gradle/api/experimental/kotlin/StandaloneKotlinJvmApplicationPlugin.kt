package org.gradle.api.experimental.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport
import org.gradle.api.experimental.kmp.internal.KotlinPluginSupport
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.kotlin.dsl.create
import org.scijava.ValidationApplication

/**
 * Creates a declarative [KotlinJvmApplication] DSL model, applies the official Kotlin and application plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneKotlinJvmApplicationPlugin : Plugin<Project>, ValidationApplication {
//    @get:SoftwareType(name = "kotlinJvmApplication", modelPublicType = KotlinJvmApplication::class)
//    abstract val application: KotlinJvmApplication

    override val extensionName = "kotlinJvmApplication"

    override fun apply(project: Project) {
        val application = project.extensions.create<KotlinJvmApplication>(extensionName)
        val dslModel = application

        project.plugins.apply(ApplicationPlugin::class.java)
        project.plugins.apply("org.jetbrains.kotlin.jvm")

        linkDslModelToPlugin(project, dslModel)

        project.validate()
    }

    private fun linkDslModelToPlugin(project: Project, dslModel: KotlinJvmApplication) {
        KotlinPluginSupport.linkJavaVersion(project, dslModel)
        JvmPluginSupport.linkApplicationMainClass(project, dslModel)
        JvmPluginSupport.linkMainSourceSourceSetDependencies(project, dslModel.dependencies)
    }
}
