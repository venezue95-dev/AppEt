package com.example

import com.example.math.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

  @Test
  fun testBasicAndScientificEvaluation() {
    val evaluatorRad = MathEvaluator(isRadians = true)
    val res1 = evaluatorRad.evaluate("2 + 3 * 4")
    assertTrue(res1 is MathEvaluator.EvaluationResult.Success)
    assertEquals(14.0, (res1 as MathEvaluator.EvaluationResult.Success).value, 1e-9)

    val res2 = evaluatorRad.evaluate("(10 - 2) / 4 + 2^3")
    assertTrue(res2 is MathEvaluator.EvaluationResult.Success)
    assertEquals(10.0, (res2 as MathEvaluator.EvaluationResult.Success).value, 1e-9)

    val res3 = evaluatorRad.evaluate("sqrt(16) + ln(e)")
    assertTrue(res3 is MathEvaluator.EvaluationResult.Success)
    assertEquals(5.0, (res3 as MathEvaluator.EvaluationResult.Success).value, 1e-9)

    val evaluatorDeg = MathEvaluator(isRadians = false)
    val resSin = evaluatorDeg.evaluate("sin(90)")
    assertTrue(resSin is MathEvaluator.EvaluationResult.Success)
    assertEquals(1.0, (resSin as MathEvaluator.EvaluationResult.Success).value, 1e-9)
  }

  @Test
  fun testCalculusEngine() {
    // d/dx (x^3) at x = 2 -> 3*(2^2) = 12
    val dRes = CalculusEngine.derivative("x^3", 2.0, isRadians = true)
    assertTrue(dRes.isSuccess)
    assertEquals(12.0, dRes.getOrThrow().derivative, 1e-2)

    // int[0, 2] (x) dx = [x^2/2] from 0 to 2 = 2.0
    val intRes = CalculusEngine.definiteIntegral("x", 0.0, 2.0, isRadians = true)
    assertTrue(intRes.isSuccess)
    assertEquals(2.0, intRes.getOrThrow().value, 1e-3)
  }

  @Test
  fun testAlgebraEngine() {
    // x^2 - 5x + 6 = 0 -> roots 2 and 3
    val quadRes = AlgebraEngine.solveQuadratic(1.0, -5.0, 6.0)
    assertTrue(quadRes.isSuccess)
    val q = quadRes.getOrThrow()
    assertEquals(1.0, q.discriminant, 1e-9)
    assertTrue(q.root1.contains("3") || q.root1.contains("2"))

    // 2x2 system
    // 2x + y = 8
    // x - y = 1 -> x=3, y=2
    val sysRes = AlgebraEngine.solveSystem2x2(2.0, 1.0, 8.0, 1.0, -1.0, 1.0)
    assertTrue(sysRes.isSuccess)
    val s = sysRes.getOrThrow()
    assertEquals(3.0, s.x, 1e-9)
    assertEquals(2.0, s.y, 1e-9)
  }

  @Test
  fun testMatrixEngine() {
    val m = MatrixEngine.Matrix(2, 2) { r, c ->
      if (r == 0 && c == 0) 1.0
      else if (r == 0 && c == 1) 2.0
      else if (r == 1 && c == 0) 3.0
      else 4.0
    }
    // det(m) = 1*4 - 2*3 = -2
    val det = MatrixEngine.determinant(m).getOrThrow()
    assertEquals(-2.0, det, 1e-9)
  }

  @Test
  fun testComplexEngine() {
    // (1 + 2i) * (3 + 4i) = 3 + 4i + 6i - 8 = -5 + 10i
    val z1 = ComplexEngine.Complex(1.0, 2.0)
    val z2 = ComplexEngine.Complex(3.0, 4.0)
    val prod = z1 * z2
    assertEquals(-5.0, prod.re, 1e-9)
    assertEquals(10.0, prod.im, 1e-9)
  }

  @Test
  fun testStatisticsEngine() {
    val stats = StatisticsEngine.calculateStats(listOf(2.0, 4.0, 6.0, 8.0, 10.0)).getOrThrow()
    assertEquals(6.0, stats.mean, 1e-9)
    assertEquals(6.0, stats.median, 1e-9)
    assertEquals(5, stats.count)

    // Combinatorics: 5C2 = 10, 5P2 = 20
    val comb = StatisticsEngine.combinations(5, 2).getOrThrow()
    val perm = StatisticsEngine.permutations(5, 2).getOrThrow()
    assertEquals(10L, comb.toLong())
    assertEquals(20L, perm.toLong())
  }

  @Test
  fun testProgrammerEngine() {
    val pState = ProgrammerEngine.ProgrammerState(255uL, ProgrammerEngine.WordSize.QWORD)
    assertEquals("FF", pState.hexString)
    assertEquals("255", pState.decString)
    assertEquals(8, pState.activeBitsCount)
  }
}
