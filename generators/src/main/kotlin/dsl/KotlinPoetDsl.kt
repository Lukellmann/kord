package dev.kord.generators.dsl

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.MemberName.Companion.member
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.TYPE

// for scope control, see https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker
@DslMarker
@Retention(SOURCE)
@Target(TYPE)
annotation class KotlinPoetDsl

typealias FileSpecBuilder = (@KotlinPoetDsl FileSpec.Builder).() -> Unit
typealias TypeSpecBuilder = (@KotlinPoetDsl TypeSpec.Builder).() -> Unit
typealias AnnotationSpecBuilder = (@KotlinPoetDsl AnnotationSpec.Builder).() -> Unit
typealias FunSpecBuilder = (@KotlinPoetDsl FunSpec.Builder).() -> Unit
typealias PropertySpecBuilder = (@KotlinPoetDsl PropertySpec.Builder).() -> Unit
typealias CodeBlockBuilder = (@KotlinPoetDsl CodeBlock.Builder).() -> Unit


// miscellaneous

inline fun <reified E : Enum<E>> E.asMemberName() = E::class.member(name)

inline fun FileSpec(packageName: String, fileName: String, builder: FileSpecBuilder) =
    FileSpec.builder(packageName, fileName).apply(builder).build()


// extensions for `FileSpec.Builder`

@DelicateKotlinPoetApi("See 'AnnotationSpec.get'")
fun FileSpec.Builder.addAnnotation(annotation: Annotation, includeDefaultValues: Boolean = false) =
    addAnnotation(AnnotationSpec.get(annotation, includeDefaultValues))

inline fun FileSpec.Builder.addClass(className: ClassName, builder: TypeSpecBuilder) =
    addType(TypeSpec.classBuilder(className).apply(builder).build())


// extensions for `TypeSpec.Builder`

inline fun <reified A : Annotation> TypeSpec.Builder.addAnnotation(builder: AnnotationSpecBuilder) =
    addAnnotation(AnnotationSpec.builder(A::class).apply(builder).build())

@DelicateKotlinPoetApi("See 'AnnotationSpec.get'")
fun TypeSpec.Builder.addAnnotation(annotation: Annotation, includeDefaultValues: Boolean = false) =
    addAnnotation(AnnotationSpec.get(annotation, includeDefaultValues))

inline fun TypeSpec.Builder.addClass(name: String, builder: TypeSpecBuilder) =
    addType(TypeSpec.classBuilder(name).apply(builder).build())

inline fun TypeSpec.Builder.addCompanionObject(name: String? = null, builder: TypeSpecBuilder) =
    addType(TypeSpec.companionObjectBuilder(name).apply(builder).build())

inline fun TypeSpec.Builder.addFunction(name: String, builder: FunSpecBuilder) =
    addFunction(FunSpec.builder(name).apply(builder).build())

inline fun TypeSpec.Builder.addObject(name: String, builder: TypeSpecBuilder) =
    addType(TypeSpec.objectBuilder(name).apply(builder).build())

inline fun <reified T> TypeSpec.Builder.addProperty(
    name: String,
    vararg modifiers: KModifier,
    builder: PropertySpecBuilder,
) = addProperty(PropertySpec.builder(name, typeNameOf<T>(), *modifiers).apply(builder).build())

inline fun TypeSpec.Builder.addProperty(
    name: String,
    type: TypeName,
    vararg modifiers: KModifier,
    builder: PropertySpecBuilder,
) = addProperty(PropertySpec.builder(name, type, *modifiers).apply(builder).build())

inline fun TypeSpec.Builder.primaryConstructor(builder: FunSpecBuilder) =
    primaryConstructor(FunSpec.constructorBuilder().apply(builder).build())


// extensions for `FunSpec.Builder`

@DelicateKotlinPoetApi("See 'AnnotationSpec.get'")
fun FunSpec.Builder.addAnnotation(annotation: Annotation, includeDefaultValues: Boolean = false) =
    addAnnotation(AnnotationSpec.get(annotation, includeDefaultValues))

inline fun <reified T> FunSpec.Builder.addParameter(name: String, vararg modifiers: KModifier) =
    addParameter(name, typeNameOf<T>(), *modifiers)

inline fun <reified T> FunSpec.Builder.returns() = returns(typeNameOf<T>())

inline fun FunSpec.Builder.withControlFlow(controlFlow: String, vararg args: Any, builder: FunSpecBuilder) =
    beginControlFlow(controlFlow, *args).apply(builder).endControlFlow()


// extensions for `PropertySpec.Builder`

@DelicateKotlinPoetApi("See 'AnnotationSpec.get'")
fun PropertySpec.Builder.addAnnotation(annotation: Annotation, includeDefaultValues: Boolean = false) =
    addAnnotation(AnnotationSpec.get(annotation, includeDefaultValues))

inline fun PropertySpec.Builder.delegate(builder: CodeBlockBuilder) =
    delegate(CodeBlock.builder().apply(builder).build())

inline fun PropertySpec.Builder.getter(builder: FunSpecBuilder) = getter(FunSpec.getterBuilder().apply(builder).build())


// extensions for `CodeBlock.Builder`

inline fun CodeBlock.Builder.withControlFlow(controlFlow: String, vararg args: Any?, builder: CodeBlockBuilder) =
    beginControlFlow(controlFlow, *args).apply(builder).endControlFlow()
