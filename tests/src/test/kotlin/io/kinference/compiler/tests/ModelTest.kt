package io.kinference.compiler.tests

import io.kinference.compiler.utils.TestData
import io.kinference.compiler.utils.getBlocks
import io.kinference.compiler.utils.getTestData
import io.kinference.data.tensors.Tensor
import io.kinference.data.tensors.asTensor
import io.kinference.ndarray.arrays.FloatNDArray
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class ModelTest {
    companion object {
        private val testData = File(this::class.java.getResource("/test_data")!!.file)

        @JvmStatic
        fun data(): List<Arguments> =
            testData.list()!!.flatMap { testSuitName ->
                val testSuitDirectory = File(testData, testSuitName)
                testSuitDirectory.list()!!.map { testSuitDirectory to it }
            }.flatMap { (testSuitDirectory, testName) ->
                val testDirectory = File(testSuitDirectory, testName)
                testDirectory.list { _, name -> name.startsWith("test_data_set_") }!!.map { datasetName ->
                    testDirectory to testDirectory.resolve(datasetName)
                }
            }.map { (testDirectory, dataset) ->
                Arguments.of(getTestData(testDirectory, dataset))
            }
    }

    @Tag("correctness")
    @MethodSource("data")
    @ParameterizedTest
    fun modelCorrectness(testData: TestData) {
        val expectedOutputs = testData.defaultModel.predict(testData.inputs).map { it as Tensor }.sortedBy { it.info.name }
        val actualOutputs = testData.generatedModel.predict(testData.inputs.associate { it.info.name to it.data }).map { (name, data) ->
            data.asTensor(name)
        }.sortedBy { it.info.name }

        assertTrue(expectedOutputs.indices.all { index -> expectedOutputs[index].info.name == actualOutputs[index].info.name })
        assertTrue(expectedOutputs.indices.all { index -> expectedOutputs[index].data.shape.contentEquals(actualOutputs[index].data.shape) })
        assertTrue(expectedOutputs.indices.all { index ->
            val expected = expectedOutputs[index].data
            val actual = actualOutputs[index].data
            expected.getBlocks().contentDeepEquals(actual.getBlocks())
        })
    }
}
