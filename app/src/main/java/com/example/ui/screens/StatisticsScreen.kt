package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathEvaluator
import com.example.ui.CalculatorViewModel

@Composable
fun StatisticsScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val rawInput by viewModel.statsRawInput.collectAsState()
    val statsResult by viewModel.statsResult.collectAsState()

    val cN by viewModel.combiN.collectAsState()
    val cR by viewModel.combiR.collectAsState()
    val combiResult by viewModel.combiResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Descriptive Statistics Card
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Estadística Descriptiva de Muestra / Población",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { viewModel.statsRawInput.value = it },
                    label = { Text("Valores separados por coma o espacio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.calculateDatasetStats() },
                    modifier = Modifier.fillMaxWidth().testTag("btn_calc_stats")
                ) {
                    Text("Calcular Estadísticas")
                }

                if (statsResult != null) {
                    val s = statsResult!!
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StatRow("Tamaño muestra (N)", "${s.count}")
                            StatRow("Suma total (∑x)", MathEvaluator.formatNumber(s.sum))
                            StatRow("Media aritmética (x̄ / μ)", MathEvaluator.formatNumber(s.mean))
                            StatRow("Mediana", MathEvaluator.formatNumber(s.median))
                            StatRow("Moda", if (s.mode.isEmpty()) "Sin moda única" else s.mode.joinToString { MathEvaluator.formatNumber(it) })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            StatRow("Desviación Estándar Muestral (s)", MathEvaluator.formatNumber(s.sampleStdDev))
                            StatRow("Desviación Estándar Poblacional (σ)", MathEvaluator.formatNumber(s.populationStdDev))
                            StatRow("Varianza Muestral (s²)", MathEvaluator.formatNumber(s.sampleVariance))
                            StatRow("Varianza Poblacional (σ²)", MathEvaluator.formatNumber(s.populationVariance))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            StatRow("Mínimo / Máximo", "${MathEvaluator.formatNumber(s.min)} / ${MathEvaluator.formatNumber(s.max)}")
                            StatRow("Rango", MathEvaluator.formatNumber(s.range))
                            StatRow("Cuartiles Q₁ / Q₃", "${MathEvaluator.formatNumber(s.q1)} / ${MathEvaluator.formatNumber(s.q3)}")
                            StatRow("Rango Intercuartílico (IQR)", MathEvaluator.formatNumber(s.iqr))
                        }
                    }
                }
            }
        }

        // Combinatorics Card
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Combinatoria y Probabilidad (nPr, nCr)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cN,
                        onValueChange = { viewModel.combiN.value = it },
                        label = { Text("Total (n)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cR,
                        onValueChange = { viewModel.combiR.value = it },
                        label = { Text("Tomados de (r)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.computeNPr() }, modifier = Modifier.weight(1f)) {
                        Text("Permutación (nPr)")
                    }
                    Button(onClick = { viewModel.computeNCr() }, modifier = Modifier.weight(1f)) {
                        Text("Combinación (nCr)")
                    }
                }

                if (combiResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = combiResult ?: "",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
