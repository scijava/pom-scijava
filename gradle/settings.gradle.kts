// This is an empty umbrella build including all the component builds.
// This build is not necessarily needed. The component builds work independently.

rootProject.name = "gradle"

includeBuild("catalog")
includeBuild("platform")
includeBuild("playground")
includeBuild("enforcer")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")