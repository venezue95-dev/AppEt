package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalcButton
import com.example.ui.components.DisplayPanel
import com.example.ui.components.KeyType

@Composable
fun ScientificCalculatorScreen(
    viewModel: CalculatorViewModel,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val livePreview by viewModel.livePreview.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRadians by viewModel.isRadians.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()

    var showAdvancedFunctions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Panel
        DisplayPanel(
            expression = expression,
            livePreview = livePreview,
            lastResult = lastResult,
            errorMessage = errorMessage,
            isRadians = isRadians,
            hasMemory = memoryValue != 0.0,
            onToggleAngle = { viewModel.toggleAngleUnit() },
            onOpenHistory = onOpenHistory
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Memory & Functions Toggle Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(
                    onClick = { viewModel.memoryClear() },
                    label = { Text("MC", fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )
                AssistChip(
                    onClick = { viewModel.memoryRecall() },
                    label = { Text("MR", fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )
                AssistChip(
                    onClick = { viewModel.memoryAdd() },
                    label = { Text("M+", fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )
                AssistChip(
                    onClick = { viewModel.memorySubtract() },
                    label = { Text("M-", fontSize = 12.sp) },
                    modifier = Modifier.height(32.dp)
                )
            }

            TextButton(
                onClick = { showAdvancedFunctions = !showAdvancedFunctions },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (showAdvancedFunctions) "Básica" else "Científica",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (showAdvancedFunctions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Advanced Scientific Panel
        AnimatedVisibility(
            visible = showAdvancedFunctions,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: sin, cos, tan, ln, log
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("sin", KeyType.FUNCTION, { viewModel.appendInput("sin(") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("cos", KeyType.FUNCTION, { viewModel.appendInput("cos(") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("tan", KeyType.FUNCTION, { viewModel.appendInput("tan(") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("ln", KeyType.FUNCTION, { viewModel.appendInput("ln(") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("log", KeyType.FUNCTION, { viewModel.appendInput("log(") }, Modifier.weight(1f), fontSize = 14)
                }
                // Row 2: asin, acos, atan, sinh, cosh
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("sin⁻¹", KeyType.FUNCTION, { viewModel.appendInput("asin(") }, Modifier.weight(1f), fontSize = 13)
                    CalcButton("cos⁻¹", KeyType.FUNCTION, { viewModel.appendInput("acos(") }, Modifier.weight(1f), fontSize = 13)
                    CalcButton("tan⁻¹", KeyType.FUNCTION, { viewModel.appendInput("atan(") }, Modifier.weight(1f), fontSize = 13)
                    CalcButton("sinh", KeyType.FUNCTION, { viewModel.appendInput("sinh(") }, Modifier.weight(1f), fontSize = 13)
                    CalcButton("cosh", KeyType.FUNCTION, { viewModel.appendInput("cosh(") }, Modifier.weight(1f), fontSize = 13)
                }
                // Row 3: e, π, φ, ^, √
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CalcButton("e", KeyType.FUNCTION, { viewModel.appendInput("e") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("π", KeyType.FUNCTION, { viewModel.appendInput("π") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("φ", KeyType.FUNCTION, { viewModel.appendInput("φ") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("xʸ", KeyType.FUNCTION, { viewModel.appendInput("^") }, Modifier.weight(1f), fontSize = 14)
                    CalcButton("√", KeyType.FUNCTION, { viewModel.appendInput("√(") }, Modifier.weight(1f), fontSize = 14)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Standard Keypad Grid (5 rows x 4 columns)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, (), %, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("AC", KeyType.CLEAR, { viewModel.clearAll() }, Modifier.weight(1f), fontSize = 18)
                CalcButton("()", KeyType.OPERATOR, {
                    // Smart parenthesis
                    val openCount = expression.count { it == '(' }
                    val closeCount = expression.count { it == ')' }
                    if (openCount > closeCount && expression.isNotEmpty() && (expression.last().isDigit() || expression.last() == ')')) {
                        viewModel.appendInput(")")
                    } else {
                        viewModel.appendInput("(")
                    }
                }, Modifier.weight(1f), fontSize = 18)
                CalcButton("%", KeyType.OPERATOR, { viewModel.appendInput("%") }, Modifier.weight(1f), fontSize = 18)
                CalcButton("÷", KeyType.OPERATOR, { viewModel.appendInput("÷") }, Modifier.weight(1f), fontSize = 22)
            }

            // Row 2: 7, 8, 9, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("7", KeyType.NUMBER, { viewModel.appendInput("7") }, Modifier.weight(1f))
                CalcButton("8", KeyType.NUMBER, { viewModel.appendInput("8") }, Modifier.weight(1f))
                CalcButton("9", KeyType.NUMBER, { viewModel.appendInput("9") }, Modifier.weight(1f))
                CalcButton("×", KeyType.OPERATOR, { viewModel.appendInput("×") }, Modifier.weight(1f), fontSize = 22)
            }

            // Row 3: 4, 5, 6, −
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("4", KeyType.NUMBER, { viewModel.appendInput("4") }, Modifier.weight(1f))
                CalcButton("5", KeyType.NUMBER, { viewModel.appendInput("5") }, Modifier.weight(1f))
                CalcButton("6", KeyType.NUMBER, { viewModel.appendInput("6") }, Modifier.weight(1f))
                CalcButton("−", KeyType.OPERATOR, { viewModel.appendInput("-") }, Modifier.weight(1f), fontSize = 22)
            }

            // Row 4: 1, 2, 3, +
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("1", KeyType.NUMBER, { viewModel.appendInput("1") }, Modifier.weight(1f))
                CalcButton("2", KeyType.NUMBER, { viewModel.appendInput("2") }, Modifier.weight(1f))
                CalcButton("3", KeyType.NUMBER, { viewModel.appendInput("3") }, Modifier.weight(1f))
                CalcButton("+", KeyType.OPERATOR, { viewModel.appendInput("+") }, Modifier.weight(1f), fontSize = 22)
            }

            // Row 5: ⌫, 0, ., =
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CalcButton("⌫", KeyType.OPERATOR, { viewModel.backspace() }, Modifier.weight(1f), fontSize = 18)
                CalcButton("0", KeyType.NUMBER, { viewModel.appendInput("0") }, Modifier.weight(1f))
                CalcButton(".", KeyType.NUMBER, { viewModel.appendInput(".") }, Modifier.weight(1f))
                CalcButton("=", KeyType.ACTION, { viewModel.calculateScientific() }, Modifier.weight(1f), fontSize = 24)
            }
        }
    }
}
