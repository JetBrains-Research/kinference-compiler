package io.kinference.compiler.generation.models

abstract class Generator<T> {
    protected abstract fun generateImpl()
    abstract fun generate(): T
}
