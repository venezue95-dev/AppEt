package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorMode
import com.example.ui.CalculatorViewModel
import com.example.ui.components.HistorySheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalculatorViewModel
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val historyList by viewModel.historyFlow.collectAsState()
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Calculadora Universal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("appbar_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Mode Tabs
                ScrollableTabRow(
                    selectedTabIndex = currentMode.ordinal,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().testTag("mode_tabs")
                ) {
                    CalculatorMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setMode(mode) },
                            text = {
                                Text(
                                    text = mode.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = getModeIcon(mode),
                                    contentDescription = mode.title,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.testTag("tab_${mode.name}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentMode, label = "ModeTransition") { mode ->
                when (mode) {
                    CalculatorMode.SCIENTIFIC -> ScientificCalculatorScreen(
                        viewModel = viewModel,
                        onOpenHistory = { showHistorySheet = true }
                    )
                    CalculatorMode.CALCULUS -> CalculusScreen(viewModel = viewModel)
                    CalculatorMode.ALGEBRA -> AlgebraScreen(viewModel = viewModel)
                    CalculatorMode.MATRICES -> MatrixScreen(viewModel = viewModel)
                    CalculatorMode.COMPLEX -> ComplexScreen(viewModel = viewModel)
                    CalculatorMode.STATISTICS -> StatisticsScreen(viewModel = viewModel)
                    CalculatorMode.PROGRAMMER -> ProgrammerScreen(viewModel = viewModel)
                    CalculatorMode.GRAPHING -> GraphingScreen(viewModel = viewModel)
                    CalculatorMode.CONVERTER -> UnitConverterScreen(viewModel = viewModel)
                    CalculatorMode.UNIVERSAL_AI -> UniversalAiScreen(viewModel = viewModel)
                }
            }

            if (showHistorySheet) {
                HistorySheet(
                    historyList = historyList,
                    onSelect = { item ->
                        viewModel.loadHistoryItem(item)
                    },
                    onClear = { viewModel.clearHistory() },
                    onDismiss = { showHistorySheet = false }
                )
            }
        }
    }
}

private fun getModeIcon(mode: CalculatorMode): ImageVector {
    return when (mode) {
        CalculatorMode.SCIENTIFIC -> Icons.Default.Calculate
        CalculatorMode.CALCULUS -> Icons.Default.Functions
        CalculatorMode.ALGEBRA -> Icons.Default.Architecture
        CalculatorMode.MATRICES -> Icons.Default.GridOn
        CalculatorMode.COMPLEX -> Icons.Default.Code
        CalculatorMode.STATISTICS -> Icons.Default.BarChart
        CalculatorMode.PROGRAMMER -> Icons.Default.Terminal
        CalculatorMode.GRAPHING -> Icons.AutoMirrored.Filled.ShowChart
        CalculatorMode.CONVERTER -> Icons.Default.SwapHoriz
        CalculatorMode.UNIVERSAL_AI -> Icons.Default.AutoAwesome
    }
}
