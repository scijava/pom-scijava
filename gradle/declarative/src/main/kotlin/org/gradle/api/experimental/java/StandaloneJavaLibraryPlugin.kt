package org.gradle.api.experimental.java

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.jvm.internal.JvmPluginSupport
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.kotlin.dsl.create
import org.scijava.ValidationLibrary

/**
 * Creates a declarative [JavaLibrary] DSL model, applies the official Java library plugin,
 * and links the declarative model to the official plugin.
 */
abstract class StandaloneJavaLibraryPlugin : Plugin<Project>, ValidationLibrary {
    //    @get:SoftwareType(name = "javaLibrary", modelPublicType = JavaLibrary::class)
    //    abstract val library: JavaLibrary

    override val extensionName = "javaLibrary"

    override fun apply(project: Project) {
        val javaLibrary = project.extensions.create<JavaLibrary>("javaLibrary")
        val dslModel = javaLibrary

        project.plugins.apply(JavaLibraryPlugin::class.java)

        linkDslModelToPlugin(project, dslModel)

        project.validate()
    }

    private fun linkDslModelToPlugin(project: Project, dslModel: JavaLibrary) {
        JvmPluginSupport.linkJavaVersion(project, dslModel)
        JvmPluginSupport.linkMainSourceSourceSetDependencies(project, dslModel.dependencies)
    }
}
