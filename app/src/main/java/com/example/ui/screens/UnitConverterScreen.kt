package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.UnitConverterEngine
import com.example.ui.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val catIdx by viewModel.converterCategoryIndex.collectAsState()
    val fromIdx by viewModel.converterFromIndex.collectAsState()
    val toIdx by viewModel.converterToIndex.collectAsState()
    val inputVal by viewModel.converterInputValue.collectAsState()
    val outputVal by viewModel.converterOutputValue.collectAsState()

    val currentCat = UnitConverterEngine.categories[catIdx]
    val categoriesScroll = rememberScrollState()
    val mainScroll = rememberScrollState()

    LaunchedEffect(catIdx, fromIdx, toIdx, inputVal) {
        viewModel.updateConversion()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(mainScroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoriesScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitConverterEngine.categories.forEachIndexed { index, cat ->
                FilterChip(
                    selected = catIdx == index,
                    onClick = {
                        viewModel.converterCategoryIndex.value = index
                        viewModel.converterFromIndex.value = 0
                        viewModel.converterToIndex.value = if (cat.units.size > 1) 1 else 0
                        viewModel.updateConversion()
                    },
                    label = { Text(cat.name) },
                    modifier = Modifier.testTag("cat_chip_${cat.name}")
                )
            }
        }

        // Conversion Card
        ElevatedCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // "DE" Input Block
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("De", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    var fromExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${currentCat.units[fromIdx].name} (${currentCat.units[fromIdx].symbol})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = fromExpanded,
                            onDismissRequest = { fromExpanded = false }
                        ) {
                            currentCat.units.forEachIndexed { index, u ->
                                DropdownMenuItem(
                                    text = { Text("${u.name} (${u.symbol})") },
                                    onClick = {
                                        viewModel.converterFromIndex.value = index
                                        fromExpanded = false
                                        viewModel.updateConversion()
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = {
                            viewModel.converterInputValue.value = it
                            viewModel.updateConversion()
                        },
                        label = { Text("Valor a convertir") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("converter_input")
                    )
                }

                // Swap Units Button
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FilledIconButton(
                        onClick = {
                            val temp = viewModel.converterFromIndex.value
                            viewModel.converterFromIndex.value = viewModel.converterToIndex.value
                            viewModel.converterToIndex.value = temp
                            viewModel.updateConversion()
                        },
                        modifier = Modifier.testTag("btn_swap_units")
                    ) {
                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Intercambiar")
                    }
                }

                // "A" Result Block
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("A", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    var toExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${currentCat.units[toIdx].name} (${currentCat.units[toIdx].symbol})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = toExpanded,
                            onDismissRequest = { toExpanded = false }
                        ) {
                            currentCat.units.forEachIndexed { index, u ->
                                DropdownMenuItem(
                                    text = { Text("${u.name} (${u.symbol})") },
                                    onClick = {
                                        viewModel.converterToIndex.value = index
                                        toExpanded = false
                                        viewModel.updateConversion()
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Resultado",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$outputVal ${currentCat.units[toIdx].symbol}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
