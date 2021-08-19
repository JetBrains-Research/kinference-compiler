package io.kinference.compiler.generation.models

/* Base class for kotlinpoet code generators. */
abstract class Generator<T> {
    protected abstract fun generateImpl()
    abstract fun generate(): T
}
