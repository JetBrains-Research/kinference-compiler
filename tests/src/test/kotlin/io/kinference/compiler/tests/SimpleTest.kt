package io.kinference.compiler.tests

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

data class TestInfo(
    val generatedModelName: String
)

fun getTestInfo(testDirectory: File): TestInfo =
    TestInfo(generatedModelName = testDirectory.resolve("model_class_name.txt").readText())

class SimpleTest {
    companion object {
        private val testData = File(this::class.java.getResource("/test_data")!!.file)

        @JvmStatic
        fun data(): List<Arguments> =
            testData.list()!!.map { testDirName ->
                Arguments.of(getTestInfo(File(testData, testDirName)))
            }
    }

    @Tag("correctness")
    @MethodSource("data")
    @ParameterizedTest
    fun modelCorrectness(testInfo: TestInfo) {
        assertDoesNotThrow {
            Class.forName(testInfo.generatedModelName)
        }
    }
}