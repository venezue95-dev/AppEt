package com.example.math

import java.math.BigInteger
import kotlin.math.*

/**
 * Statistics and Probability Engine.
 * Descriptive statistics for datasets, combinatorics (nPr, nCr), and Normal Distribution.
 */
object StatisticsEngine {

    data class DatasetStats(
        val count: Int,
        val sum: Double,
        val sumSquares: Double,
        val mean: Double,
        val median: Double,
        val mode: List<Double>,
        val min: Double,
        val max: Double,
        val range: Double,
        val sampleVariance: Double,
        val populationVariance: Double,
        val sampleStdDev: Double,
        val populationStdDev: Double,
        val q1: Double,
        val q3: Double,
        val iqr: Double
    )

    fun calculateStats(numbers: List<Double>): Result<DatasetStats> {
        return runCatching {
            if (numbers.isEmpty()) throw IllegalArgumentException("El conjunto de datos no puede estar vacío")
            val n = numbers.size
            val sorted = numbers.sorted()

            val sum = sorted.sum()
            val sumSq = sorted.sumOf { it * it }
            val mean = sum / n

            val median = if (n % 2 == 1) {
                sorted[n / 2]
            } else {
                (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
            }

            // Mode
            val freqMap = sorted.groupingBy { it }.eachCount()
            val maxFreq = freqMap.values.maxOrNull() ?: 1
            val mode = if (maxFreq > 1) {
                freqMap.filter { it.value == maxFreq }.keys.toList()
            } else emptyList()

            val min = sorted.first()
            val max = sorted.last()
            val range = max - min

            val popVar = sorted.sumOf { (it - mean).pow(2) } / n
            val sampVar = if (n > 1) sorted.sumOf { (it - mean).pow(2) } / (n - 1) else 0.0

            val popStd = sqrt(popVar)
            val sampStd = sqrt(sampVar)

            // Quartiles
            val q1 = percentile(sorted, 0.25)
            val q3 = percentile(sorted, 0.75)
            val iqr = q3 - q1

            DatasetStats(
                count = n,
                sum = sum,
                sumSquares = sumSq,
                mean = mean,
                median = median,
                mode = mode,
                min = min,
                max = max,
                range = range,
                sampleVariance = sampVar,
                populationVariance = popVar,
                sampleStdDev = sampStd,
                populationStdDev = popStd,
                q1 = q1,
                q3 = q3,
                iqr = iqr
            )
        }
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        val n = sorted.size
        if (n == 1) return sorted[0]
        val index = p * (n - 1)
        val lower = floor(index).toInt()
        val upper = ceil(index).toInt()
        val fraction = index - lower
        return sorted[lower] + fraction * (sorted[upper] - sorted[lower])
    }

    fun permutations(n: Long, r: Long): Result<BigInteger> {
        return runCatching {
            if (n < 0 || r < 0) throw IllegalArgumentException("n y r deben ser ≥ 0")
            if (r > n) throw IllegalArgumentException("r no puede ser mayor que n")

            var result = BigInteger.ONE
            for (i in (n - r + 1)..n) {
                result = result.multiply(BigInteger.valueOf(i))
            }
            result
        }
    }

    fun combinations(n: Long, r: Long): Result<BigInteger> {
        return runCatching {
            if (n < 0 || r < 0) throw IllegalArgumentException("n y r deben ser ≥ 0")
            if (r > n) throw IllegalArgumentException("r no puede ser mayor que n")

            val k = if (r > n - r) n - r else r
            var num = BigInteger.ONE
            var den = BigInteger.ONE

            for (i in 1..k) {
                num = num.multiply(BigInteger.valueOf(n - i + 1))
                den = den.multiply(BigInteger.valueOf(i))
            }
            num.divide(den)
        }
    }

    /**
     * Standard Normal cumulative distribution function P(Z <= z).
     */
    fun normalCdf(z: Double): Double {
        return 0.5 * (1.0 + erf(z / sqrt(2.0)))
    }

    private fun erf(x: Double): Double {
        // Abramowitz and Stegun formula 7.1.26
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911

        val sign = if (x < 0) -1.0 else 1.0
        val absX = abs(x)

        val t = 1.0 / (1.0 + p * absX)
        val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-absX * absX)

        return sign * y
    }
}
