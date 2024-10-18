plugins {
    org.jetbrains.kotlin.jvm
}

private val extension = createKordExtension()

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        applyKordJvmCompilerOptions(extension)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = extension.jvmTargetInt
    }
}
