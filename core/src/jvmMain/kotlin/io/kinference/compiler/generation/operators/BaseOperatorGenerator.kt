package io.kinference.compiler.generation.operators

import io.kinference.compiler.generation.models.CodeBlockGenerator

abstract class BaseOperatorGenerator(protected val nameMapping: (String) -> String) : CodeBlockGenerator()
