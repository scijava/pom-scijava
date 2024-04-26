package org.scijava

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.java.StandaloneJavaApplicationPlugin
import org.gradle.api.experimental.kotlin.StandaloneKotlinJvmApplicationPlugin

class SciJavaKotlinApplicationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(StandaloneKotlinJvmApplicationPlugin::class.java)
    }
}