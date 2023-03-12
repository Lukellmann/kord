package dev.kord.generators

import dev.kord.generators.dsl.KordEnum
import dev.kord.generators.dsl.Packages
import dev.kord.generators.generation.generateFileSpec
import dev.kord.generators.projects.common.commonEntity
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.writer

fun main(vararg args: String) {
    require(args.size == 1) { "project root dir should be passed as the single argument" }
    val rootDir = Path(args.first())

    val packages = Packages {
        commonEntity()
    }

    for (p in packages.packages) {
        val packageName = p.name
        val components = packageName.split('.').drop(2) // drop "dev.kord."
        val project = components.first()
        for (t in p.thingsToGenerate) {
            val path =
                rootDir / project / "src" / "main" / "kotlin" / (components.drop(1).joinToString("/")) / t.fileName
            val fileSpec = when (t) {
                is KordEnum<*> -> t.generateFileSpec(packageName)
            }
            path.writer().use(fileSpec::writeTo)
        }
    }
}
