package org.gradle.api.experimental.java

import org.gradle.api.experimental.jvm.HasJavaTarget
import org.gradle.api.experimental.jvm.HasJvmApplication
import org.gradle.api.experimental.jvm.HasJvmLibrary
import org.gradle.declarative.dsl.model.annotations.Restricted
import org.scijava.Fine

/**
 * An application implemented using a single version of Java.
 */
@Restricted
interface JavaApplication : HasJavaTarget, HasJvmApplication