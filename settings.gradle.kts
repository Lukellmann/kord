rootProject.name = "kord"

include(
    "bom",
    "common",
    "core",
    "gateway",
    "generators",
    "ksp-annotations",
    "ksp-processors",
    "rest",
    "voice",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
