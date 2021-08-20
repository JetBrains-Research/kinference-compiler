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
@Warmup(iterations = 10)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 100)
@ExperimentalTime
open class ModelBenchmark {
    @Param(
        "complex_tests.comment_updater_test.0",
        //"complex_tests.simple_onnx_model_test.0",

        "add.test_add.0",
        "add.test_add_bcast.0",
        "add.test_add_scalar.0",
        "concat.test_concat_1d_axis_0.0",
        "concat.test_concat_1d_axis_negative_1.0",
        "concat.test_concat_2d_axis_0.0",
        "concat.test_concat_2d_axis_1.0",
        "concat.test_concat_2d_axis_negative_1.0",
        "concat.test_concat_2d_axis_negative_2.0",
        "concat.test_concat_3d_axis_0.0",
        "concat.test_concat_3d_axis_1.0",
        "concat.test_concat_3d_axis_2.0",
        "concat.test_concat_3d_axis_negative_1.0",
        "concat.test_concat_3d_axis_negative_2.0",
        "concat.test_concat_3d_axis_negative_3.0",
        "equal.test_equal.0",
        "equal.test_equal_bcast.0",
        "flatten.test_flatten_axis0.0",
        "flatten.test_flatten_axis1.0",
        "flatten.test_flatten_axis2.0",
        "flatten.test_flatten_axis3.0",
        "flatten.test_flatten_default_axis.0",
        "flatten.test_flatten_negative_axis1.0",
        "flatten.test_flatten_negative_axis2.0",
        "flatten.test_flatten_negative_axis3.0",
        "flatten.test_flatten_negative_axis4.0",
        "gather.test_gather_0.0",
        "gather.test_gather_1.0",
        "gather.test_gather_negative_indices.0",
        "greater.test_greater.0",
        "greater.test_greater_bcast.0",
        "gru.test_gru_defaults.0",
        "gru.test_gru_seq_length.0",
        "gru.test_gru_with_initial_bias.0",
        "log_softmax.test_logsoftmax_axis_0.0",
        "log_softmax.test_logsoftmax_axis_1.0",
        "log_softmax.test_logsoftmax_axis_2.0",
        "log_softmax.test_logsoftmax_default_axis.0",
        "log_softmax.test_logsoftmax_example_1.0",
        "log_softmax.test_logsoftmax_large_number.0",
        "log_softmax.test_logsoftmax_negative_axis.0",
        "matmul.test_matmul_2d.0",
        "matmul.test_matmul_3d.0",
        "matmul.test_matmul_4d.0",
        "mul.test_mul.0",
        "mul.test_mul_bcast.0",
        "mul.test_mul_example.0",
        "or.test_or2d.0",
        "or.test_or3d.0",
        "or.test_or4d.0",
        "or.test_or_bcast3v1d.0",
        "or.test_or_bcast3v2d.0",
        "or.test_or_bcast4v2d.0",
        "or.test_or_bcast4v3d.0",
        "or.test_or_bcast4v4d.0",
        "reshape.test_reshape_extended_dims.0",
        "reshape.test_reshape_negative_dim.0",
        "reshape.test_reshape_negative_extended_dims.0",
        "reshape.test_reshape_one_dim.0",
        "reshape.test_reshape_reduced_dims.0",
        "reshape.test_reshape_reordered_all_dims.0",
        "reshape.test_reshape_reordered_last_dims.0",
        "reshape.test_reshape_zero_and_negative_dim.0",
        "reshape.test_reshape_zero_dim.0",
        "split.test_split_equal_parts_1d.0",
        "split.test_split_equal_parts_2d.0",
        "split.test_split_equal_parts_default_axis.0",
        "split.test_split_variable_parts_1d.0",
        "split.test_split_variable_parts_2d.0",
        "split.test_split_variable_parts_default_axis.0",
        "split.test_split_zero_size_splits.0",
        "tanh.test_tanh.0",
        "tanh.test_tanh_example.0",
        "tanh.test_tanh_scalar.0",
        "transpose.test_transpose_all_permutations_0.0",
        "transpose.test_transpose_all_permutations_1.0",
        "transpose.test_transpose_all_permutations_2.0",
        "transpose.test_transpose_all_permutations_3.0",
        "transpose.test_transpose_all_permutations_4.0",
        "transpose.test_transpose_all_permutations_5.0",
        "transpose.test_transpose_default.0",
        "unsqueeze.test_unsqueeze_axis_0.0",
        "unsqueeze.test_unsqueeze_axis_1.0",
        "unsqueeze.test_unsqueeze_axis_2.0",
        "unsqueeze.test_unsqueeze_axis_3.0",
        "unsqueeze.test_unsqueeze_negative_axes.0",
        "unsqueeze.test_unsqueeze_three_axes.0",
        "unsqueeze.test_unsqueeze_two_axes.0",
        "unsqueeze.test_unsqueeze_unsorted_axes.0",
        "where.test_where_example.0",
        "where.test_where_long_example.0",
    )
    lateinit var path: String

    lateinit var testData: TestData

    @Setup(Level.Trial)
    fun setup() {
        val (mainPath, testName, datasetIndex) = path.split(".")
        val testDirectory = File(javaClass.getResource("/test_data/$mainPath/$testName")!!.path)
        val testDataset = testDirectory.resolve("test_data_set_$datasetIndex")
        testData = getTestData(testDirectory, testDataset)
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
