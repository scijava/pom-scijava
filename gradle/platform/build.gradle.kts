import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChild

plugins {
    `java-platform`
    `maven-publish`
}

tasks {
    register<Exec>("eff.xml") {
        commandLine("bash", "-c", "mvn -B -f \"../../pom.xml\" help:effective-pom | grep -A9999999 '^<?xml' | grep -B9999999 '^</project>' > eff.xml")
    }
}

operator fun GPathResult.div(child: String) = children().find { (it!! as NodeChild).name() == child } as GPathResult

val effXml = XmlSlurper().parse("platform/eff.xml")

dependencies {

    val deps = effXml / "dependencyManagement" / "dependencies"
    constraints {
        deps.children().forEach {
            val node = it!! as NodeChild
            val g = node / "groupId"
            val a = node / "artifactId"
            val v = node / "version"
            api("$g:$a:$v")
            //            println("$g:$a:$v")
        }
    }
}



publishing {
    publications {
        repositories {
            maven("to fill")
        }
        create<MavenPublication>("sciJavaPlatform") {
            groupId = "org.scijava"
            artifactId = "pom-scijava"
            version = (effXml / "version").toString()

            from(components["javaPlatform"])
            pom {
                name = "SciJava Parent POM"
                description = "This POM provides a parent from which participating projects can declare their build configurations. It ensures that projects all use a compatible build environment, including Java version, as well as versions of dependencies and plugins."
                url = "https://scijava.org/"
                inceptionYear = "2011"
                organization {
                    name = "SciJava"
                    url = "https://scijava.org/"
                }
                licenses {
                    license {
                        name = "Unlicense"
                        url = "https://unlicense.org/"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "ctrueden"
                        name = "Curtis Rueden"
                        url = "https://imagej.net/people/ctrueden"
                        roles.addAll("founder", "lead", "developer", "debugger", "reviewer", "support", "maintainer")
                    }
                }
                contributors {
                    operator fun String.invoke(id: String) = contributor {
                        name = this@invoke
                        url = "https://imagej.net/people/$id"
                        properties = mapOf("id" to id)
                    }
                    "Mark Hiner"("hinerm")
                    "Johannes Schindelin"("dscho")
                    "Sébastien Besson"("sbesson")
                    "John Bogovic"("bogovicj")
                    "Nicolas Chiaruttini"("NicoKiaru")
                    "Barry DeZonia"("bdezonia")
                    "Richard Domander"("rimadoma")
                    "Karl Duderstadt"("karlduderstadt")
                    "Jan Eglinger"("imagejan")
                    "Gabriel Einsdorf"("gab1one")
                    "Tiago Ferreira"("tferr")
                    contributor {
                        name = "David Gault"
                        properties = mapOf("id" to "dgault")
                    }
                    "Ulrik Günther"("skalarproduktraum")
                    "Philipp Hanslovsky"("hanslovsky")
                    "Stefan Helfrich"("stelfrich")
                    "Cameron Lloyd"("camlloyd")
                    "Hadrien Mary"("hadim")
                    "Tobias Pietzsch"("tpietzsch")
                    "Stephan Preibisch"("StephanPreibisch")
                    "Stephan Saalfeld"("axtimwalde")
                    "Deborah Schmidt"("frauzufall")
                    "Lorenzo Scianatico"("LoreScianatico")
                    "Jean - Yves Tinevez"("tinevez")
                    "Christian Tischer"("tischi")
                    "Gabriella Turek"("turekg")
                    contributor {
                        name = "Giuseppe Barbieri"
                        properties = mapOf("id" to "elect")
                    }
                }

                mailingLists {
                    mailingList {
                        name = "SciJava"
                        subscribe = "https://groups.google.com/group/scijava"
                        unsubscribe = subscribe
                        post = "scijava@googlegroups.com"
                        archive = "https://groups.google.com/group/scijava"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/scijava/pom-scijava"
                    developerConnection = "scm:git:git@github.com:scijava/pom-scijava"
                    tag = "HEAD"
                    url = "https://github.com/scijava/pom-scijava"
                }
                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/scijava/pom-scijava/issues"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https: //github.com/scijava/pom-scijava/actions"
                }
            }
        }
    }
}

