import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal fun Project.git(vararg command: String): Provider<String> {
    val output = providers.exec {
        commandLine("git", *command)
        workingDir = rootDir
    }
    return output.result.flatMap { result ->
        result.rethrowFailure().assertNormalExitValue()
        output.standardOutput.asText.map { it.trim() }
    }
}
