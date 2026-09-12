package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorViewModel

@Composable
fun UniversalAiScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.aiQuery.collectAsState()
    val isLoading by viewModel.aiIsLoading.collectAsState()
    val response by viewModel.aiResponse.collectAsState()
    val errorMessage by viewModel.aiErrorMessage.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val suggestions = listOf(
        "Resolver EDO: y'' + 4y = sin(x)",
        "Integral simbólica: ∫ x²·ln(x) dx",
        "Transformada de Laplace: L{cos(3t)·e^(-2t)}",
        "Demostración: ¿Por qué √2 es irracional?",
        "Autovalores y autovectores de [[2, 1], [1, 2]]",
        "Solución exacta de x³ - 6x - 9 = 0"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Cálculo Universal IA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Capaz de resolver cualquier cálculo matemático existente en el universo con procedimiento paso a paso.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Query Input Card
        OutlinedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.aiQuery.value = it },
                    label = { Text("Escribe o pega cualquier problema matemático...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("ai_query_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.solveWithAI() },
                    enabled = !isLoading && query.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("btn_solve_ai")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculando solución paso a paso...")
                    } else {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calcular con IA")
                    }
                }
            }
        }

        // Quick Suggestions
        Text("Ejemplos de cálculos avanzados:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            suggestions.forEach { sug ->
                SuggestionChip(
                    onClick = {
                        viewModel.aiQuery.value = sug
                        viewModel.solveWithAI(sug)
                    },
                    label = { Text(sug, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Error message
        if (errorMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // AI Response Card
        if (response != null) {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solución Matemática Detallada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Solución Matemática", response))
                            Toast.makeText(context, "Solución copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copiar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = response ?: "",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
