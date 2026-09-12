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
fun AlgebraScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val linA by viewModel.linearA.collectAsState()
    val linB by viewModel.linearB.collectAsState()
    val linRes by viewModel.linearResult.collectAsState()

    val qA by viewModel.quadA.collectAsState()
    val qB by viewModel.quadB.collectAsState()
    val qC by viewModel.quadC.collectAsState()
    val qRes by viewModel.quadResult.collectAsState()

    val sA1 by viewModel.sys2A1.collectAsState()
    val sB1 by viewModel.sys2B1.collectAsState()
    val sC1 by viewModel.sys2C1.collectAsState()
    val sA2 by viewModel.sys2A2.collectAsState()
    val sB2 by viewModel.sys2B2.collectAsState()
    val sC2 by viewModel.sys2C2.collectAsState()
    val sysRes by viewModel.sys2Result.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ecuación Cuadrática
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Ecuación Cuadrática: ax² + bx + c = 0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qA,
                        onValueChange = { viewModel.quadA.value = it },
                        label = { Text("a") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qB,
                        onValueChange = { viewModel.quadB.value = it },
                        label = { Text("b") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qC,
                        onValueChange = { viewModel.quadC.value = it },
                        label = { Text("c") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.solveQuadraticEquation() },
                    modifier = Modifier.fillMaxWidth().testTag("btn_solve_quad")
                ) {
                    Text("Resolver Cuadrática")
                }

                if (qRes != null) {
                    val r = qRes!!
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Discriminante Δ = ${MathEvaluator.formatNumber(r.discriminant)} (${r.description})", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(r.root1, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(r.root2, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Vértice (h, k) = (${MathEvaluator.formatNumber(r.vertexX)}, ${MathEvaluator.formatNumber(r.vertexY)})", fontSize = 13.sp)
                            Text("Concavidad: ${if (r.isConcaveUp) "Hacia arriba ∪" else "Hacia abajo ∩"}", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 2. Sistema Lineal 2x2
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sistema de Ecuaciones 2x2 (Cramer)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Ecuación 1: a₁x + b₁y = c₁\nEcuación 2: a₂x + b₂y = c₂", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Eq 1
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = sA1, onValueChange = { viewModel.sys2A1.value = it }, label = { Text("a₁") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sB1, onValueChange = { viewModel.sys2B1.value = it }, label = { Text("b₁") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sC1, onValueChange = { viewModel.sys2C1.value = it }, label = { Text("c₁") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Eq 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = sA2, onValueChange = { viewModel.sys2A2.value = it }, label = { Text("a₂") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sB2, onValueChange = { viewModel.sys2B2.value = it }, label = { Text("b₂") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sC2, onValueChange = { viewModel.sys2C2.value = it }, label = { Text("c₂") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.solveSystem2x2() },
                    modifier = Modifier.fillMaxWidth().testTag("btn_solve_sys2")
                ) {
                    Text("Resolver Sistema 2x2")
                }

                if (sysRes != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sysRes ?: "",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 3. Ecuación Lineal ax + b = 0
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ecuación Lineal: ax + b = 0", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = linA, onValueChange = { viewModel.linearA.value = it }, label = { Text("a") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = linB, onValueChange = { viewModel.linearB.value = it }, label = { Text("b") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.solveLinearEquation() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Resolver Lineal")
                }
                if (linRes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = linRes ?: "", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                }
            }
        }
    }
}
