package io.kinference.compiler.plugin.generation

import io.kinference.compiler.plugin.utils.log
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump

class ExampleIrGenerationExtension(
    private val messageCollector: MessageCollector? = null
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        messageCollector?.log("Module ${moduleFragment.name}:\n${moduleFragment.dump()}")
    }
}
