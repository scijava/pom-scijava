package org.gradle.api.experimental.kmp.internal

import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.experimental.jvm.HasJavaTarget
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

object KotlinPluginSupport {
    fun linkJavaVersion(project: Project, dslModel: HasJavaTarget) {
        val kotlin = project.extensions.getByType<KotlinJvmProjectExtension>()
        kotlin.jvmToolchain { languageVersion = dslModel.javaVersion.map(JavaLanguageVersion::of) }
    }
}
