package dev.kord.ksp.inspection

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import dev.kord.ksp.classDeclaration
import dev.kord.ksp.getSymbolsWithAnnotation
import dev.kord.ksp.isClassifierReference
import kotlinx.serialization.Serializable

/** [SymbolProcessorProvider] for [OptionalDefaultInspectionProcessor]. */
class OptionalDefaultInspectionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        OptionalDefaultInspectionProcessor(environment.logger)
}

private val OPTIONAL_TYPES =
    listOf("Optional", "OptionalBoolean", "OptionalInt", "OptionalLong", "OptionalSnowflake")
        .map { "dev.kord.common.entity.optional.$it" }
        .toSet()

/**
 * [SymbolProcessor] that verifies that every primary constructor parameter with `Optional` type of a [Serializable]
 * class has a default value.
 */
private class OptionalDefaultInspectionProcessor(private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation<Serializable>()
            .filterIsInstance<KSClassDeclaration>()
            .forEach { it.verifySerializableClassPrimaryConstructor() }

        return emptyList() // we never have to defer any symbols
    }

    private fun KSClassDeclaration.verifySerializableClassPrimaryConstructor() {
        primaryConstructor?.parameters
            ?.filterNot { it.hasDefault }
            ?.filter {
                val type = it.type
                type.element?.isClassifierReference() != false
                    && type.resolve().classDeclaration?.qualifiedName?.asString() in OPTIONAL_TYPES
            }
            ?.forEach {
                logger.error("Missing default for parameter '${it.name?.asString()}'.", symbol = it)
            }
    }
}
