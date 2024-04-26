package org.gradle.api.experimental.common

import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.declarative.dsl.model.annotations.Restricted

/**
 * The declarative dependencies DSL block for an application.
 */
@Restricted
interface ApplicationDependencies : Dependencies {
    val implementation: DependencyCollector
    val runtimeOnly: DependencyCollector
    val compileOnly: DependencyCollector
}
