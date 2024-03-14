plugins {
    embeddedKotlin("jvm")
    id("org.gradlex.java-ecosystem-capabilities-base")
}

repositories {
    mavenCentral()
    maven("https://maven.scijava.org/content/repositories/releases")
    maven("https://maven.scijava.org/content/groups/public")
}

// it will appear as this was the original downloaded metadata of the dependency
// @CacheableRule is more verbose but more efficient, because the rule will be applied before the dependency get cached,
inline fun <reified T : ComponentMetadataRule> ComponentMetadataHandler.withModules(vararg modules: String) {
    for (module in modules) withModule<T>(module)
}
abstract class Rule : ComponentMetadataRule {
    fun ComponentMetadataContext.remove(vararg modules: String) = details.allVariants { withDependencies { removeIf { it.module.toString() in modules } } }
}
dependencies {
    components {
        @CacheableRule open class NoXalan : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("xalan:serializer", "xalan:xalan")
        }
        withModules<NoXalan>("org.openmicroscopy:ome-common", "ome:bio-formats-tools", "ome:formats-api", "ome:formats-bsd",
                             "org.apache.xmlgraphics:batik-bridge", "org.apache.xmlgraphics:batik-dom")

        @CacheableRule open class NoUeberJars : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("org.apache.xmlgraphics:batik-all", "it.unimi.dsi:fastutil")
        }
        withModules<NoUeberJars>("org.openmicroscopy:omero-blitz", "org.openmicroscopy:omero-server")

        @CacheableRule open class NoAndroid : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("com.google.android.tools:dx")
        }
        withModules<NoAndroid>("net.clearcontrol:coremem", "com.nativelibs4java:bridj")

        @CacheableRule open class ClearGl : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("org.jogamp.gluegen:gluegen-rt-main", "org.jogamp.jogl:jogl-all-main")
        }
        withModule<ClearGl>("net.clearvolume:cleargl")

        @CacheableRule open class SparkCore211 : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("io.netty:netty-all")
        }
        withModule<SparkCore211>("org.apache.spark:spark-core_2.11")

        @CacheableRule open class NoBatikExtensions : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove("org.apache.xmlgraphics:batik-extensions")
        }
        withModules<NoBatikExtensions>("org.apache.xmlgraphics:batik-rasterizer-ext", "org.apache.xmlgraphics:batik-squiggle-ext")

        @CacheableRule open class NoJogampNatives : Rule() {
            override fun execute(context: ComponentMetadataContext) {
                // Conflicts with properly packaged jogamp native classifiers.
                val libs = listOf("gluegen:gluegen-rt", "jogl:jogl-all")
                val modules = listOf("linux-aarch64", "linux-amd64", "linux-armv6hf", "linux-i586", "macosx-universal", "windows-amd64", "windows-i586")
                    .flatMap { p -> libs.map { "org.jogamp.$it-natives-$p" } }.toTypedArray()
                context.remove(*modules)
            }
        }
        withModules<NoJogampNatives>("org.jzy3d:jzy3d-native-jogl-awt", "org.jzy3d:jzy3d-native-jogl-core")

        @CacheableRule open class Weka : Rule() {
            override fun execute(context: ComponentMetadataContext) = context.remove(
                "com.github.fommil.netlib:all", "com.googlecode.netlib-java:netlib-java", "net.sourceforge.f2j:arpack_combined_all",
                "nz.ac.waikato.cms.weka.thirdparty:java-cup-11b", "nz.ac.waikato.cms.weka.thirdparty:java-cup-11b-runtime")
        }
        withModule<Weka>("nz.ac.waikato.cms.weka:weka-dev")
    }
    implementation(libs.org.morphonets.snt)
    implementation("org.jzy3d:jzy3d-emul-gl-awt:2.1.0")
}