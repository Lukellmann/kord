package dev.kord.generators.dsl

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

@DslMarker
@Retention(SOURCE)
@Target(CLASS)
annotation class GenerationDsl

@GenerationDsl
sealed interface ThingToGenerate {
    val fileName: String
}

@GenerationDsl
sealed class KordEnum<T : Comparable<T>>(val name: String) : ThingToGenerate {
    override val fileName get() = "$name.kt"

    var kDoc: String? = null

    /** Url to the Discord Developer Documentation for the [KordEnum]. */
    var docUrl: String? = null

    /** Name of the [value][Entry.value] of the [KordEnum]. */
    var valueName = "value"

    // TODO remove eventually (for migration purposes)
    enum class ValuesPropertyType { SET }

    // TODO remove eventually (for migration purposes)
    var valuesPropertyName: String? = null
    var valuesPropertyType: ValuesPropertyType? = null
    var deprecatedSerializerName: String? = null

    @GenerationDsl
    class Entry<T : Comparable<T>>(val name: String, val value: T) {
        var kDoc: String? = null
        var deprecated: Deprecated? = null
        val isDeprecated get() = deprecated != null
    }

    val entries = mutableListOf<Entry<T>>()

    /** Adds an [Entry] to this [KordEnum]. */
    operator fun String.invoke(value: T, builder: Entry<T>.() -> Unit = {}) {
        entries.add(Entry(name = this, value).apply(builder))
    }
}

@GenerationDsl
class IntKordEnum(name: String) : KordEnum<Int>(name)

@GenerationDsl
class StringKordEnum(name: String) : KordEnum<String>(name)

@GenerationDsl
class Package(val name: String) {
    val thingsToGenerate = mutableListOf<ThingToGenerate>()
    infix fun String.intKordEnum(builder: IntKordEnum.() -> Unit) {
        thingsToGenerate.add(IntKordEnum(name = this).apply(builder))
    }

    infix fun String.stringKordEnum(builder: StringKordEnum.() -> Unit) {
        thingsToGenerate.add(StringKordEnum(name = this).apply(builder))
    }
}

@GenerationDsl
class Packages {
    val packages = mutableListOf<Package>()
    operator fun String.invoke(builder: Package.() -> Unit) {
        packages.add(Package(name = this).apply(builder))
    }
}

fun Packages(builder: Packages.() -> Unit) = Packages().apply(builder)
