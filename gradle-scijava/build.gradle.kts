import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild
import java.io.ByteArrayOutputStream

plugins {
    `java-platform`
    `version-catalog`
    `maven-publish`
    //    id("org.gradlex.java-ecosystem-capabilities-base") // only rules
    //    id("org.gradlex.logging-capabilities") // logging extension
}

layout.buildDirectory = layout.projectDirectory.asFile.parentFile.resolve("target/gradle")

group = "org.scijava"
version = "38.0.0-SNAPSHOT"

javaPlatform {
    allowDependencies()
}

val computeCatalogAndPlatform = tasks.register<Exec>("generateCatalog") {

    workingDir = projectDir.parentFile
    commandLine("sh", "-c", "mvn -B -f pom.xml help:effective-pom")
    standardOutput = ByteArrayOutputStream()

    doLast {
        var output = standardOutput.toString()
        // clean output from dirty
        output = output.substringAfter("\n\n").substringBefore("\n\n")

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

            catalog.versionCatalog { library(alias, gav) }

            dependencies {
                constraints {
                    val ga = "$g:$a"
                    if (ga in runtimeDeps || jogampNatives.any { it.startsWith(ga) })
                        runtime(gav) //.also { println("runtime($dep)") }
                    else
                        api(gav) //.also{ println("api($dep)") }
                }
            }
        }

        for ((alias, aliases) in bundles)
            catalog.versionCatalog { bundle(alias, aliases) }
    }
}

publishing {
    publications {
        repositories {
            maven {
                name = "sciJava"
                //                credentials(PasswordCredentials::class)
                //                url = uri("https://maven.scijava.org/content/repositories/releases")
                url = uri("repo")
            }
        }
        create<MavenPublication>("pomScijava") {
            from(components["javaPlatform"])
            //            from(components["versionCatalog"])
        }
    }
}

val versionCatalogElements by configurations
val javaPlatform by components.existing {
    this as AdhocComponentWithVariants
    addVariantsFromConfiguration(versionCatalogElements) {}
}

tasks {
    // dependsOn runs only if the src is successful, finalizedBy not
    generateCatalogAsToml { dependsOn(computeCatalogAndPlatform) }
    val generateMetadataFileForPomScijavaPublication by getting { dependsOn(computeCatalogAndPlatform) }
    register("generateCatalogAndPlatform") { dependsOn(generateMetadataFileForPomScijavaPublication, generateCatalogAsToml) }
}

val runtimeDeps = listOf("org.antlr:antlr-runtime",
                         "xalan:serializer",
                         "xalan:xalan",
                         "com.github.vbmacher:java-cup-runtime",
                         "nz.ac.waikato.cms.weka.thirdparty:java-cup-11b-runtime",
                         "org.jogamp.gluegen:gluegen-rt-main",
                         "org.jogamp.gluegen:gluegen-rt",
                         "org.jogamp.joal:joal",
                         "org.jogamp.jocl:jocl",
                         "org.jogamp.jogl:jogl-all-main",
                         "org.jogamp.jogl:jogl-all",
                         "org.jogamp.jogl:jogl-all-noawt",
                         "com.nativelibs4java:bridj",
                         "org.bytedeco:ffmpeg",
                         "org.bytedeco:hdf5",
                         "org.bytedeco:leptonica",
                         "org.bytedeco:openblas",
                         "org.bytedeco:opencv",
                         "org.bytedeco:tesseract",
                         "org.jline:jline-native",
                         "com.github.jnr:jffi",
                         "org.jzy3d:jzy3d-native-jogl-awt",
                         "org.jzy3d:jzy3d-native-jogl-swing")

val jogampNatives = listOf("org.jogamp.gluegen:gluegen-rt-natives-",
                           "org.jogamp.jogl:jogl-all-natives-",
                           "org.jogamp.gluegen:gluegen-rt-natives-",
                           "org.jogamp.jogl:jogl-all-natives-")