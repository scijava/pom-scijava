rootProject.name = "declarative-playground"

rootDir.list()!!.forEach {
    if ("java" in it || "kotlin" in it)
        include(it)
}
includeBuild("../declarative")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories { mavenCentral() }
}