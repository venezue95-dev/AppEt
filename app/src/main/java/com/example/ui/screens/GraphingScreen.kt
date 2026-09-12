package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathEvaluator
import com.example.ui.CalculatorViewModel

@Composable
fun GraphingScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val fnStr by viewModel.graphFunction.collectAsState()
    val xMin by viewModel.graphXMin.collectAsState()
    val xMax by viewModel.graphXMax.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = fnStr,
                onValueChange = { viewModel.graphFunction.value = it },
                label = { Text("Función f(x)") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("graph_fn_input")
            )
            IconButton(onClick = { viewModel.zoomInGraph() }) {
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In")
            }
            IconButton(onClick = { viewModel.zoomOutGraph() }) {
                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }
            IconButton(onClick = { viewModel.resetGraph() }) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset")
            }
        }

        // Quick Function Suggestions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("sin(x)", "x^2 - 4", "cos(x)*x", "1/x", "exp(-x^2)").forEach { suggestion ->
                SuggestionChip(
                    onClick = { viewModel.graphFunction.value = suggestion },
                    label = { Text(suggestion, fontSize = 11.sp) }
                )
            }
        }

        // Graph Canvas
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            val evaluator = remember { MathEvaluator(isRadians = true) }

            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val w = size.width
                val h = size.height

                val xRange = xMax - xMin
                val yRange = xRange * (h / w)
                val yMin = -yRange / 2.0
                val yMax = yRange / 2.0

                fun toScreenX(x: Double): Float = ((x - xMin) / xRange * w).toFloat()
                fun toScreenY(y: Double): Float = (h - (y - yMin) / yRange * h).toFloat()

                // Draw Grid
                val gridColor = Color.Gray.copy(alpha = 0.2f)
                val axisColor = Color.LightGray.copy(alpha = 0.7f)

                // Grid lines (vertical and horizontal every 1 or 2 units depending on range)
                val step = when {
                    xRange < 10 -> 1.0
                    xRange < 30 -> 2.0
                    xRange < 100 -> 5.0
                    else -> 10.0
                }

                var gx = (kotlin.math.floor(xMin / step) * step)
                while (gx <= xMax) {
                    val sx = toScreenX(gx)
                    drawLine(gridColor, Offset(sx, 0f), Offset(sx, h), strokeWidth = 1f)
                    gx += step
                }

                var gy = (kotlin.math.floor(yMin / step) * step)
                while (gy <= yMax) {
                    val sy = toScreenY(gy)
                    drawLine(gridColor, Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                    gy += step
                }

                // Axes
                val originX = toScreenX(0.0)
                val originY = toScreenY(0.0)

                // Y-axis
                if (originX in 0f..w) {
                    drawLine(axisColor, Offset(originX, 0f), Offset(originX, h), strokeWidth = 2f)
                }
                // X-axis
                if (originY in 0f..h) {
                    drawLine(axisColor, Offset(0f, originY), Offset(w, originY), strokeWidth = 2f)
                }

                // Plot f(x)
                val path = Path()
                var isStarted = false
                val stepsCount = 300
                val dx = xRange / stepsCount

                for (i in 0..stepsCount) {
                    val curX = xMin + i * dx
                    val evalRes = evaluator.evaluate(fnStr, curX)
                    if (evalRes is MathEvaluator.EvaluationResult.Success && !evalRes.value.isNaN() && !evalRes.value.isInfinite()) {
                        val screenX = toScreenX(curX)
                        val screenY = toScreenY(evalRes.value)

                        if (screenY in -h..(2 * h)) {
                            if (!isStarted) {
                                path.moveTo(screenX, screenY)
                                isStarted = true
                            } else {
                                path.lineTo(screenX, screenY)
                            }
                        } else {
                            isStarted = false
                        }
                    } else {
                        isStarted = false
                    }
                }

                drawPath(path, color = Color(0xFF00E5FF), style = Stroke(width = 3.5f))
            }
        }
    }
}
