package com.example.math

import kotlin.math.*

/**
 * Algebra and Equation Solver Engine.
 * Solves Linear, Quadratic, Cubic equations and 2x2, 3x3 Linear Systems.
 */
object AlgebraEngine {

    data class QuadraticResult(
        val a: Double,
        val b: Double,
        val c: Double,
        val discriminant: Double,
        val root1: String,
        val root2: String,
        val vertexX: Double,
        val vertexY: Double,
        val isConcaveUp: Boolean,
        val description: String
    )

    data class CubicResult(
        val roots: List<String>,
        val nature: String
    )

    data class System2x2Result(
        val x: Double,
        val y: Double,
        val determinant: Double,
        val solutionType: String
    )

    data class System3x3Result(
        val x: Double,
        val y: Double,
        val z: Double,
        val determinant: Double,
        val solutionType: String
    )

    fun solveLinear(a: Double, b: Double): Result<String> {
        return runCatching {
            if (a == 0.0) {
                if (b == 0.0) "Identidad: Infinitas soluciones (0 = 0)"
                else "Incompatible: Sin solución ($b ≠ 0)"
            } else {
                val x = -b / a
                "x = ${MathEvaluator.formatNumber(x)}"
            }
        }
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): Result<QuadraticResult> {
        return runCatching {
            if (a == 0.0) throw IllegalArgumentException("El coeficiente 'a' no puede ser 0 en una ecuación cuadrática")

            val delta = b * b - 4.0 * a * c
            val h = -b / (2.0 * a)
            val k = c - (b * b) / (4.0 * a)
            val concaveUp = a > 0

            val (r1, r2, desc) = when {
                delta > 1e-12 -> {
                    val x1 = (-b + sqrt(delta)) / (2.0 * a)
                    val x2 = (-b - sqrt(delta)) / (2.0 * a)
                    Triple(
                        "x₁ = ${MathEvaluator.formatNumber(x1)}",
                        "x₂ = ${MathEvaluator.formatNumber(x2)}",
                        "Dos raíces reales y distintas (Δ > 0)"
                    )
                }
                abs(delta) <= 1e-12 -> {
                    val x = -b / (2.0 * a)
                    Triple(
                        "x₁ = x₂ = ${MathEvaluator.formatNumber(x)}",
                        "Raíz doble (multiplicidad 2)",
                        "Una raíz real repetida (Δ = 0)"
                    )
                }
                else -> {
                    val realPart = -b / (2.0 * a)
                    val imagPart = sqrt(-delta) / (2.0 * a)
                    Triple(
                        "x₁ = ${MathEvaluator.formatNumber(realPart)} + ${MathEvaluator.formatNumber(abs(imagPart))}i",
                        "x₂ = ${MathEvaluator.formatNumber(realPart)} - ${MathEvaluator.formatNumber(abs(imagPart))}i",
                        "Dos raíces complejas conjugadas (Δ < 0)"
                    )
                }
            }

            QuadraticResult(
                a = a,
                b = b,
                c = c,
                discriminant = delta,
                root1 = r1,
                root2 = r2,
                vertexX = h,
                vertexY = k,
                isConcaveUp = concaveUp,
                description = desc
            )
        }
    }

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): Result<CubicResult> {
        return runCatching {
            if (a == 0.0) throw IllegalArgumentException("'a' no puede ser 0 en una cúbica")

            // Normalized: x^3 + a1*x^2 + a2*x + a3 = 0
            val a1 = b / a
            val a2 = c / a
            val a3 = d / a

            // Depressed cubic: t^3 + p*t + q = 0, where x = t - a1/3
            val p = a2 - (a1 * a1) / 3.0
            val q = (2.0 * a1.pow(3)) / 27.0 - (a1 * a2) / 3.0 + a3
            val delta = (q / 2.0).pow(2) + (p / 3.0).pow(3)

            val roots = mutableListOf<String>()
            val nature: String

            if (delta > 1e-12) {
                // One real root, two complex
                val u = cbrt(-q / 2.0 + sqrt(delta))
                val v = cbrt(-q / 2.0 - sqrt(delta))
                val t1 = u + v
                val x1 = t1 - a1 / 3.0
                roots.add("x₁ = ${MathEvaluator.formatNumber(x1)}")

                val realPart = - (u + v) / 2.0 - a1 / 3.0
                val imagPart = (u - v) * sqrt(3.0) / 2.0
                roots.add("x₂ = ${MathEvaluator.formatNumber(realPart)} + ${MathEvaluator.formatNumber(abs(imagPart))}i")
                roots.add("x₃ = ${MathEvaluator.formatNumber(realPart)} - ${MathEvaluator.formatNumber(abs(imagPart))}i")
                nature = "1 raíz real y 2 raíces complejas conjugadas"
            } else if (abs(delta) <= 1e-12) {
                // All real, at least two equal
                val u = cbrt(-q / 2.0)
                val x1 = 2.0 * u - a1 / 3.0
                val x2 = -u - a1 / 3.0
                roots.add("x₁ = ${MathEvaluator.formatNumber(x1)}")
                roots.add("x₂ = x₃ = ${MathEvaluator.formatNumber(x2)}")
                nature = "3 raíces reales (raíz múltiple)"
            } else {
                // Three distinct real roots (casus irreducibilis)
                val r = sqrt(-p.pow(3) / 27.0)
                val phi = acos(-q / (2.0 * r))
                val m = 2.0 * cbrt(r)

                val x1 = m * cos(phi / 3.0) - a1 / 3.0
                val x2 = m * cos((phi + 2.0 * Math.PI) / 3.0) - a1 / 3.0
                val x3 = m * cos((phi + 4.0 * Math.PI) / 3.0) - a1 / 3.0

                roots.add("x₁ = ${MathEvaluator.formatNumber(x1)}")
                roots.add("x₂ = ${MathEvaluator.formatNumber(x2)}")
                roots.add("x₃ = ${MathEvaluator.formatNumber(x3)}")
                nature = "3 raíces reales y distintas"
            }

            CubicResult(roots, nature)
        }
    }

    fun solveSystem2x2(
        a1: Double, b1: Double, c1: Double,
        a2: Double, b2: Double, c2: Double
    ): Result<System2x2Result> {
        return runCatching {
            // a1*x + b1*y = c1
            // a2*x + b2*y = c2
            val det = a1 * b2 - a2 * b1
            val detX = c1 * b2 - c2 * b1
            val detY = a1 * c2 - a2 * c1

            if (abs(det) < 1e-12) {
                if (abs(detX) < 1e-12 && abs(detY) < 1e-12) {
                    throw IllegalStateException("Sistema Compatible Indeterminado (Infinitas soluciones)")
                } else {
                    throw IllegalStateException("Sistema Incompatible (Sin solución - Rectas paralelas)")
                }
            }

            val x = detX / det
            val y = detY / det

            System2x2Result(x, y, det, "Sistema Compatible Determinado (Solución única)")
        }
    }

    fun solveSystem3x3(
        row1: DoubleArray, // a1, b1, c1, d1 -> a1*x + b1*y + c1*z = d1
        row2: DoubleArray,
        row3: DoubleArray
    ): Result<System3x3Result> {
        return runCatching {
            fun det3(
                m00: Double, m01: Double, m02: Double,
                m10: Double, m11: Double, m12: Double,
                m20: Double, m21: Double, m22: Double
            ): Double {
                return m00 * (m11 * m22 - m12 * m21) -
                       m01 * (m10 * m22 - m12 * m20) +
                       m02 * (m10 * m21 - m11 * m20)
            }

            val detA = det3(
                row1[0], row1[1], row1[2],
                row2[0], row2[1], row2[2],
                row3[0], row3[1], row3[2]
            )

            if (abs(detA) < 1e-12) {
                throw IllegalStateException("Determinante principal es 0. El sistema no tiene solución única.")
            }

            val detX = det3(
                row1[3], row1[1], row1[2],
                row2[3], row2[1], row2[2],
                row3[3], row3[1], row3[2]
            )

            val detY = det3(
                row1[0], row1[3], row1[2],
                row2[0], row2[3], row2[2],
                row3[0], row3[3], row3[2]
            )

            val detZ = det3(
                row1[0], row1[1], row1[3],
                row2[0], row2[1], row2[3],
                row3[0], row3[1], row3[3]
            )

            val x = detX / detA
            val y = detY / detA
            val z = detZ / detA

            System3x3Result(x, y, z, detA, "Solución única encontrada (Regla de Cramer)")
        }
    }
}
