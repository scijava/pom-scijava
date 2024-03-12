plugins {
    embeddedKotlin("jvm")
}

repositories {
    mavenCentral()
    maven("https://maven.scijava.org/content/repositories/releases")
    maven("https://maven.scijava.org/content/groups/public")
}

//configurations.filter { it.isCanBeResolved }.map { println(it) }

dependencies {
    api(libs.imagej.ij)

    // Activate the "ScijavaCapability" rule
//    components.all<ScijavaCapability>()
}

open class ConflictCapability(val group: String,
                              val artifact: String = group,
                              val modules: List<String>,
                              val reason: String? = null) {
    val ga get() = "$group:$artifact"
}

class ResolutionCapability(group: String, artifact: String = group, modules: List<String>, reason: String? = null) : ConflictCapability(group, artifact, modules, reason)

// the right module to select will be always the last in the list
val resolutions = listOf(
    ResolutionCapability("jzy3d", "emul-gl", listOf("org.jzy3d:jzy3d-emul-gl", "org.jzy3d:jzy3d-emul-gl-awt"), "Replaced by org.jzy3d:jzy3d-emul-gl-awt"),
    ResolutionCapability("jzy3d", "jGL", listOf("org.jzy3d:jGL", "jzy3d-jGL-awt"), "Replaced by org.jzy3d:jzy3d-jGL-awt"),
    ResolutionCapability("javax", "transaction", listOf("javax.transaction:jta", "jzy3d-jGL-awt"), "Replaced by org.jzy3d:jzy3d-jGL-awt"),
    ResolutionCapability("javax", "servlet", listOf("javax.servlet:servlet-api", "javax.servlet:javax.servlet-api")))
val conflicts = listOf(
    ConflictCapability("slf4j", modules = listOf("ch.qos.logback:logback-classic", "org.slf4j:slf4j-simple")),
    ConflictCapability("jcl", modules = listOf("commons-logging:commons-logging", "org.slf4j:jcl-over-slf4j")),
    ConflictCapability("jakarta", "activation",
                       listOf("jakarta.activation:jakarta.activation-api", "com.sun.activation:javax.activation", "javax.activation:activation"),
                       "See: https://wiki.eclipse.org/Jakarta_EE_Maven_Coordinates"),
    ConflictCapability("javax" , "transaction", listOf("javax.transaction:javax.transaction-api", "javax.transaction:jta")),
    ConflictCapability("apache", "commonsCsv", listOf("org.apache.commons:commons-csv", "org.apache.solr:solr-commons-csv")),
    ConflictCapability("c3p0", modules = listOf("com.mchange:c3p0", "com.mchange:mchange-commons-java", "c3p0:c3p0")),
    ConflictCapability("javax", "mail", listOf("javax.mail:mail", "com.sun.mail:javax.mail")),
    ConflictCapability("jersey", "core", listOf("javax.ws.rs:javax.ws.rs-api", "com.sun.jersey:jersey-core")),
    ConflictCapability("commons", "beanutils", listOf("commons-beanutils:commons-beanutils", "commons-collections:commons-collections", "commons-beanutils:commons-beanutils-core")))

// TODO

// Uber-JAR of all batik components
// org.apache.xmlgraphics:batik-all

// Conflicts with junit:junit. We don't need Android here.
// com.google.android.tools:dx

// Brings in all gluegen-rt native classifiers.
// org.jogamp.gluegen:gluegen-rt-main

// Brings in all jogl-all native classifiers.
// org.jogamp.jogl:jogl-all-main

// Uber-JAR of all netty components
// io.netty:netty-all

/*
				<groupId>org.jzy3d</groupId>
				<artifactId>jzy3d-native-jogl-awt</artifactId>
				<version>${org.jzy3d.jzy3d-native-jogl-awt.version}</version>
				<exclusions>
					<!-- Conflicts with properly packaged jogamp native classifiers. -->
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-aarch64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-armv6hf</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-windows-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-windows-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-aarch64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-armv6hf</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-windows-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-windows-i586</artifactId>
					</exclusion>


									<groupId>org.jzy3d</groupId>
				<artifactId>jzy3d-native-jogl-core</artifactId>
				<version>${org.jzy3d.jzy3d-native-jogl-core.version}</version>
				<exclusions>
					<!-- Conflicts with properly packaged jogamp native classifiers. -->
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-aarch64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-armv6hf</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-linux-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-windows-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.gluegen</groupId>
						<artifactId>gluegen-rt-natives-windows-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-aarch64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-armv6hf</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-linux-i586</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-macosx-universal</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-windows-amd64</artifactId>
					</exclusion>
					<exclusion>
						<groupId>org.jogamp.jogl</groupId>
						<artifactId>jogl-all-natives-windows-i586</artifactId>
					</exclusion>
*/

class ScijavaCapability : ComponentMetadataRule {
    override
    fun execute(context: ComponentMetadataContext) = context.details.run {
        for (res in resolutions + conflicts) {
            if (id.module.toString() in res.modules)
                allVariants {
                    withCapabilities {
                        // Declare that all of them provide the same capability
                        addCapability(res.group, res.artifact, id.version)
                    }
                }
        }
    }
}

configurations.all {
//    dependencies.forEach { it. }
    resolutionStrategy.capabilitiesResolution {
        for (res in resolutions)
            withCapability(res.ga) {
                for (cand in candidates) {
                    val id = cand.id
                    if (id is ModuleComponentIdentifier && id.moduleIdentifier.toString() == res.modules.last())
                        select(cand)
                }
                res.reason?.let(::because)
            }
    }
}