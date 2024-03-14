import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    `java-platform`
    publish
//    id("org.gradlex.java-ecosystem-capabilities-base") // only rules
//    id("org.gradlex.logging-capabilities") // logging extension
}

group = "org.scijava"
version = "0.1" //(effXml / "version").toString()

operator fun GPathResult.div(child: String) = children().find { (it!! as NodeChild).name() == child } as GPathResult

val effXml = XmlSlurper().parse(projectDir.resolve("eff.xml"))

javaPlatform {
    allowDependencies()
}

val runtimeDeps = listOf("xalan:serializer", "xalan:xalan")

dependencies {

    val deps = effXml / "dependencyManagement" / "dependencies"
    constraints {
        deps.children().forEach {
            val node = it!! as NodeChild
            val g = node / "groupId"
            val a = node / "artifactId"
            val v = node / "version"
            if ("$g:$a" in runtimeDeps)
                runtime("$g:$a:$v")
            else
                api("$g:$a:$v")
            //            println("$g:$a:$v")
        }
    }
    //    versionCatalogs.forEach { versionCatalog ->
    //        println("catalog ${versionCatalog.name}")
    //        versionCatalog.libraryAliases.forEach {
    //            println(versionCatalog.findLibrary(it).get().get())
    //        }
    //    }

    api(platform("com.fasterxml.jackson:jackson-bom:" + libs.com.fasterxml.jackson.core.jacksonCore.get().version))
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