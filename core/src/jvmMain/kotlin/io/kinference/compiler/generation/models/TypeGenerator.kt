package io.kinference.compiler.generation.models

import com.squareup.kotlinpoet.TypeSpec

abstract class TypeGenerator : Generator<TypeSpec>() {
    protected lateinit var builder: TypeSpec.Builder

    abstract fun initBuilder()

    override fun generate(): TypeSpec {
        initBuilder()
        generateImpl()
        return builder.build()
    }
}
