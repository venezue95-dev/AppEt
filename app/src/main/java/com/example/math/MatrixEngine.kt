package com.example.math

import kotlin.math.abs

/**
 * Linear Algebra and Matrix Operations Engine.
 * Supports matrix arithmetic, determinant, inverse, transpose, trace, and rank.
 */
object MatrixEngine {

    data class Matrix(
        val rows: Int,
        val cols: Int,
        val data: Array<DoubleArray>
    ) {
        constructor(rows: Int, cols: Int, init: (Int, Int) -> Double = { _, _ -> 0.0 }) : this(
            rows, cols, Array(rows) { r -> DoubleArray(cols) { c -> init(r, c) } }
        )

        operator fun get(r: Int, c: Int): Double = data[r][c]
        operator fun set(r: Int, c: Int, v: Double) { data[r][c] = v }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Matrix) return false
            if (rows != other.rows || cols != other.cols) return false
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (abs(data[r][c] - other.data[r][c]) > 1e-9) return false
                }
            }
            return true
        }

        override fun hashCode(): Int = data.contentDeepHashCode()
    }

    fun add(a: Matrix, b: Matrix): Result<Matrix> {
        return runCatching {
            if (a.rows != b.rows || a.cols != b.cols) {
                throw IllegalArgumentException("Las dimensiones deben ser iguales (${a.rows}x${a.cols} vs ${b.rows}x${b.cols})")
            }
            Matrix(a.rows, a.cols) { r, c -> a[r, c] + b[r, c] }
        }
    }

    fun subtract(a: Matrix, b: Matrix): Result<Matrix> {
        return runCatching {
            if (a.rows != b.rows || a.cols != b.cols) {
                throw IllegalArgumentException("Las dimensiones deben ser iguales (${a.rows}x${a.cols} vs ${b.rows}x${b.cols})")
            }
            Matrix(a.rows, a.cols) { r, c -> a[r, c] - b[r, c] }
        }
    }

    fun multiply(a: Matrix, b: Matrix): Result<Matrix> {
        return runCatching {
            if (a.cols != b.rows) {
                throw IllegalArgumentException("Columnas de A (${a.cols}) deben coincidir con Filas de B (${b.rows})")
            }
            val res = Matrix(a.rows, b.cols)
            for (i in 0 until a.rows) {
                for (j in 0 until b.cols) {
                    var sum = 0.0
                    for (k in 0 until a.cols) {
                        sum += a[i, k] * b[k, j]
                    }
                    res[i, j] = sum
                }
            }
            res
        }
    }

    fun scale(a: Matrix, scalar: Double): Matrix {
        return Matrix(a.rows, a.cols) { r, c -> a[r, c] * scalar }
    }

    fun transpose(a: Matrix): Matrix {
        return Matrix(a.cols, a.rows) { r, c -> a[c, r] }
    }

    fun trace(a: Matrix): Result<Double> {
        return runCatching {
            if (a.rows != a.cols) throw IllegalArgumentException("La matriz debe ser cuadrada")
            var tr = 0.0
            for (i in 0 until a.rows) tr += a[i, i]
            tr
        }
    }

    fun determinant(a: Matrix): Result<Double> {
        return runCatching {
            if (a.rows != a.cols) throw IllegalArgumentException("La matriz debe ser cuadrada para calcular el determinante")
            val n = a.rows
            if (n == 1) return@runCatching a[0, 0]
            if (n == 2) return@runCatching a[0, 0] * a[1, 1] - a[0, 1] * a[1, 0]
            if (n == 3) {
                return@runCatching a[0, 0] * (a[1, 1] * a[2, 2] - a[1, 2] * a[2, 1]) -
                                   a[0, 1] * (a[1, 0] * a[2, 2] - a[1, 2] * a[2, 0]) +
                                   a[0, 2] * (a[1, 0] * a[2, 1] - a[1, 1] * a[2, 0])
            }

            // Gaussian elimination with partial pivoting for NxN
            val m = Array(n) { r -> a.data[r].clone() }
            var det = 1.0
            var sign = 1.0

            for (i in 0 until n) {
                var pivot = i
                for (j in i + 1 until n) {
                    if (abs(m[j][i]) > abs(m[pivot][i])) pivot = j
                }
                if (abs(m[pivot][i]) < 1e-12) return@runCatching 0.0
                if (pivot != i) {
                    val temp = m[i]
                    m[i] = m[pivot]
                    m[pivot] = temp
                    sign = -sign
                }
                det *= m[i][i]
                for (j in i + 1 until n) {
                    val factor = m[j][i] / m[i][i]
                    for (k in i until n) {
                        m[j][k] -= factor * m[i][k]
                    }
                }
            }
            det * sign
        }
    }

    fun inverse(a: Matrix): Result<Matrix> {
        return runCatching {
            if (a.rows != a.cols) throw IllegalArgumentException("La matriz debe ser cuadrada para tener inversa")
            val n = a.rows
            val det = determinant(a).getOrThrow()
            if (abs(det) < 1e-12) throw IllegalStateException("Matriz singular (det = 0). No tiene inversa.")

            // Gauss-Jordan elimination on [A | I]
            val aug = Array(n) { r ->
                DoubleArray(2 * n) { c ->
                    if (c < n) a[r, c] else if (c - n == r) 1.0 else 0.0
                }
            }

            for (i in 0 until n) {
                var pivot = i
                for (j in i + 1 until n) {
                    if (abs(aug[j][i]) > abs(aug[pivot][i])) pivot = j
                }
                if (pivot != i) {
                    val temp = aug[i]
                    aug[i] = aug[pivot]
                    aug[pivot] = temp
                }

                val pivotVal = aug[i][i]
                for (c in 0 until 2 * n) {
                    aug[i][c] /= pivotVal
                }

                for (r in 0 until n) {
                    if (r != i) {
                        val factor = aug[r][i]
                        for (c in 0 until 2 * n) {
                            aug[r][c] -= factor * aug[i][c]
                        }
                    }
                }
            }

            Matrix(n, n) { r, c -> aug[r][c + n] }
        }
    }

    fun rank(a: Matrix): Int {
        val rows = a.rows
        val cols = a.cols
        val m = Array(rows) { r -> a.data[r].clone() }
        var rank = 0

        for (col in 0 until cols) {
            var pivot = rank
            while (pivot < rows && abs(m[pivot][col]) < 1e-12) pivot++
            if (pivot < rows) {
                val temp = m[rank]
                m[rank] = m[pivot]
                m[pivot] = temp

                val pivotVal = m[rank][col]
                for (c in col until cols) m[rank][c] /= pivotVal

                for (r in 0 until rows) {
                    if (r != rank) {
                        val factor = m[r][col]
                        for (c in col until cols) {
                            m[r][c] -= factor * m[rank][c]
                        }
                    }
                }
                rank++
            }
        }
        return rank
    }
}
