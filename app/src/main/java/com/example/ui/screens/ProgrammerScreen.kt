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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ProgrammerEngine
import com.example.ui.CalculatorViewModel

@Composable
fun ProgrammerScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val wordSize by viewModel.programmerWordSize.collectAsState()
    val state by viewModel.programmerState.collectAsState()
    val inputVal by viewModel.programmerInput.collectAsState()

    var selectedRadix by remember { mutableStateOf(10) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Word Size Picker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tamaño de Palabra", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ProgrammerEngine.WordSize.values().forEach { ws ->
                    FilterChip(
                        selected = wordSize == ws,
                        onClick = { viewModel.setProgrammerWordSize(ws) },
                        label = { Text(ws.name, fontSize = 12.sp) },
                        modifier = Modifier.testTag("ws_${ws.name}")
                    )
                }
            }
        }

        // Base Representations Card
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BaseDisplayRow("HEX", state.hexString, selectedRadix == 16) { selectedRadix = 16 }
                BaseDisplayRow("DEC", state.decString, selectedRadix == 10) { selectedRadix = 10 }
                BaseDisplayRow("OCT", state.octString, selectedRadix == 8) { selectedRadix = 8 }
                BaseDisplayRow("BIN", state.binString, selectedRadix == 2) { selectedRadix = 2 }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bits activos (Hamming):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${state.activeBitsCount} / ${wordSize.bits}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Direct Input
        OutlinedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Entrada en Base ${if (selectedRadix == 16) "Hexadecimal" else if (selectedRadix == 10) "Decimal" else if (selectedRadix == 8) "Octal" else "Binaria"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            viewModel.programmerInput.value = it
                            when (selectedRadix) {
                                16 -> viewModel.setProgrammerFromHex(it)
                                10 -> viewModel.setProgrammerFromDec(it)
                                2 -> viewModel.setProgrammerFromBin(it)
                                else -> viewModel.setProgrammerFromDec(it)
                            }
                        },
                        label = { Text("Valor") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.programmerNot() },
                        modifier = Modifier.align(Alignment.CenterVertically).testTag("btn_prog_not")
                    ) {
                        Text("NOT (~)")
                    }
                }
            }
        }

        // Bitwise quick ops
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Operaciones a Nivel de Bit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = {
                        val v = state.value
                        viewModel.setProgrammerValue(ProgrammerEngine.applyBitwise(v, 1uL, "LSH", wordSize))
                    }, modifier = Modifier.weight(1f)) { Text("<< 1") }
                    FilledTonalButton(onClick = {
                        val v = state.value
                        viewModel.setProgrammerValue(ProgrammerEngine.applyBitwise(v, 1uL, "RSH", wordSize))
                    }, modifier = Modifier.weight(1f)) { Text(">> 1") }
                }
            }
        }
    }
}

@Composable
fun BaseDisplayRow(
    base: String,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = base,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
