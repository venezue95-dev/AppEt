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
fun ComplexScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val re1 by viewModel.complexRe1.collectAsState()
    val im1 by viewModel.complexIm1.collectAsState()
    val re2 by viewModel.complexRe2.collectAsState()
    val im2 by viewModel.complexIm2.collectAsState()
    val result by viewModel.complexResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Complex Number z1
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Número Complejo z₁ = a + bi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = re1,
                        onValueChange = { viewModel.complexRe1.value = it },
                        label = { Text("Parte Real (a)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = im1,
                        onValueChange = { viewModel.complexIm1.value = it },
                        label = { Text("Parte Imag (b)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Complex Number z2
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Número Complejo z₂ = c + di", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = re2,
                        onValueChange = { viewModel.complexRe2.value = it },
                        label = { Text("Parte Real (c)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = im2,
                        onValueChange = { viewModel.complexIm2.value = it },
                        label = { Text("Parte Imag (d)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Operations
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.complexAdd() }, modifier = Modifier.weight(1f).testTag("btn_comp_add")) {
                    Text("z₁ + z₂")
                }
                Button(onClick = { viewModel.complexMultiply() }, modifier = Modifier.weight(1f).testTag("btn_comp_mul")) {
                    Text("z₁ × z₂")
                }
                Button(onClick = { viewModel.complexDivide() }, modifier = Modifier.weight(1f).testTag("btn_comp_div")) {
                    Text("z₁ / z₂")
                }
            }
            FilledTonalButton(
                onClick = { viewModel.complexInspectZ1() },
                modifier = Modifier.fillMaxWidth().testTag("btn_comp_inspect")
            ) {
                Text("Propiedades de z₁ (|z|, θ, z̄, Polar)")
            }
        }

        // Result Card
        if (result != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resultado Complejo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
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
