package com.example.math

import kotlin.math.*

/**
 * Complex Numbers Engine.
 * Arithmetic, polar/rectangular conversions, powers, roots, and properties.
 */
object ComplexEngine {

    data class Complex(val re: Double, val im: Double) {
        val modulus: Double get() = sqrt(re * re + im * im)
        val phaseRad: Double get() = atan2(im, re)
        val phaseDeg: Double get() = Math.toDegrees(phaseRad)
        val conjugate: Complex get() = Complex(re, -im)

        operator fun plus(other: Complex) = Complex(re + other.re, im + other.im)
        operator fun minus(other: Complex) = Complex(re - other.re, im - other.im)
        operator fun times(other: Complex) = Complex(
            re * other.re - im * other.im,
            re * other.im + im * other.re
        )
        operator fun div(other: Complex): Complex {
            val denom = other.re * other.re + other.im * other.im
            if (denom == 0.0) throw ArithmeticException("División por cero en números complejos")
            return Complex(
                (re * other.re + im * other.im) / denom,
                (im * other.re - re * other.im) / denom
            )
        }

        fun power(n: Double): Complex {
            val r = modulus.pow(n)
            val theta = phaseRad * n
            return Complex(r * cos(theta), r * sin(theta))
        }

        fun sqrtComplex(): Complex {
            val r = sqrt(modulus)
            val theta = phaseRad / 2.0
            return Complex(r * cos(theta), r * sin(theta))
        }

        fun toFormattedString(): String {
            val reStr = MathEvaluator.formatNumber(re)
            val imAbs = abs(im)
            val imStr = MathEvaluator.formatNumber(imAbs)

            return when {
                abs(im) < 1e-12 -> reStr
                abs(re) < 1e-12 -> if (im == 1.0) "i" else if (im == -1.0) "-i" else "${MathEvaluator.formatNumber(im)}i"
                im > 0 -> "$reStr + ${if (imAbs == 1.0) "" else imStr}i"
                else -> "$reStr - ${if (imAbs == 1.0) "" else imStr}i"
            }
        }

        fun toPolarString(): String {
            return "${MathEvaluator.formatNumber(modulus)} ∠ ${MathEvaluator.formatNumber(phaseDeg)}°"
        }
    }
}
