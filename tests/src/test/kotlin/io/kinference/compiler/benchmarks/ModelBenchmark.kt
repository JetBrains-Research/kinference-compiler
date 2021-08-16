package io.kinference.compiler.benchmarks

import io.kinference.compiler.utils.TestData
import io.kinference.compiler.utils.getTestData
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 30)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 200)
@ExperimentalTime
open class ModelBenchmark {
    @Param(
        "comment_updater_test.0",
        "simple_onnx_model_test.0",
    )
    lateinit var path: String

    lateinit var testData: TestData

    @Setup(Level.Trial)
    fun setup() {
        val (testPath, dataSetIndex) = path.split(".")
        val testDirectory = File(javaClass.getResource("/test_data/$testPath")!!.path)
        val testDataSet = testDirectory.resolve("test_data_set_$dataSetIndex")
        testData = getTestData(testDirectory, testDataSet)
    }

    @Benchmark
    fun benchmarkDefault(blackhole: Blackhole) {
        val outputs = testData.defaultModel.predict(testData.inputs)
        blackhole.consume(outputs)
    }

    @Benchmark
    fun benchmarkGenerated(blackhole: Blackhole) {
        val outputs = testData.generatedModel.predict(testData.inputs.associate { it.info.name to it.data })
        blackhole.consume(outputs)
    }

    @Tag("benchmark")
    @Test
    fun benchmarkGeneratedModelPerformance() {
        val opts = OptionsBuilder()
            .include("ModelBenchmark")
            .build()

        Runner(opts).run()
    }
}
