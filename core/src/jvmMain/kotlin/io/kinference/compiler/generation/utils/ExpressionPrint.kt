package io.kinference.compiler.generation.utils

/* Simplifies arithmetic expression and prints it in convenient way (e. g. returns + 0 or * 1). */
sealed class ArithmeticExpression(val priority: Int) {
    abstract fun asString(): String
    abstract fun simplify(): ArithmeticExpression

    override fun toString(): String = simplify().asString()
}

class Literal(private val value: String) : ArithmeticExpression(priority = 0) {
    override fun asString(): String = value
    override fun simplify(): ArithmeticExpression = this
}
fun Any.toLiteral() = Literal(value = this.toString())

sealed class OperatorExpression(
    arguments: List<ArithmeticExpression>,
    private val operation: String,
    priority: Int
) : ArithmeticExpression(priority) {
    val arguments = arguments.map { it.simplify() }

    override fun asString(): String = arguments.joinToString(separator = " $operation ") { argument ->
        argument.asString().let { representation ->
            if (argument.priority < priority) {
                representation
            } else {
                "($representation)"
            }
        }
    }
}

class MulExpression(
    arguments: List<ArithmeticExpression>
) : OperatorExpression(arguments, "*", priority = 4) {
    constructor(vararg arguments: ArithmeticExpression) : this(arguments.toList())

    override fun simplify(): ArithmeticExpression {
        val filteredArguments = arguments.filter { it.asString() != "1" }
        return when {
            filteredArguments.isEmpty() -> Literal("1")
            filteredArguments.any { it.asString() == "0" } -> Literal("0")
            filteredArguments.size == 1 -> filteredArguments.first()
            else -> MulExpression(filteredArguments)
        }
    }
}
fun List<ArithmeticExpression>.toMulExpression() = MulExpression(this)
infix operator fun ArithmeticExpression.times(other: ArithmeticExpression) = MulExpression(this, other)

class AddExpression(
    arguments: List<ArithmeticExpression>
) : OperatorExpression(arguments, "+", priority = 5) {
    constructor(vararg arguments: ArithmeticExpression) : this(arguments.toList())

    override fun simplify(): ArithmeticExpression {
        val filteredArguments = arguments.filter { it.asString() != "0" }
        return when {
            filteredArguments.isEmpty() -> Literal("0")
            filteredArguments.size == 1 -> filteredArguments.first()
            else -> AddExpression(filteredArguments)
        }
    }
}
fun List<ArithmeticExpression>.toAddExpression() = AddExpression(this)
infix operator fun ArithmeticExpression.plus(other: ArithmeticExpression) = AddExpression(this, other)
