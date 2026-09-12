package com.example.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.MathEvaluator
import com.example.math.MatrixEngine
import com.example.ui.CalculatorViewModel

@Composable
fun MatrixScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val dim by viewModel.matrixDim.collectAsState()
    val matA by viewModel.matrixA.collectAsState()
    val matB by viewModel.matrixB.collectAsState()
    val resultText by viewModel.matrixResult.collectAsState()
    val resultMatrix by viewModel.matrixResultMatrix.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dimension Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dimensión de Matrices", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = dim == 2,
                    onClick = { viewModel.setMatrixDimension(2) },
                    label = { Text("2 × 2") },
                    modifier = Modifier.testTag("dim_2x2")
                )
                FilterChip(
                    selected = dim == 3,
                    onClick = { viewModel.setMatrixDimension(3) },
                    label = { Text("3 × 3") },
                    modifier = Modifier.testTag("dim_3x3")
                )
            }
        }

        // Matrix A Input Card
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Matriz A", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                MatrixGridInput(
                    matrix = matA,
                    onCellChange = { r, c, v -> viewModel.updateMatrixACell(r, c, v) }
                )
            }
        }

        // Matrix B Input Card
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Matriz B", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                MatrixGridInput(
                    matrix = matB,
                    onCellChange = { r, c, v -> viewModel.updateMatrixBCell(r, c, v) }
                )
            }
        }

        // Operations Buttons Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.matrixAdd() }, modifier = Modifier.weight(1f).testTag("btn_mat_add")) {
                    Text("A + B")
                }
                Button(onClick = { viewModel.matrixSubtract() }, modifier = Modifier.weight(1f).testTag("btn_mat_sub")) {
                    Text("A - B")
                }
                Button(onClick = { viewModel.matrixMultiply() }, modifier = Modifier.weight(1f).testTag("btn_mat_mul")) {
                    Text("A × B")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = { viewModel.matrixDetA() }, modifier = Modifier.weight(1f).testTag("btn_mat_det")) {
                    Text("det(A)")
                }
                FilledTonalButton(onClick = { viewModel.matrixInvA() }, modifier = Modifier.weight(1f).testTag("btn_mat_inv")) {
                    Text("A⁻¹")
                }
                FilledTonalButton(onClick = { viewModel.matrixTransposeA() }, modifier = Modifier.weight(1f).testTag("btn_mat_trans")) {
                    Text("Aᵀ")
                }
            }
        }

        // Results Card
        if (resultText != null || resultMatrix != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (resultText != null) {
                        Text(
                            text = resultText ?: "",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }

                    if (resultMatrix != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MatrixView(matrix = resultMatrix!!)
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixGridInput(
    matrix: MatrixEngine.Matrix,
    onCellChange: (Int, Int, Double) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (r in 0 until matrix.rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (c in 0 until matrix.cols) {
                    var textValue by remember(matrix[r, c]) {
                        mutableStateOf(MathEvaluator.formatNumber(matrix[r, c]))
                    }
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onCellChange(r, c, parsed)
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MatrixView(matrix: MatrixEngine.Matrix) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (r in 0 until matrix.rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until matrix.cols) {
                        Text(
                            text = MathEvaluator.formatNumber(matrix[r, c]),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
