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
import com.example.ui.CalculatorViewModel

@Composable
fun CalculusScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expr by viewModel.calculusExpr.collectAsState()
    val x0 by viewModel.calculusX0.collectAsState()
    val intA by viewModel.calculusIntegralA.collectAsState()
    val intB by viewModel.calculusIntegralB.collectAsState()
    val limC by viewModel.calculusLimitC.collectAsState()
    val sumA by viewModel.calculusSeriesStart.collectAsState()
    val sumB by viewModel.calculusSeriesEnd.collectAsState()
    val result by viewModel.calculusResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shared Function Input f(x)
        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Función Matemática f(x)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expr,
                    onValueChange = { viewModel.calculusExpr.value = it },
                    label = { Text("Expresión f(x) e.g. x^3 - 2*x + 5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("calculus_input_expr")
                )
            }
        }

        // Calculation Operations Cards
        // 1. Derivada y Recta Tangente
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Derivada Numérica f'(x₀) y f''(x₀)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = x0,
                        onValueChange = { viewModel.calculusX0.value = it },
                        label = { Text("Punto x₀") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.computeDerivative() },
                        modifier = Modifier.testTag("btn_calc_derivative")
                    ) {
                        Text("Derivar")
                    }
                }
            }
        }

        // 2. Integral Definida
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Integral Definida ∫[a, b] f(x) dx", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = intA,
                        onValueChange = { viewModel.calculusIntegralA.value = it },
                        label = { Text("Límite a") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = intB,
                        onValueChange = { viewModel.calculusIntegralB.value = it },
                        label = { Text("Límite b") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.computeIntegral() },
                        modifier = Modifier.testTag("btn_calc_integral")
                    ) {
                        Text("Integrar")
                    }
                }
            }
        }

        // 3. Límite
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Límite lim(x → c) f(x)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = limC,
                        onValueChange = { viewModel.calculusLimitC.value = it },
                        label = { Text("Hacia c") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.computeLimit() },
                        modifier = Modifier.testTag("btn_calc_limit")
                    ) {
                        Text("Límite")
                    }
                }
            }
        }

        // 4. Sumatoria
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sumatoria de Series ∑(n = a a b) f(n)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = sumA,
                        onValueChange = { viewModel.calculusSeriesStart.value = it },
                        label = { Text("Inicio a") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sumB,
                        onValueChange = { viewModel.calculusSeriesEnd.value = it },
                        label = { Text("Fin b") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.computeSummation() },
                        modifier = Modifier.testTag("btn_calc_sum")
                    ) {
                        Text("Sumar")
                    }
                }
            }
        }

        // Result Card
        if (result != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resultado del Cálculo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
