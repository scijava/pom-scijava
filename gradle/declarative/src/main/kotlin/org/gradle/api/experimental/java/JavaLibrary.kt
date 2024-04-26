package org.gradle.api.experimental.java

import org.gradle.api.experimental.jvm.HasJavaTarget
import org.gradle.api.experimental.jvm.HasJvmLibrary
import org.gradle.declarative.dsl.model.annotations.Restricted

/**
 * A library implemented using a single version of Java.
 */
@Restricted
interface JavaLibrary : HasJavaTarget, HasJvmLibrary

//inline fun javaLibrary(block: JavaLibrary.() -> Unit): JavaLibrary {
//    return object : JavaLibrary{
//        override val javaVersion: Property<Int> = Property
//        override val dependencies: LibraryDependencies
//            get() = TODO("Not yet implemented")
//
//    }.also(block)
//}