package io.kinference.compiler

import io.kinference.data.ONNXData

@Suppress("UNUSED")
open class CompileTimeModel {
    constructor()

    constructor(filePath: String)

    open fun predict(inputs: Collection<ONNXData>): List<ONNXData> =
        error("Should be replaced during compilation")
}
