plugins {
    `java-platform`
    publish
    //    id("org.gradlex.java-ecosystem-capabilities-base") // only rules
    //    id("org.gradlex.logging-capabilities") // logging extension
}

group = "org.scijava"
version = "0.13" //(effXml / "version").toString()

//operator fun GPathResult.div(child: String) = children().find { (it!! as NodeChild).name() == child } as GPathResult

//val effXml = XmlSlurper().parse(projectDir.resolve("eff.xml"))

javaPlatform {
    allowDependencies()
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

dependencies {

    //    val deps = effXml / "dependencyManagement" / "dependencies"
    constraints {
        //        deps.children().forEach {
        //            val node = it!! as NodeChild
        //            val g = node / "groupId"
        //            val a = node / "artifactId"
        //            val v = node / "version"
        //        }
        versionCatalogs.forEach { versionCatalog ->
            //            println("catalog ${versionCatalog.name}")
            versionCatalog.libraryAliases.forEach { lib ->
                val dep = versionCatalog.findLibrary(lib).get().get()
                val ga = dep.module.run { "$group:$name" }
                //                println(dep)
                if (ga in runtimeDeps || jogampNatives.any { it.startsWith(ga) })
                    runtime(dep) //.also { println("runtime($dep)") }
                else
                    api(dep) //.also{ println("api($dep)") }
            }
        }
    }

    //    println(libs.my.lib.get())
    //    api(platform("com.fasterxml.jackson:jackson-bom:" + libs.com.fasterxml.jackson.core.jacksonCore.get().version))
    api(platform("jakarta.platform:jakarta.jakartaee-bom:10.0.0"))
    //        api(platform("com.google.api-client:google-api-client-bom:" + libs.com.google.api.client.googleApiClient.get().version))
    //        api(platform("com.google.api:gax-bom:" + libs.com.google.api.gax.get().version))
    //        api(platform("com.google.api:gapic-generator-java-bom:" + libs.com.google.api.grpc.protoGoogleCommonProtos.get().version))
    //        api(platform("com.google.auth:google-auth-library-bom:" + libs.com.google.auth.googleAuthLibraryAppengine.get().version))
    //        // no google auto-value bom
    //        api(platform("com.google.cloud:google-cloud-core-bom:" + libs.com.google.cloud.googleCloudCore.get().version))
}




tasks {
    //    named<GenerateMavenPom>("generatePomFileForSciJavaPlatformPublication") {
    //        pom.withXml {
    //            asNode().appendNode("parent").apply {
    //                appendNode("groupId", group)
    //                appendNode("artifactId", project.name)
    //                appendNode("version", version)
    //                appendNode("relativePath")
    //            }
    //            println(asNode().name())
    //        }
    //    }
}