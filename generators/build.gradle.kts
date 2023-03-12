plugins {
    `kord-internal-module`
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinx.serialization.json) // use types directly
}

tasks.register<JavaExec>("generate") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("dev.kord.generators.MainKt")
    args(rootDir)
}
