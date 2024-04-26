package org.gradle.api.experimental.jvm

import org.gradle.api.Action
import org.gradle.api.tasks.Nested
import org.gradle.declarative.dsl.model.annotations.Configuring
import org.gradle.declarative.dsl.model.annotations.Restricted

/**
 * A component that is built for multiple Java versions.
 */
@Restricted
interface HasJavaTargets {
    @get:Nested
    val targets: JvmTargetContainer

    @Configuring
    fun targets(action: Action<in JvmTargetContainer>) = action.execute(targets)
}
