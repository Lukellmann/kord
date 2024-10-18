import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8

interface KordExtension {
    val jvmTarget: Property<JvmTarget>
}

internal fun Project.createKordExtension() = extensions
    .create<KordExtension>(name = "kord")
    .apply { jvmTarget.convention(JVM_1_8) }

internal val KordExtension.jvmTargetInt get() = jvmTarget.map { it.target.removePrefix("1.").toInt() }
