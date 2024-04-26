package org.gradle.api.experimental.java

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport
import org.gradle.api.internal.plugins.software.SoftwareType
import org.gradle.api.plugins.ApplicationPlugin

/**
 * Creates a declarative [JavaApplication] DSL model, applies the official Java application plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneJavaApplicationPlugin : Plugin<Project> {
    @get:SoftwareType(name = "javaApplication", modelPublicType = JavaApplication::class)
    abstract val application: JavaApplication

    override fun apply(project: Project) {
        val dslModel = application

        project.plugins.apply(ApplicationPlugin::class.java)

        linkDslModelToPlugin(project, dslModel)
    }

    private fun linkDslModelToPlugin(project: Project, dslModel: JavaApplication) {
        JvmPluginSupport.linkJavaVersion(project, dslModel)
        JvmPluginSupport.linkApplicationMainClass(project, dslModel)
        JvmPluginSupport.linkMainSourceSourceSetDependencies(project, dslModel.dependencies)
    }
}
