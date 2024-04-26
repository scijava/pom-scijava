package org.gradle.api.experimental.jvm

import org.gradle.api.Named

abstract class JavaTarget(val javaVersion: Int) : JvmTarget, Named {
    override fun getName(): String = "java$javaVersion"
}
