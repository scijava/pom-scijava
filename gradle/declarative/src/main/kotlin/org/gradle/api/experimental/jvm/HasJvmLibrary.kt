package org.gradle.api.experimental.jvm

import org.gradle.api.Action
import org.gradle.api.experimental.common.LibraryDependencies
import org.gradle.api.tasks.Nested
import org.gradle.declarative.dsl.model.annotations.Configuring
import org.gradle.declarative.dsl.model.annotations.Restricted
import org.scijava.Fine

/**
 * Represents a library that runs on the JVM.
 */
@Restricted
interface HasJvmLibrary {
    @get:Nested
    val dependencies: LibraryDependencies

    @Configuring
    fun dependencies(action: Action<in LibraryDependencies>) = action.execute(dependencies)
}