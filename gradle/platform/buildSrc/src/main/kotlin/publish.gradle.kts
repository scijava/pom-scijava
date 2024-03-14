plugins {
    `maven-publish`
}

publishing {
    publications {
        repositories {
            maven {
                name = "sciJava"
                credentials(PasswordCredentials::class)
                url = uri("https://maven.scijava.org/content/repositories/releases")
            }
        }
        create<MavenPublication>("sciJavaPlatform") {
            from(components["javaPlatform"])
            /*pom.withXml {
                val nodes = Stack<Node>()

                nodes += asNode()
                fun parent() = nodes.peek()
                // Definition of `groupId` is redundant, because it's inherited from the parent
                run {
                    val list = parent().children() as NodeList
                    require(list.removeIf { (it as Node).name().toString().substringAfter("{http://maven.apache.org/POM/4.0.0}") == "groupId" })
                }

                var i = 1 // <modelVersion>4.0.0</modelVersion> is first

                operator fun String.invoke(vararg values: Any?) {
                    for (value in values) {
                        val node = when (value) {
                            null -> parent().appendNode(this)
                            else -> parent().appendNode(this, value)
                        }
                        if (nodes.size == 1) { // only at `project` level
                            val list = parent().value() as NodeList
                            list.removeLast()
                            list.add(i++, node)
                        }
                    }
                }

                //                operator fun String.compareTo(value: Any): Int {
                //                    return 0
                //                }

                operator fun String.invoke(block: () -> Unit) {
                    val node = parent()
                    nodes += node.appendNode(this)
                    block()
                    if (nodes.size == 2) { // only as `project` child
                        val list = node.value() as NodeList
                        list.removeLast()
                        list.add(i++, parent())
                    }
                    nodes.pop()
                }

                //                fun comment(text: String) {
                //                    val builder = asString()
                //                    builder.insert(builder.lastIndex - "</project>".length, text.trimMargin())
                //                }

                "parent" {
                    "groupId"(group)
                    "artifactId"(project.name)
                    "version"(version)
                    "relativePath"()
                }
                // <artifactId>
                // <version>
                // <packaging>
                i += 3
                "name"("SciJava Parent POM")
                "description"("This POM provides a parent from which participating projects can declare their build configurations. It ensures that projects all use a compatible build environment, including Java version, as well as versions of dependencies and plugins.")
                "url"("https://scijava.org/")
                "inceptionYear"("2011")
                "organization" {
                    "name"("SciJava")
                    "url"("https://scijava.org/")
                }
                "licenses" {
                    "license" {
                        "name"("Unlicense")
                        "url"("https://unlicense.org/")
                        "distribution"("repo")
                    }
                }
                "developers" {
                    "developer" {
                        "id"("ctrueden")
                        "name"("Curtis Rueden")
                        "url"("https://imagej.net/people/ctrueden")
                        "roles" {
                            "role"("founder", "lead", "developer", "debugger", "reviewer", "support", "maintainer")
                        }
                    }
                }
                "contributors" {
                    operator fun String.minus(id: String) = "contributor" {
                        "name"(this)
                        "url"("https://imagej.net/people/$id")
                        "properties" { "id"(id) }
                    }
                    "Mark Hiner" - "hinerm"
                    "Johannes Schindelin" - "dscho"
                    "Sébastien Besson" - "sbesson"
                    "John Bogovic" - "bogovicj"
                    "Nicolas Chiaruttini" - "NicoKiaru"
                    "Barry DeZonia" - "bdezonia"
                    "Richard Domander" - "rimadoma"
                    "Karl Duderstadt" - "karlduderstadt"
                    "Jan Eglinger" - "imagejan"
                    "Gabriel Einsdorf" - "gab1one"
                    "Tiago Ferreira" - "tferr"
                    "contributor" {
                        "name"("David Gault")
                        "properties" { "id"("dgault") }
                    }
                    "Ulrik Günther" - "skalarproduktraum"
                    "Philipp Hanslovsky" - "hanslovsky"
                    "Stefan Helfrich" - "stelfrich"
                    "Cameron Lloyd" - "camlloyd"
                    "Hadrien Mary" - "hadim"
                    "Tobias Pietzsch" - "tpietzsch"
                    "Stephan Preibisch" - "StephanPreibisch"
                    "Stephan Saalfeld" - "axtimwalde"
                    "Deborah Schmidt" - "frauzufall"
                    "Lorenzo Scianatico" - "LoreScianatico"
                    "Jean - Yves Tinevez" - "tinevez"
                    "Christian Tischer" - "tischi"
                    "Gabriella Turek" - "turekg"
                    "contributor" {
                        "name"("Giuseppe Barbieri")
                        "properties" { "id"("elect") }
                    }
                }
                "mailingLists" {
                    "mailingList" {
                        "name"("SciJava")
                        "subscribe"("https://groups.google.com/group/scijava")
                        "unsubscribe"("https://groups.google.com/group/scijava")
                        "post"("scijava@googlegroups.com")
                        "archive"("https://groups.google.com/group/scijava")
                    }
                }
                "scm" {
                    "connection"("scm:git:https://github.com/scijava/pom-scijava")
                    "developerConnection"("scm:git:git@github.com:scijava/pom-scijava")
                    "tag"("HEAD")
                    "url"("https://github.com/scijava/pom-scijava")
                }
                "issueManagement" {
                    "system"("GitHub Issues")
                    "url"("https://github.com/scijava/pom-scijava/issues")
                }
                "ciManagement" {
                    "system"("GitHub Actions")
                    "url"("https: //github.com/scijava/pom-scijava/actions")
                }
                "properties" {
                    // HACK: The following list of allowed-to-be-duplicated classes
                    // facilitates intended combinations of artifacts:
                    // ==
                    // Classes: com.google.inject.*
                    // Part of: com.google.inject:guice:no_aop
                    //         Also in: org.sonatype.sisu:sisu-guice
                    // Enables:
                    //     org.apache.maven:maven-core (guice) +
                    //     org.apache.maven.shared:maven-common-artifact-filters (sisu-guice)
                    // ==
                    // Class: javax.xml.namespace.QName
                    // Part of: Java Runtime Environment
                    //         Also in: xml-apis:xml-apis, xpp3:xpp3
                    // Enables: xml-apis:xml-apis + xpp3:xpp3
                    // ==
                    // TEMP: Until scijava/scripting-jruby#5 is resolved.
                    // Classes: jnr.ffi.*
                    // Part of: com.github.jnr:jnr-ffi
                    // Also in: org.jruby:jruby-core
                    // Enables: org.jruby:jruby-core + org.python:jython-slim (jnr-ffi)
                    // ==
                    // Classes: org.apache.hadoop.yarn.*.package-info
                    // Part of: org.apache.hadoop:hadoop-yarn-*
                    // Enables:
                    //     org.apache.hadoop:hadoop-yarn-<foo> +
                    //     org.apache.hadoop:hadoop-yarn-<bar>
                    // ==
                    // Classes: org.apache.spark.unused.UnusedStubClass
                    // Part of: org.apache.spark:spark-*
                    // Enables: org.apache.spark:spark-core_2.11 (spark-*)
                    // ==
                    // Classes: org.eclipse.aether.*
                    // Part of:
                    //     org.apache.maven.resolver:maven-resolver-api
                    //     org.apache.maven.resolver:maven-resolver-util
                    //     org.apache.maven.shared:maven-artifact-transfer
                    //     org.eclipse.aether:aether-api
                    //     org.eclipse.aether:aether-util
                    // Enables: dependence on Apache Maven libraries
                    // ==
                    // Classes: org.hibernate.stat.ConcurrentStatisticsImpl
                    // Part of: org.hibernate:hibernate-core
                    // Also in: org.openmicroscopy:omero-blitz
                    // Enables:
                    //     org.openmicroscopy:omero-blitz +
                    //     org.openmicroscopy:omero-model (hibernate-core)
                    // ==
                    // Classes: org.junit.runner.Runner
                    // Part of: junit:junit
                    //         Also in: org.jmockit:jmockit
                    //         Enables: junit:junit + org.jmockit:jmockit
                    // ==
                    // Classes: org.jzy3d.plot3d.pipelines.*
                    // Part of: org.jzy3d:jzy3d-core, org.jzy3d:jzy3d-native-jogl-awt
                    // Enables: org.jzy3d:jzy3d-core + org.jzy3d:jzy3d-native-jogl-awt
                    "scijava.allowedDuplicateClasses"("com.google.inject.*,javax.xml.namespace.QName,jnr.ffi.*,org.apache.hadoop.yarn.*.package-info,org.apache.spark.unused.UnusedStubClass,org.eclipse.aether.*,org.hibernate.stat.ConcurrentStatisticsImpl,org.junit.runner.Runner,org.jzy3d.plot3d.pipelines.*")
                    // NB: The scijava.allowedDuplicateClasses property above makes it easier to
                    // append to the list of allowed duplicate classes in downstream projects.
                    // Simply override the property in your POM with something like this:
                    // "allowedDuplicateClasses"("\${scijava.allowedDuplicateClasses},com.example.AnotherDuplicate")
                    "allowedDuplicateClasses"("\${scijava.allowedDuplicateClasses}")
                    // NB: We override these properties to make the enforcer happy.
                    // You will need to override them in your POM, too... to valid values.
                    "license.licenseName"("N/A")
                    "license.copyrightOwners"("N/A")
                }
            }*/
        }
    }
}