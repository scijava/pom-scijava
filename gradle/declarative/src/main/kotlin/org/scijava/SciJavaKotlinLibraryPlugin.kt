package org.scijava

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.java.StandaloneJavaLibraryPlugin
import org.gradle.api.experimental.kotlin.StandaloneKotlinJvmLibraryPlugin

class SciJavaKotlinLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(StandaloneKotlinJvmLibraryPlugin::class.java)
    }
}