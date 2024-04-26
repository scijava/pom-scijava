//import org.gradle.api.experimental.java.javaLibrary

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.2.1"
}

repositories { mavenCentral() }

//javaLibrary {
//    javaVersion = 11
//}

dependencies {
//    implementation(kotlin("multiplatform", embeddedKotlinVersion))
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$embeddedKotlinVersion")
//    implementation(kotlin("jvm"))
}

gradlePlugin {
    plugins {
//        create("jvm-library") {
//            id = "org.gradle.experimental.jvm-library"
//            implementationClass = "org.gradle.api.experimental.jvm.StandaloneJvmLibraryPlugin"
//            tags = setOf("declarative-gradle", "java", "jvm")
//        }
//        create("java-library") {
//            id = "org.gradle.experimental.java-library"
//            implementationClass = "org.gradle.api.experimental.java.StandaloneJavaLibraryPlugin"
//            tags = setOf("declarative-gradle", "java", "jvm")
//        }
        create("scijava.library") {
            id = "org.$name"
            implementationClass = "org.scijava.SciJavaLibraryPlugin"
            tags = setOf("declarative-gradle", "scijava", "java", "jvm")
        }
        create("scijava.application") {
            id = "org.$name"
            implementationClass = "org.scijava.SciJavaApplicationPlugin"
            tags = setOf("declarative-gradle", "scijava", "java", "jvm")
        }
        create("scijava.kotlin-library") {
            id = "org.$name"
            implementationClass = "org.scijava.SciJavaKotlinLibraryPlugin"
            tags = setOf("declarative-gradle", "scijava", "java", "kotlin", "jvm")
        }
        create("scijava.kotlin-application") {
            id = "org.$name"
            implementationClass = "org.scijava.SciJavaKotlinApplicationPlugin"
            tags = setOf("declarative-gradle", "scijava", "java", "kotlin", "jvm")
        }
//        create("java-application") {
//            id = "org.gradle.experimental.java-application"
//            implementationClass = "org.gradle.api.experimental.java.StandaloneJavaApplicationPlugin"
//            tags = setOf("declarative-gradle", "java", "jvm")
//        }
//        create("jvm-ecosystem") {
//            id = "org.gradle.experimental.jvm-ecosystem"
//            implementationClass = "org.gradle.api.experimental.jvm.JvmEcosystemPlugin"
//            tags = setOf("declarative-gradle", "java", "jvm")
//        }
    }
}
