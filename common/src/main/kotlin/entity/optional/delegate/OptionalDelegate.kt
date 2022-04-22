package dev.kord.common.entity.optional.delegate

import dev.kord.common.entity.optional.Optional
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

public fun <V : Any> KMutableProperty0<Optional<V>>.delegate(): ReadWriteProperty<Any?, V?> =
    object : ReadWriteProperty<Any?, V?> {

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V?) {
            val optional = if (value == null) Optional.Missing()
            else Optional.Value(value)

            this@delegate.set(optional)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): V? {
            return when (val optional = this@delegate.get()) {
                is Optional.Value -> optional.value
                is Optional.Missing, is Optional.Null<*> -> null
            }
        }
    }

public fun <V : Any> KMutableProperty0<Optional<List<V>>>.delegateList(): ReadWriteProperty<Any?, List<V>> =
    object : ReadWriteProperty<Any?, List<V>> {

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<V>) {
            val optional = if (value.isEmpty()) Optional.Missing()
            else Optional.Value(value)

            this@delegateList.set(optional)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): List<V> {
            return when (val optional = this@delegateList.get()) {
                is Optional.Value -> optional.value
                is Optional.Missing, is Optional.Null<*> -> emptyList()
            }
        }
    }

@JvmName("provideNullableDelegate")
public fun <V : Any> KMutableProperty0<Optional<V?>>.delegate(): ReadWriteProperty<Any?, V?> =
    object : ReadWriteProperty<Any?, V?> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): V? {
            return this@delegate.get().value
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V?) {
            this@delegate.set(Optional(value))
        }

    }


public interface OptionalAndNullableDelegate<V : Any> : ReadWriteProperty<Any?, V?> {
    public fun resetToDefault()
    public val optional: Optional<V?>
}

public class NullAsNullDefaultMissing<V : Any> : OptionalAndNullableDelegate<V> {

    private companion object {
        // unique reference to distinguish between missing and null
        private val MISSING = Any()
    }

    private var value: Any? /* V? | MISSING */ = MISSING

    override fun resetToDefault() {
        value = MISSING
    }

    override val optional: Optional<V?>
        get() {
            val v = value
            @Suppress("UNCHECKED_CAST")
            return when {
                v === MISSING -> Optional.Missing()
                v == null -> Optional.Null()
                else -> Optional.Value(v as V)
            }
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): V? {
        val v = value
        @Suppress("UNCHECKED_CAST")
        return if (v === MISSING) null else v as V?
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V?) {
        this.value = value
    }
}


public interface OptionalDelegate<V : Any> : OptionalAndNullableDelegate<V> {
    override val optional: Optional<V>
}

public class NullAsMissingDefaultMissing<V : Any> : OptionalDelegate<V> {

    private var value: V? = null

    override fun resetToDefault() {
        value = null
    }

    override val optional: Optional<V>
        get() = when (val v = value) {
            null -> Optional.Missing()
            else -> Optional.Value(v)
        }

    override fun getValue(thisRef: Any?, property: KProperty<*>): V? = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V?) {
        this.value = value
    }
}
