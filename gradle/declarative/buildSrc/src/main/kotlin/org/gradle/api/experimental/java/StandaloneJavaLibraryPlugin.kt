package org.gradle.api.experimental.java

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport
import org.gradle.api.internal.plugins.software.SoftwareType
import org.gradle.api.plugins.JavaLibraryPlugin

/**
 * Creates a declarative [JavaLibrary] DSL model, applies the official Java library plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneJavaLibraryPlugin : Plugin<Project> {
    @get:SoftwareType(name = "javaLibrary", modelPublicType = JavaLibrary::class) abstract val library: JavaLibrary

    override fun apply(project: Project) {
        val dslModel = library

        project.plugins.apply(JavaLibraryPlugin::class.java)

        linkDslModelToPlugin(project, dslModel)
    }

    private fun linkDslModelToPlugin(project: Project, dslModel: JavaLibrary) {
        JvmPluginSupport.linkJavaVersion(project, dslModel)
        JvmPluginSupport.linkMainSourceSourceSetDependencies(project, dslModel.dependencies)
    }
}
