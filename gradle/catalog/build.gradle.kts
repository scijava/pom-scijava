import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild
import java.io.ByteArrayOutputStream

plugins {
    `version-catalog`
    `maven-publish`
}

group = "org.scijava"
version = "0.11"

tasks {

    val generateCatalog by register<Exec>("generateCatalog") {

        workingDir = projectDir.parentFile.parentFile
        commandLine("sh", "-c", "mvn -B -f pom.xml help:effective-pom")
        standardOutput = ByteArrayOutputStream()

        doLast {
            var output = standardOutput.toString()
            // clean output from dirty
            output = output.substringAfter("\n\n").substringBefore("\n\n")

            catalog.versionCatalog {

                operator fun GPathResult.div(child: String) = children().find { (it!! as NodeChild).name() == child } as GPathResult

                val xml = XmlSlurper().parseText(output)
                val deps = xml / "dependencyManagement" / "dependencies"
                val bundles = mutableMapOf<String, ArrayList<String>>()
                val skip = listOf("mpicbg" to "mpicbg_")
                val cache = mutableSetOf<String>() // skip duplicates, such as org.bytedeco:ffmpeg
                for (dep in deps.children()) {
                    val node = dep as NodeChild
                    val g = node / "groupId"
                    val a = node / "artifactId"
                    val v = node / "version"
                    val gav = "$g:$a:$v"

                    if (("$g" to "$a") in skip || gav in cache)
                        continue

                    cache += gav

                    val camel = "$a".split('-', '_')
                            .joinToString("") { if (it.isEmpty()) "" else it[0].uppercase() + it.substring(1).lowercase() }
                            .replaceFirstChar { it.lowercase() }

                    fun getAlias(group: String): String {
                        val alias = "$group." + when {
                            camel.startsWith(group) -> camel.substringAfter(group).replaceFirstChar { it.lowercase() }.ifEmpty { group }
                            else -> camel
                        }
                        bundles.getOrPut(group, ::ArrayList) += alias
                        return alias
                    }

                    val lastWordAsGroup = listOf("org.scijava", "net.imagej", "net.imglib2", "sc.fiji", "org.janelia.saalfeldlab")
                    val alias = when ("$g") {
                        in lastWordAsGroup -> getAlias(g.toString().substringAfterLast('.'))
                        "io.scif" -> getAlias("scifio")
                        else -> "$g.$camel"
                    }

                    library(alias, gav)
                }

                for ((alias, aliases) in bundles)
                    bundle(alias, aliases)
            }
        }
    }
    // dependsOn runs only if the src is successful, finalizedBy not
    generateCatalogAsToml { dependsOn(generateCatalog) }
}

publishing {
    publications {
        create<MavenPublication>("sciJavaCatalog") { from(components["versionCatalog"]) }
        repositories {
            maven {
                name = "sciJava"
                credentials(PasswordCredentials::class)
                url = uri("https://maven.scijava.org/content/repositories/releases")
                //                url = uri("repo")
            }
        }
    }
}