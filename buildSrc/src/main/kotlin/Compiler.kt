import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val testOptIns = listOf(
    // allow `ExperimentalCoroutinesApi` for `TestScope.currentTime`
    "kotlinx.coroutines.ExperimentalCoroutinesApi",

    "dev.kord.common.annotation.KordInternal",
    "dev.kord.common.annotation.KordPreview",
    "dev.kord.common.annotation.KordExperimental",
)

object Jvm {
    const val target = 8
}

fun KotlinCommonCompilerOptions.applyKordCompilerOptions() {
    allWarningsAsErrors = true
    progressiveMode = true
    optIn.add("kotlin.contracts.ExperimentalContracts")
}

fun KotlinSourceSet.applyKordOptIns() {
    languageSettings {
        if ("Test" in name) testOptIns.forEach(::optIn)
    }
}

fun Project.configureAtomicFU() {
    // https://github.com/Kotlin/kotlinx-atomicfu/issues/210
    configure<AtomicFUPluginExtension> {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        dependenciesVersion = libs.findVersion("kotlinx-atomicfu").get().requiredVersion
    }
}
