import numpy as np
from onnx import TensorProto
import onnx.numpy_helper
import onnxruntime as ort

mul_node = onnx.helper.make_node(
    "Mul",
    inputs=["x", "y"],
    outputs=["mul_result"],
    name="Mul1",
)

sum_node = onnx.helper.make_node(
    "Add",
    inputs=["mul_result", "add_model_param"],
    outputs=["add_result"],
    name="Add1"
)

matmul_node = onnx.helper.make_node(
    "MatMul",
    inputs=["add_result", "matmul_model_param"],
    outputs=["output"],
    name="MatMul1"
)

add_model_param = np.arange(start=0.0, stop=5.0, step=1.0, dtype=np.float32).reshape((1, 5))
matmul_model_param = np.random.rand(2, 5, 10).astype(dtype=np.float32)

inputs_vi = [onnx.helper.make_tensor_value_info("x", TensorProto.FLOAT, (5, 5)),
             onnx.helper.make_tensor_value_info("y", TensorProto.FLOAT, (1, 5))]

outputs_vi = [onnx.helper.make_tensor_value_info("output", TensorProto.FLOAT, (2, 5, 10))]

add_model_param_tensor = onnx.numpy_helper.from_array(add_model_param, "add_model_param")
matmul_model_param_tensor = onnx.numpy_helper.from_array(matmul_model_param, "matmul_model_param")

graph = onnx.helper.make_graph(
    nodes=[mul_node, sum_node, matmul_node],
    name="test_model",
    inputs=inputs_vi,
    outputs=outputs_vi,
    initializer=[add_model_param_tensor, matmul_model_param_tensor]
)

model = onnx.helper.make_model(
    graph=graph,
    opset_imports=[onnx.helper.make_opsetid("ai.onnx", 13)]
)

with open("model.onnx", "wb") as file:
    file.write(model.SerializeToString())

input_x = np.random.rand(5, 5).astype(dtype=np.float32)

input_y = np.random.rand(1, 5).astype(dtype=np.float32)

session = ort.InferenceSession("model.onnx")
outputs = session.run(input_feed={"x": input_x, "y": input_y}, output_names=["output"])


tensor_input_x = onnx.numpy_helper.from_array(input_x, "x")
with open("input_0.pb", "wb") as file:
    file.write(tensor_input_x.SerializeToString())

tensor_input_y = onnx.numpy_helper.from_array(input_y, "y")
with open("input_1.pb", "wb") as file:
    file.write(tensor_input_y.SerializeToString())


output_tensor = onnx.numpy_helper.from_array(outputs[0], "output")
with open("output_0.pb", "wb") as file:
    file.write(output_tensor.SerializeToString())

