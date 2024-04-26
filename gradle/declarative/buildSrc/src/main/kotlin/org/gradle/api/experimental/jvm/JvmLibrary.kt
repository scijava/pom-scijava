package org.gradle.api.experimental.jvm

import org.gradle.declarative.dsl.model.annotations.Restricted

/**
 * A library that runs on the JVM and that is implemented using one or more versions of Java.
 */
@Restricted
interface JvmLibrary : HasJavaTargets, HasJvmLibrary
