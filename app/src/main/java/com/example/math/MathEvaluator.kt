package com.example.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

/**
 * Robust mathematical expression parser and evaluator.
 * Supports numbers, constants (π, e, φ), operators (+, -, *, /, ^, %, !),
 * functions (sin, cos, tan, asin, acos, atan, sinh, cosh, tanh, sqrt, cbrt, ln, log, log2, abs, exp),
 * and variable substitution (e.g. 'x' for function evaluation).
 */
class MathEvaluator(
    private val isRadians: Boolean = true
) {

    sealed class EvaluationResult {
        data class Success(val value: Double, val formatted: String) : EvaluationResult()
        data class Error(val message: String) : EvaluationResult()
    }

    /**
     * Evaluates a mathematical expression string.
     * Optional [variableX] can be provided to evaluate f(x).
     */
    fun evaluate(expression: String, variableX: Double? = null): EvaluationResult {
        if (expression.isBlank()) return EvaluationResult.Error("Expresión vacía")
        return try {
            val sanitized = preprocess(expression, variableX)
            val tokens = tokenize(sanitized)
            val parser = Parser(tokens, isRadians)
            val result = parser.parse()
            if (result.isNaN()) {
                EvaluationResult.Error("Indeterminado")
            } else if (result.isInfinite()) {
                EvaluationResult.Error(if (result > 0) "Infinito (∞)" else "-Infinito (-∞)")
            } else {
                EvaluationResult.Success(result, formatNumber(result))
            }
        } catch (e: Exception) {
            EvaluationResult.Error(e.message ?: "Error de sintaxis")
        }
    }

    private fun preprocess(raw: String, variableX: Double?): String {
        var expr = raw
            .replace("×", "*")
            .replace("·", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "${Math.PI}")
            .replace("phi", "1.618033988749895")
            .replace("φ", "1.618033988749895")
            .replace("e^", "exp")
            .replace("√", "sqrt")

        if (variableX != null) {
            // Replace x as a standalone variable, taking care not to replace inside 'exp'
            expr = expr.replace(Regex("(?<![a-zA-Z])x(?![a-zA-Z])"), "($variableX)")
        }

        // Implicit multiplication: e.g. 2(3) -> 2*(3), (2)(3) -> (2)*(3), 2sin -> 2*sin
        expr = expr.replace(Regex("(\\d+)\\("), "$1*(")
        expr = expr.replace(Regex("\\)(\\d+)"), ")*$1")
        expr = expr.replace(Regex("\\)\\("), ")*(")
        expr = expr.replace(Regex("(\\d+)([a-zA-Z]+)"), "$1*$2")

        return expr
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val n = expr.length

        while (i < n) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "0123456789." -> {
                    val sb = StringBuilder()
                    while (i < n && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                        if ((expr[i] == 'E' || expr[i] == 'e') && i + 1 < n && (expr[i + 1] == '+' || expr[i + 1] == '-')) {
                            sb.append(expr[i])
                            i++
                            sb.append(expr[i])
                            i++
                        } else {
                            sb.append(expr[i])
                            i++
                        }
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < n && (expr[i].isLetter() || expr[i].isDigit())) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c in "+-*/^%!()" -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> i++
            }
        }
        return tokens
    }

    private class Parser(
        private val tokens: List<String>,
        private val isRadians: Boolean
    ) {
        private var pos = 0

        fun parse(): Double {
            if (tokens.isEmpty()) throw IllegalArgumentException("Entrada vacía")
            val result = parseExpression()
            if (pos < tokens.size) {
                throw IllegalArgumentException("Símbolo inesperado: ${tokens[pos]}")
            }
            return result
        }

        private fun parseExpression(): Double {
            var result = parseTerm()
            while (pos < tokens.size && (tokens[pos] == "+" || tokens[pos] == "-")) {
                val op = tokens[pos++]
                val nextTerm = parseTerm()
                result = if (op == "+") result + nextTerm else result - nextTerm
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (pos < tokens.size && (tokens[pos] == "*" || tokens[pos] == "/" || tokens[pos] == "%")) {
                val op = tokens[pos++]
                val nextFactor = parseFactor()
                result = when (op) {
                    "*" -> result * nextFactor
                    "/" -> {
                        if (nextFactor == 0.0) throw ArithmeticException("División por cero")
                        result / nextFactor
                    }
                    "%" -> result % nextFactor
                    else -> result
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            var result = parsePower()
            // Factorial handling: postfix '!'
            while (pos < tokens.size && tokens[pos] == "!") {
                pos++
                result = factorial(result)
            }
            return result
        }

        private fun parsePower(): Double {
            var result = parsePrimary()
            if (pos < tokens.size && tokens[pos] == "^") {
                pos++
                val exponent = parseFactor() // right-associative power
                result = result.pow(exponent)
            }
            return result
        }

        private fun parsePrimary(): Double {
            if (pos >= tokens.size) throw IllegalArgumentException("Expresión incompleta")
            val token = tokens[pos]

            // Unary operators
            if (token == "+") {
                pos++
                return parsePrimary()
            }
            if (token == "-") {
                pos++
                return -parsePrimary()
            }

            // Parentheses
            if (token == "(") {
                pos++
                val result = parseExpression()
                if (pos >= tokens.size || tokens[pos] != ")") {
                    throw IllegalArgumentException("Falta paréntesis de cierre ')'")
                }
                pos++ // Consume ')'
                return result
            }

            // Number
            token.toDoubleOrNull()?.let {
                pos++
                return it
            }

            // Constants
            if (token.equals("pi", ignoreCase = true)) {
                pos++
                return Math.PI
            }
            if (token.equals("e", ignoreCase = true)) {
                pos++
                return Math.E
            }

            // Functions
            if (isFunction(token)) {
                pos++
                var hasParen = false
                if (pos < tokens.size && tokens[pos] == "(") {
                    hasParen = true
                    pos++
                }
                val arg = parseExpression()
                if (hasParen) {
                    if (pos >= tokens.size || tokens[pos] != ")") {
                        throw IllegalArgumentException("Falta ')' para función $token")
                    }
                    pos++
                }
                return applyFunction(token.lowercase(), arg)
            }

            throw IllegalArgumentException("Token desconocido: $token")
        }

        private fun isFunction(name: String): Boolean {
            return name.lowercase() in setOf(
                "sin", "cos", "tan", "asin", "acos", "atan",
                "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
                "sqrt", "cbrt", "ln", "log", "log10", "log2",
                "exp", "abs", "round", "floor", "ceil", "rad", "deg"
            )
        }

        private fun applyFunction(name: String, rawArg: Double): Double {
            val angleArg = if (!isRadians && name in setOf("sin", "cos", "tan")) {
                Math.toRadians(rawArg)
            } else {
                rawArg
            }

            return when (name) {
                "sin" -> sin(angleArg)
                "cos" -> cos(angleArg)
                "tan" -> {
                    val cosVal = cos(angleArg)
                    if (abs(cosVal) < 1e-12) throw ArithmeticException("Tangente indefinida")
                    tan(angleArg)
                }
                "asin" -> {
                    if (rawArg < -1.0 || rawArg > 1.0) throw IllegalArgumentException("Dominio de asin es [-1, 1]")
                    val res = asin(rawArg)
                    if (!isRadians) Math.toDegrees(res) else res
                }
                "acos" -> {
                    if (rawArg < -1.0 || rawArg > 1.0) throw IllegalArgumentException("Dominio de acos es [-1, 1]")
                    val res = acos(rawArg)
                    if (!isRadians) Math.toDegrees(res) else res
                }
                "atan" -> {
                    val res = atan(rawArg)
                    if (!isRadians) Math.toDegrees(res) else res
                }
                "sinh" -> sinh(rawArg)
                "cosh" -> cosh(rawArg)
                "tanh" -> tanh(rawArg)
                "asinh" -> ln(rawArg + sqrt(rawArg * rawArg + 1.0))
                "acosh" -> {
                    if (rawArg < 1.0) throw IllegalArgumentException("Dominio de acosh es x ≥ 1")
                    ln(rawArg + sqrt(rawArg * rawArg - 1.0))
                }
                "atanh" -> {
                    if (abs(rawArg) >= 1.0) throw IllegalArgumentException("Dominio de atanh es (-1, 1)")
                    0.5 * ln((1.0 + rawArg) / (1.0 - rawArg))
                }
                "sqrt" -> {
                    if (rawArg < 0.0) throw IllegalArgumentException("Raíz cuadrada de número negativo")
                    sqrt(rawArg)
                }
                "cbrt" -> cbrt(rawArg)
                "ln" -> {
                    if (rawArg <= 0.0) throw IllegalArgumentException("Logaritmo natural de número ≤ 0")
                    ln(rawArg)
                }
                "log", "log10" -> {
                    if (rawArg <= 0.0) throw IllegalArgumentException("Logaritmo base 10 de número ≤ 0")
                    log10(rawArg)
                }
                "log2" -> {
                    if (rawArg <= 0.0) throw IllegalArgumentException("Logaritmo base 2 de número ≤ 0")
                    log2(rawArg)
                }
                "exp" -> exp(rawArg)
                "abs" -> abs(rawArg)
                "round" -> round(rawArg)
                "floor" -> floor(rawArg)
                "ceil" -> ceil(rawArg)
                "rad" -> Math.toRadians(rawArg)
                "deg" -> Math.toDegrees(rawArg)
                else -> throw IllegalArgumentException("Función no soportada: $name")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) {
                // Gamma function approximation for general numbers
                return gamma(n + 1.0)
            }
            if (n > 170) return Double.POSITIVE_INFINITY
            var res = 1.0
            val intN = n.toInt()
            for (i in 2..intN) {
                res *= i
            }
            return res
        }

        // Lanczos approximation for Gamma function
        private fun gamma(z: Double): Double {
            val p = doubleArrayOf(
                676.5203681218851,
                -1259.1392167224028,
                771.32342877765313,
                -176.61502916214059,
                12.507343278686905,
                -0.138571095836524,
                9.9843695780195716e-6,
                1.5056327351493116e-7
            )
            val g = 7.0
            if (z < 0.5) {
                return Math.PI / (sin(Math.PI * z) * gamma(1.0 - z))
            }
            var x = z - 1.0
            var a = 0.99999999999980993
            for (i in p.indices) {
                a += p[i] / (x + i + 1)
            }
            val t = x + g + 0.5
            return sqrt(2.0 * Math.PI) * t.pow(x + 0.5) * exp(-t) * a
        }
    }

    companion object {
        fun formatNumber(value: Double): String {
            if (value.isNaN()) return "NaN"
            if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

            // Integer check
            if (abs(value - round(value)) < 1e-12 && abs(value) < 1e14) {
                return String.format(Locale.US, "%.0f", value)
            }

            // Very large or very small -> scientific notation
            if (abs(value) >= 1e12 || (abs(value) > 0 && abs(value) < 1e-6)) {
                val symbols = DecimalFormatSymbols(Locale.US)
                val df = DecimalFormat("0.######E0", symbols)
                return df.format(value)
            }

            // Normal floating point: strip trailing zeroes
            val symbols = DecimalFormatSymbols(Locale.US)
            val df = DecimalFormat("#,##0.##########", symbols)
            return df.format(value)
        }
    }
}
