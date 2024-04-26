package org.scijava

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.experimental.java.StandaloneJavaApplicationPlugin

class SciJavaApplicationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(StandaloneJavaApplicationPlugin::class.java)
    }
}