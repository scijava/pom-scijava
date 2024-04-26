package org.scijava

import org.gradle.api.Project

internal interface ValidationApplication : ValidationLibrary
internal interface ExtensionName {
    val extensionName: String
}

internal interface ValidationLibrary : ExtensionName {

    fun Project.validate() {

        val lineIterator = buildFile.readText().lines().filter { it.isNotBlank() && !it.startsWith("//") }
            .map(String::trim).toMutableList().listIterator()
        while (lineIterator.hasNext()) {
            var line = lineIterator.next()
            reformatIfNeeded(line, lineIterator)
            when (line) {
                "plugins {" ->
                    while (true) {
                        line = lineIterator.next()
                        if (line == "}") break
                        check(line.startsWith("id(\""))
                    }
                "$extensionName {" -> validate(lineIterator)
                else -> error("forbidden code `$line`")
            }
        }
        println("build script verification passed!")
        //    while (lineIterator.hasPrevious())
        //        lineIterator.previous()
        //    println(lineIterator.asSequence().joinToString("\n"))
    }

    private fun reformatIfNeeded(line: String, lineIterator: MutableListIterator<String>) {
        if ('}' in line)
            if ('{' in line) { // ie `repositories { mavenCentral() }`
                check(line.last() == '}')
                val second = line.dropLast(1).substringAfter('{')
                val first = line.substringBefore('{')
                lineIterator.apply {
                    set("$first {")
                    add(second)
                    add("}")
                }
            } else // ie `whatever}`
                lineIterator.apply {
                    set(line.dropLast(1))
                    add("}")
                }
    }

    infix fun validate(lineIterator: MutableListIterator<String>) {
        while (true) {
            var line = lineIterator.next()
            if (line == "}") break
            when {
                line.startsWith("javaVersion =") -> Fine
                //                line.startsWith("repositories {") -> Fine
                line.startsWith("dependencies {") -> {
                    val configurations = listOf("api", "implementation", "runtimeOnly", "compileOnly")
                    while (true) {
                        line = lineIterator.next()
                        if (line == "}") break
                        check(configurations.any(line::startsWith))
                    }
                }
                else -> when {
                    line.startsWith("mainClass =") && this is ValidationApplication -> Fine
                    else -> error("forbidden code in `$line`")
                }
            }
        }
    }
}

typealias Fine = Unit