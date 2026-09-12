package com.example.math

import kotlin.math.*

/**
 * High-accuracy numerical calculus engine.
 * Computes derivatives, definite integrals, limits, summations, and products.
 */
object CalculusEngine {

    data class DerivativeResult(
        val x0: Double,
        val fAtX0: Double,
        val derivative: Double,
        val secondDerivative: Double,
        val tangentEquation: String
    )

    data class IntegralResult(
        val lowerBound: Double,
        val upperBound: Double,
        val value: Double,
        val formatted: String,
        val method: String = "Regla de Simpson Compuesta (N=1000)"
    )

    data class LimitResult(
        val targetC: Double,
        val leftLimit: Double?,
        val rightLimit: Double?,
        val overallLimit: Double?,
        val explanation: String
    )

    data class SeriesResult(
        val startN: Long,
        val endN: Long,
        val result: Double,
        val formatted: String
    )

    fun derivative(
        expression: String,
        x0: Double,
        isRadians: Boolean = true,
        h: Double = 1e-5
    ): Result<DerivativeResult> {
        return runCatching {
            val evaluator = MathEvaluator(isRadians)

            val f0Res = evaluator.evaluate(expression, x0)
            val f0 = (f0Res as? MathEvaluator.EvaluationResult.Success)?.value
                ?: throw IllegalArgumentException("No se pudo evaluar f($x0)")

            val fPlus = (evaluator.evaluate(expression, x0 + h) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: throw IllegalArgumentException("Error evaluando en x0 + h")
            val fMinus = (evaluator.evaluate(expression, x0 - h) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: throw IllegalArgumentException("Error evaluando en x0 - h")

            val fPlus2 = (evaluator.evaluate(expression, x0 + 2 * h) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: fPlus
            val fMinus2 = (evaluator.evaluate(expression, x0 - 2 * h) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: fMinus

            // 5-point central difference stencil for first derivative: (-f(x+2h) + 8f(x+h) - 8f(x-h) + f(x-2h)) / (12h)
            val d1 = (-fPlus2 + 8.0 * fPlus - 8.0 * fMinus + fMinus2) / (12.0 * h)

            // Second derivative: (f(x+h) - 2f(x) + f(x-h)) / h^2
            val d2 = (fPlus - 2.0 * f0 + fMinus) / (h * h)

            // Tangent line: y = m(x - x0) + y0 -> y = m*x + (y0 - m*x0)
            val b = f0 - d1 * x0
            val bSign = if (b >= 0) "+ " else "- "
            val tangentStr = "y = ${MathEvaluator.formatNumber(d1)}·x $bSign${MathEvaluator.formatNumber(abs(b))}"

            DerivativeResult(
                x0 = x0,
                fAtX0 = f0,
                derivative = d1,
                secondDerivative = d2,
                tangentEquation = tangentStr
            )
        }
    }

    fun definiteIntegral(
        expression: String,
        a: Double,
        b: Double,
        isRadians: Boolean = true,
        intervals: Int = 1000
    ): Result<IntegralResult> {
        return runCatching {
            if (a == b) {
                return@runCatching IntegralResult(a, b, 0.0, "0")
            }

            val evaluator = MathEvaluator(isRadians)
            val n = if (intervals % 2 != 0) intervals + 1 else intervals
            val h = (b - a) / n

            val faRes = (evaluator.evaluate(expression, a) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: throw IllegalArgumentException("f(a) no evaluable")
            val fbRes = (evaluator.evaluate(expression, b) as? MathEvaluator.EvaluationResult.Success)?.value
                ?: throw IllegalArgumentException("f(b) no evaluable")

            var sumOdd = 0.0
            var sumEven = 0.0

            for (i in 1 until n) {
                val x = a + i * h
                val fxRes = evaluator.evaluate(expression, x)
                val fx = (fxRes as? MathEvaluator.EvaluationResult.Success)?.value ?: 0.0
                if (i % 2 == 1) {
                    sumOdd += fx
                } else {
                    sumEven += fx
                }
            }

            val result = (h / 3.0) * (faRes + 4.0 * sumOdd + 2.0 * sumEven + fbRes)

            IntegralResult(
                lowerBound = a,
                upperBound = b,
                value = result,
                formatted = MathEvaluator.formatNumber(result)
            )
        }
    }

    fun limit(
        expression: String,
        c: Double,
        isRadians: Boolean = true
    ): Result<LimitResult> {
        return runCatching {
            val evaluator = MathEvaluator(isRadians)
            val deltas = doubleArrayOf(1e-3, 1e-5, 1e-7, 1e-9)

            var leftVal: Double? = null
            var rightVal: Double? = null

            for (d in deltas) {
                val left = evaluator.evaluate(expression, c - d)
                if (left is MathEvaluator.EvaluationResult.Success && !left.value.isNaN()) {
                    leftVal = left.value
                }
                val right = evaluator.evaluate(expression, c + d)
                if (right is MathEvaluator.EvaluationResult.Success && !right.value.isNaN()) {
                    rightVal = right.value
                }
            }

            val overall = if (leftVal != null && rightVal != null && abs(leftVal - rightVal) < 1e-3) {
                (leftVal + rightVal) / 2.0
            } else null

            val explanation = when {
                overall != null -> "El límite existe y es bilateralmente convergente a ${MathEvaluator.formatNumber(overall)}"
                leftVal != null && rightVal != null -> "Los límites laterales difieren (Salto): Izq = ${MathEvaluator.formatNumber(leftVal)}, Der = ${MathEvaluator.formatNumber(rightVal)}"
                else -> "Límite divergente o asintótico en x -> $c"
            }

            LimitResult(
                targetC = c,
                leftLimit = leftVal,
                rightLimit = rightVal,
                overallLimit = overall,
                explanation = explanation
            )
        }
    }

    fun summation(
        expression: String,
        startN: Long,
        endN: Long,
        isRadians: Boolean = true
    ): Result<SeriesResult> {
        return runCatching {
            if (endN < startN) throw IllegalArgumentException("El fin debe ser mayor o igual al inicio")
            if (endN - startN > 100000) throw IllegalArgumentException("Rango demasiado grande (máximo 100,000 términos)")

            val evaluator = MathEvaluator(isRadians)
            var total = 0.0

            for (n in startN..endN) {
                val eval = evaluator.evaluate(expression, n.toDouble())
                val v = (eval as? MathEvaluator.EvaluationResult.Success)?.value
                    ?: throw IllegalArgumentException("Error en término n = $n")
                total += v
            }

            SeriesResult(startN, endN, total, MathEvaluator.formatNumber(total))
        }
    }

    fun product(
        expression: String,
        startN: Long,
        endN: Long,
        isRadians: Boolean = true
    ): Result<SeriesResult> {
        return runCatching {
            if (endN < startN) throw IllegalArgumentException("El fin debe ser mayor o igual al inicio")
            if (endN - startN > 10000) throw IllegalArgumentException("Rango demasiado grande (máximo 10,000 factores)")

            val evaluator = MathEvaluator(isRadians)
            var total = 1.0

            for (n in startN..endN) {
                val eval = evaluator.evaluate(expression, n.toDouble())
                val v = (eval as? MathEvaluator.EvaluationResult.Success)?.value
                    ?: throw IllegalArgumentException("Error en término n = $n")
                total *= v
            }

            SeriesResult(startN, endN, total, MathEvaluator.formatNumber(total))
        }
    }
}
