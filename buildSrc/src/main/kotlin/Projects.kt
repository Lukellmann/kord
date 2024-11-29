import org.gradle.api.Project
import org.gradle.api.provider.Provider

object Library {
    const val name = "kord"
    internal const val group = "dev.kord"
    internal const val description = "Idiomatic Kotlin Wrapper for The Discord API"
    internal const val projectUrl = "https://github.com/kordlib/kord"
}

private val Project.tag: Provider<String>
    get() = git("tag", "--no-column", "--points-at", "HEAD")
        .map { tags -> tags.takeIf { it.isNotBlank() }?.lines()?.single() }

val Project.libraryVersion: Provider<String>
    get() = tag.orElse(git("branch", "--show-current").flatMap { branch ->
        val snapshotPrefix = when (branch) {
            "main" -> providers.gradleProperty("nextPlannedVersion")
            else -> providers.provider { branch.replace('/', '-') }
        }
        snapshotPrefix.map { "$it-SNAPSHOT" }
    })

val Project.commitHash get() = git("rev-parse", "--verify", "HEAD")
val Project.shortCommitHash get() = git("rev-parse", "--short", "HEAD")

internal val Project.isRelease get() = tag.map { true }.orElse(false)

internal object Repo {
    const val releasesUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
    const val snapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
}
