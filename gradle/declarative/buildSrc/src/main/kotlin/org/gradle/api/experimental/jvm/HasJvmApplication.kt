package org.gradle.api.experimental.jvm

import org.gradle.api.Action
import org.gradle.api.experimental.common.ApplicationDependencies
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.declarative.dsl.model.annotations.Configuring
import org.gradle.declarative.dsl.model.annotations.Restricted

/**
 * Represents an application that runs on the JVM.
 */
@Restricted
interface HasJvmApplication {
    @get:Restricted
    val mainClass: Property<String>

    @get:Nested
    val dependencies: ApplicationDependencies

    @Configuring
    fun dependencies(action: Action<in ApplicationDependencies>) = action.execute(dependencies)
}
