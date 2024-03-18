import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild

plugins {
    `version-catalog`
    `maven-publish`
}

group = "org.scijava"
version = "0.11"

catalog.versionCatalog {

    operator fun GPathResult.div(child: String) = children().find { (it!! as NodeChild).name() == child } as GPathResult

    val effXml = XmlSlurper().parse(projectDir.resolve("eff.xml"))
    val deps = effXml / "dependencyManagement" / "dependencies"
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

        //            println(gav)
        val camel = "$a".split('-', '_')
            .joinToString("") { if (it.isEmpty()) "" else it[0].uppercase() + it.substring(1).lowercase() }
            .replaceFirstChar { it.lowercase() }

        fun getAlias(group: String = "$g".substringAfterLast('.')): String {
            val alias = "$group." + when {
                camel.startsWith(group) -> camel.substringAfter(group).replaceFirstChar { it.lowercase() }.ifEmpty { group }
                else -> camel
            }
            bundles.getOrPut(group, ::ArrayList) += alias
            return alias
        }

        val alias = when ("$g") {
            in listOf("org.scijava", "net.imagej", "net.imglib2", "sc.fiji", "org.janelia.saalfeldlab") -> getAlias()
            "io.scif" -> getAlias("scifio")
            else -> "$g.$camel"
        }

        //                    println("$alias($gav)")
        library(alias, gav)
    }

    for ((alias, aliases) in bundles)
        bundle(alias, aliases)

    //    jakarta()

    //    version("groovy", "3.0.5")
    //    library("groovy-core", "org.codehaus.groovy", "groovy").versionRef("groovy")
}

//fun VersionCatalogBuilder.jakarta() {
//    val bom = URL.of("https://repo1.maven.org/maven2/jakarta/platform/jakarta.jakartaee-bom/10.0.0/jakarta.jakartaee-bom-10.0.0.pom")
//    library("jakarta.json", "jakarta.json:jakarta.json-api:")
//}

publishing {
    publications {
        repositories {
            maven {
                name = "sciJava"
                credentials(PasswordCredentials::class)
                url = uri("https://maven.scijava.org/content/repositories/releases")
            }
        }
        create<MavenPublication>("sciJavaCatalog") {
            from(components["versionCatalog"])
        }
    }
}