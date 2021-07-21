package org.example

import io.kinference.data.tensors.Tensor
import io.kinference.model.Model
import io.kinference.onnx.TensorProto
import org.junit.jupiter.api.Test
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.results.format.ResultFormatType
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.TimeUnit

private fun path(name: String) = "/test_data_set/$name"

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 3)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 100)
open class BenchmarkGeneratedModel {
    private val model = GeneratedModel()

    private val inputs = List(2) {
        Tensor.create(
            TensorProto.ADAPTER.decode(javaClass.getResourceAsStream(path("input_$it.pb"))!!.readAllBytes())
        )
    }.associate { it.info.name to it.data }

    @Benchmark
    fun benchmark(blackhole: Blackhole) {
        val outputs = model.predict(inputs)
        blackhole.consume(outputs)
    }
}

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 0)
@Warmup(iterations = 3)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 100)
open class BenchmarkDefaultModel {
    private val model = Model.load(javaClass.getResourceAsStream(path("model.onnx"))!!.readAllBytes())

    private val inputs = List(2) {
        Tensor.create(
            TensorProto.ADAPTER.decode(javaClass.getResourceAsStream(path("input_$it.pb"))!!.readAllBytes())
        )
    }

    @Benchmark
    fun benchmark(blackhole: Blackhole) {
        val outputs = model.predict(inputs)
        blackhole.consume(outputs)
    }
}

class BenchmarkTest {
    @Test
    fun benchmark() {
        val opts = OptionsBuilder()
            .include("Benchmark*")
            .resultFormat(ResultFormatType.CSV)
            .result("benchmark_results.csv")
            .build()
        val results = Runner(opts).run()
    }
}