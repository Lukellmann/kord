import org.gradle.api.NamedDomainObjectSet
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val kordOptIns = listOf(
    "kotlin.contracts.ExperimentalContracts",

    "dev.kord.common.annotation.KordInternal",
    "dev.kord.common.annotation.KordPreview",
    "dev.kord.common.annotation.KordExperimental",
    "dev.kord.common.annotation.KordVoice",
)

internal fun KotlinCommonCompilerOptions.applyKordCommonCompilerOptions() {
    allWarningsAsErrors = true
    progressiveMode = true
    freeCompilerArgs.add("-Xexpect-actual-classes")
}

internal fun KotlinJvmCompilerOptions.applyKordJvmCompilerOptions(extension: KordExtension) {
    applyKordCommonCompilerOptions()
    val target = extension.jvmTarget
    jvmTarget = target
    freeCompilerArgs.add(target.map { "-Xjdk-release=${it.target}" })
}

internal fun NamedDomainObjectSet<KotlinSourceSet>.applyKordTestOptIns() {
    named { it.contains("test", ignoreCase = true) }.configureEach {
        // allow `ExperimentalCoroutinesApi` for `TestScope.currentTime`
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
