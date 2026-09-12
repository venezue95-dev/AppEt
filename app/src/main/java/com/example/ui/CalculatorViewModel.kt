package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CalculationEntity
import com.example.data.local.CalculationRepository
import com.example.data.local.CalculatorDatabase
import com.example.math.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CalculatorMode(val title: String, val iconName: String) {
    SCIENTIFIC("Científica", "Calculate"),
    CALCULUS("Cálculo", "Functions"),
    ALGEBRA("Álgebra", "SquareFoot"),
    MATRICES("Matrices", "GridOn"),
    COMPLEX("Complejos", "Superscript"),
    STATISTICS("Estadística", "BarChart"),
    PROGRAMMER("Programador", "Terminal"),
    GRAPHING("Gráficos", "ShowChart"),
    CONVERTER("Unidades", "SwapHoriz"),
    UNIVERSAL_AI("IA Universal", "AutoAwesome")
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository

    init {
        val db = CalculatorDatabase.getInstance(application)
        repository = CalculationRepository(db.calculationDao())
    }

    val historyFlow: StateFlow<List<CalculationEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Mode ---
    private val _currentMode = MutableStateFlow(CalculatorMode.SCIENTIFIC)
    val currentMode: StateFlow<CalculatorMode> = _currentMode.asStateFlow()

    fun setMode(mode: CalculatorMode) {
        _currentMode.value = mode
    }

    // --- Scientific Calculator State ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _livePreview = MutableStateFlow("")
    val livePreview: StateFlow<String> = _livePreview.asStateFlow()

    private val _lastResult = MutableStateFlow("")
    val lastResult: StateFlow<String> = _lastResult.asStateFlow()

    private val _isRadians = MutableStateFlow(false) // Default degrees for general users
    val isRadians: StateFlow<Boolean> = _isRadians.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleAngleUnit() {
        _isRadians.value = !_isRadians.value
        updateLivePreview(_expression.value)
    }

    fun appendInput(text: String) {
        _errorMessage.value = null
        val current = _expression.value
        _expression.value = current + text
        updateLivePreview(_expression.value)
    }

    fun backspace() {
        _errorMessage.value = null
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = current.dropLast(1)
            updateLivePreview(_expression.value)
        }
    }

    fun clearAll() {
        _expression.value = ""
        _livePreview.value = ""
        _lastResult.value = ""
        _errorMessage.value = null
    }

    private fun updateLivePreview(expr: String) {
        if (expr.isBlank()) {
            _livePreview.value = ""
            return
        }
        val evaluator = MathEvaluator(_isRadians.value)
        when (val res = evaluator.evaluate(expr)) {
            is MathEvaluator.EvaluationResult.Success -> {
                _livePreview.value = res.formatted
            }
            is MathEvaluator.EvaluationResult.Error -> {
                _livePreview.value = ""
            }
        }
    }

    fun calculateScientific() {
        val expr = _expression.value
        if (expr.isBlank()) return

        val evaluator = MathEvaluator(_isRadians.value)
        when (val res = evaluator.evaluate(expr)) {
            is MathEvaluator.EvaluationResult.Success -> {
                _lastResult.value = res.formatted
                _errorMessage.value = null
                viewModelScope.launch {
                    repository.saveCalculation(expr, res.formatted, "CIENTIFICA")
                }
            }
            is MathEvaluator.EvaluationResult.Error -> {
                _errorMessage.value = res.message
            }
        }
    }

    fun loadHistoryItem(item: CalculationEntity) {
        _expression.value = item.expression
        _lastResult.value = item.result
        updateLivePreview(item.expression)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Memory operations
    fun memoryAdd() {
        val value = _lastResult.value.toDoubleOrNull() ?: return
        _memoryValue.value += value
    }

    fun memorySubtract() {
        val value = _lastResult.value.toDoubleOrNull() ?: return
        _memoryValue.value -= value
    }

    fun memoryRecall() {
        appendInput(MathEvaluator.formatNumber(_memoryValue.value))
    }

    fun memoryClear() {
        _memoryValue.value = 0.0
    }

    // --- Calculus State ---
    val calculusExpr = MutableStateFlow("x^3 - 2*x + 5")
    val calculusX0 = MutableStateFlow("2")
    val calculusIntegralA = MutableStateFlow("0")
    val calculusIntegralB = MutableStateFlow("3")
    val calculusLimitC = MutableStateFlow("0")
    val calculusSeriesStart = MutableStateFlow("1")
    val calculusSeriesEnd = MutableStateFlow("10")
    val calculusResult = MutableStateFlow<String?>(null)

    fun computeDerivative() {
        val expr = calculusExpr.value
        val x0 = calculusX0.value.toDoubleOrNull() ?: 0.0
        val res = CalculusEngine.derivative(expr, x0, _isRadians.value)
        res.onSuccess {
            val text = "f(${it.x0}) = ${MathEvaluator.formatNumber(it.fAtX0)}\n" +
                    "f'(${it.x0}) = ${MathEvaluator.formatNumber(it.derivative)}\n" +
                    "f''(${it.x0}) = ${MathEvaluator.formatNumber(it.secondDerivative)}\n" +
                    "Recta Tangente: ${it.tangentEquation}"
            calculusResult.value = text
            viewModelScope.launch { repository.saveCalculation("d/dx ($expr) en x=$x0", it.tangentEquation, "CALCULO") }
        }.onFailure {
            calculusResult.value = "Error: ${it.message}"
        }
    }

    fun computeIntegral() {
        val expr = calculusExpr.value
        val a = calculusIntegralA.value.toDoubleOrNull() ?: 0.0
        val b = calculusIntegralB.value.toDoubleOrNull() ?: 1.0
        val res = CalculusEngine.definiteIntegral(expr, a, b, _isRadians.value)
        res.onSuccess {
            calculusResult.value = "∫[${it.lowerBound}, ${it.upperBound}] ($expr) dx = ${it.formatted}\nMétodo: ${it.method}"
            viewModelScope.launch { repository.saveCalculation("∫[$a, $b] ($expr) dx", it.formatted, "CALCULO") }
        }.onFailure {
            calculusResult.value = "Error: ${it.message}"
        }
    }

    fun computeLimit() {
        val expr = calculusExpr.value
        val c = calculusLimitC.value.toDoubleOrNull() ?: 0.0
        val res = CalculusEngine.limit(expr, c, _isRadians.value)
        res.onSuccess {
            calculusResult.value = "Límite cuando x -> $c de ($expr):\n${it.explanation}"
            viewModelScope.launch { repository.saveCalculation("lim(x->$c) ($expr)", it.explanation, "CALCULO") }
        }.onFailure {
            calculusResult.value = "Error: ${it.message}"
        }
    }

    fun computeSummation() {
        val expr = calculusExpr.value
        val a = calculusSeriesStart.value.toLongOrNull() ?: 1L
        val b = calculusSeriesEnd.value.toLongOrNull() ?: 10L
        val res = CalculusEngine.summation(expr, a, b, _isRadians.value)
        res.onSuccess {
            calculusResult.value = "∑ (n=$a a $b) [$expr] = ${it.formatted}"
            viewModelScope.launch { repository.saveCalculation("∑(n=$a..$b) [$expr]", it.formatted, "CALCULO") }
        }.onFailure {
            calculusResult.value = "Error: ${it.message}"
        }
    }

    // --- Algebra State ---
    val linearA = MutableStateFlow("2")
    val linearB = MutableStateFlow("-8")
    val linearResult = MutableStateFlow<String?>(null)

    val quadA = MutableStateFlow("1")
    val quadB = MutableStateFlow("-5")
    val quadC = MutableStateFlow("6")
    val quadResult = MutableStateFlow<AlgebraEngine.QuadraticResult?>(null)

    val sys2A1 = MutableStateFlow("2")
    val sys2B1 = MutableStateFlow("1")
    val sys2C1 = MutableStateFlow("8")
    val sys2A2 = MutableStateFlow("1")
    val sys2B2 = MutableStateFlow("-1")
    val sys2C2 = MutableStateFlow("1")
    val sys2Result = MutableStateFlow<String?>(null)

    fun solveLinearEquation() {
        val a = linearA.value.toDoubleOrNull() ?: 0.0
        val b = linearB.value.toDoubleOrNull() ?: 0.0
        val res = AlgebraEngine.solveLinear(a, b)
        res.onSuccess {
            linearResult.value = it
            viewModelScope.launch { repository.saveCalculation("${a}x + $b = 0", it, "ALGEBRA") }
        }.onFailure {
            linearResult.value = "Error: ${it.message}"
        }
    }

    fun solveQuadraticEquation() {
        val a = quadA.value.toDoubleOrNull() ?: 1.0
        val b = quadB.value.toDoubleOrNull() ?: 0.0
        val c = quadC.value.toDoubleOrNull() ?: 0.0
        val res = AlgebraEngine.solveQuadratic(a, b, c)
        res.onSuccess {
            quadResult.value = it
            viewModelScope.launch { repository.saveCalculation("${a}x² + ${b}x + $c = 0", "${it.root1}, ${it.root2}", "ALGEBRA") }
        }.onFailure {
            quadResult.value = null
        }
    }

    fun solveSystem2x2() {
        val a1 = sys2A1.value.toDoubleOrNull() ?: 0.0
        val b1 = sys2B1.value.toDoubleOrNull() ?: 0.0
        val c1 = sys2C1.value.toDoubleOrNull() ?: 0.0
        val a2 = sys2A2.value.toDoubleOrNull() ?: 0.0
        val b2 = sys2B2.value.toDoubleOrNull() ?: 0.0
        val c2 = sys2C2.value.toDoubleOrNull() ?: 0.0

        val res = AlgebraEngine.solveSystem2x2(a1, b1, c1, a2, b2, c2)
        res.onSuccess {
            val text = "x = ${MathEvaluator.formatNumber(it.x)}\ny = ${MathEvaluator.formatNumber(it.y)}\nDet = ${MathEvaluator.formatNumber(it.determinant)}\n${it.solutionType}"
            sys2Result.value = text
            viewModelScope.launch { repository.saveCalculation("Sistema 2x2", "x=${it.x}, y=${it.y}", "ALGEBRA") }
        }.onFailure {
            sys2Result.value = "Error: ${it.message}"
        }
    }

    // --- Matrices State ---
    val matrixDim = MutableStateFlow(2) // 2x2 or 3x3
    val matrixA = MutableStateFlow(MatrixEngine.Matrix(2, 2) { r, c -> if (r == c) 1.0 else 0.0 })
    val matrixB = MutableStateFlow(MatrixEngine.Matrix(2, 2) { r, c -> if (r == c) 2.0 else 1.0 })
    val matrixResult = MutableStateFlow<String?>(null)
    val matrixResultMatrix = MutableStateFlow<MatrixEngine.Matrix?>(null)

    fun setMatrixDimension(dim: Int) {
        matrixDim.value = dim
        matrixA.value = MatrixEngine.Matrix(dim, dim) { r, c -> if (r == c) 1.0 else 0.0 }
        matrixB.value = MatrixEngine.Matrix(dim, dim) { r, c -> if (r == c) 2.0 else 1.0 }
        matrixResult.value = null
        matrixResultMatrix.value = null
    }

    fun updateMatrixACell(r: Int, c: Int, value: Double) {
        val current = matrixA.value
        val next = MatrixEngine.Matrix(current.rows, current.cols) { i, j ->
            if (i == r && j == c) value else current[i, j]
        }
        matrixA.value = next
    }

    fun updateMatrixBCell(r: Int, c: Int, value: Double) {
        val current = matrixB.value
        val next = MatrixEngine.Matrix(current.rows, current.cols) { i, j ->
            if (i == r && j == c) value else current[i, j]
        }
        matrixB.value = next
    }

    fun matrixAdd() {
        val res = MatrixEngine.add(matrixA.value, matrixB.value)
        res.onSuccess {
            matrixResultMatrix.value = it
            matrixResult.value = "A + B calculada con éxito"
        }.onFailure { matrixResult.value = it.message }
    }

    fun matrixSubtract() {
        val res = MatrixEngine.subtract(matrixA.value, matrixB.value)
        res.onSuccess {
            matrixResultMatrix.value = it
            matrixResult.value = "A - B calculada con éxito"
        }.onFailure { matrixResult.value = it.message }
    }

    fun matrixMultiply() {
        val res = MatrixEngine.multiply(matrixA.value, matrixB.value)
        res.onSuccess {
            matrixResultMatrix.value = it
            matrixResult.value = "A × B calculada con éxito"
        }.onFailure { matrixResult.value = it.message }
    }

    fun matrixDetA() {
        val res = MatrixEngine.determinant(matrixA.value)
        res.onSuccess {
            matrixResultMatrix.value = null
            matrixResult.value = "det(A) = ${MathEvaluator.formatNumber(it)}"
            viewModelScope.launch { repository.saveCalculation("det(A)", MathEvaluator.formatNumber(it), "MATRICES") }
        }.onFailure { matrixResult.value = it.message }
    }

    fun matrixInvA() {
        val res = MatrixEngine.inverse(matrixA.value)
        res.onSuccess {
            matrixResultMatrix.value = it
            matrixResult.value = "A⁻¹ (Inversa de A) calculada"
        }.onFailure { matrixResult.value = it.message }
    }

    fun matrixTransposeA() {
        val tr = MatrixEngine.transpose(matrixA.value)
        matrixResultMatrix.value = tr
        matrixResult.value = "Aᵀ (Transpuesta de A)"
    }

    // --- Complex Numbers State ---
    val complexRe1 = MutableStateFlow("3")
    val complexIm1 = MutableStateFlow("4")
    val complexRe2 = MutableStateFlow("1")
    val complexIm2 = MutableStateFlow("-2")
    val complexResult = MutableStateFlow<String?>(null)

    fun complexAdd() {
        val z1 = ComplexEngine.Complex(complexRe1.value.toDoubleOrNull() ?: 0.0, complexIm1.value.toDoubleOrNull() ?: 0.0)
        val z2 = ComplexEngine.Complex(complexRe2.value.toDoubleOrNull() ?: 0.0, complexIm2.value.toDoubleOrNull() ?: 0.0)
        val res = z1 + z2
        complexResult.value = "z₁ + z₂ = ${res.toFormattedString()}\nForma Polar: ${res.toPolarString()}"
    }

    fun complexMultiply() {
        val z1 = ComplexEngine.Complex(complexRe1.value.toDoubleOrNull() ?: 0.0, complexIm1.value.toDoubleOrNull() ?: 0.0)
        val z2 = ComplexEngine.Complex(complexRe2.value.toDoubleOrNull() ?: 0.0, complexIm2.value.toDoubleOrNull() ?: 0.0)
        val res = z1 * z2
        complexResult.value = "z₁ × z₂ = ${res.toFormattedString()}\nForma Polar: ${res.toPolarString()}"
    }

    fun complexDivide() {
        runCatching {
            val z1 = ComplexEngine.Complex(complexRe1.value.toDoubleOrNull() ?: 0.0, complexIm1.value.toDoubleOrNull() ?: 0.0)
            val z2 = ComplexEngine.Complex(complexRe2.value.toDoubleOrNull() ?: 0.0, complexIm2.value.toDoubleOrNull() ?: 0.0)
            val res = z1 / z2
            complexResult.value = "z₁ / z₂ = ${res.toFormattedString()}\nForma Polar: ${res.toPolarString()}"
        }.onFailure { complexResult.value = "Error: ${it.message}" }
    }

    fun complexInspectZ1() {
        val z1 = ComplexEngine.Complex(complexRe1.value.toDoubleOrNull() ?: 0.0, complexIm1.value.toDoubleOrNull() ?: 0.0)
        val polar = z1.toPolarString()
        val conj = z1.conjugate.toFormattedString()
        complexResult.value = "z₁ = ${z1.toFormattedString()}\n|z₁| (Módulo) = ${MathEvaluator.formatNumber(z1.modulus)}\nθ (Fase) = ${MathEvaluator.formatNumber(z1.phaseDeg)}° (${MathEvaluator.formatNumber(z1.phaseRad)} rad)\nConjugado z̄₁ = $conj\nPolar: $polar"
    }

    // --- Statistics State ---
    val statsRawInput = MutableStateFlow("12, 15, 12, 19, 23, 29, 31, 35, 40")
    val statsResult = MutableStateFlow<StatisticsEngine.DatasetStats?>(null)
    val combiN = MutableStateFlow("10")
    val combiR = MutableStateFlow("3")
    val combiResult = MutableStateFlow<String?>(null)

    fun calculateDatasetStats() {
        val numbers = statsRawInput.value
            .split(",", " ", "\n", ";")
            .mapNotNull { it.trim().toDoubleOrNull() }

        val res = StatisticsEngine.calculateStats(numbers)
        res.onSuccess {
            statsResult.value = it
        }.onFailure {
            statsResult.value = null
        }
    }

    fun computeNPr() {
        val n = combiN.value.toLongOrNull() ?: 0L
        val r = combiR.value.toLongOrNull() ?: 0L
        val res = StatisticsEngine.permutations(n, r)
        res.onSuccess {
            combiResult.value = "nPr = P($n, $r) = $it"
        }.onFailure { combiResult.value = "Error: ${it.message}" }
    }

    fun computeNCr() {
        val n = combiN.value.toLongOrNull() ?: 0L
        val r = combiR.value.toLongOrNull() ?: 0L
        val res = StatisticsEngine.combinations(n, r)
        res.onSuccess {
            combiResult.value = "nCr = C($n, $r) = $it"
        }.onFailure { combiResult.value = "Error: ${it.message}" }
    }

    // --- Programmer State ---
    val programmerInput = MutableStateFlow("255")
    val programmerWordSize = MutableStateFlow(ProgrammerEngine.WordSize.QWORD)
    val programmerState = MutableStateFlow(ProgrammerEngine.ProgrammerState(255uL))

    fun setProgrammerWordSize(ws: ProgrammerEngine.WordSize) {
        programmerWordSize.value = ws
        val currentVal = programmerState.value.value
        programmerState.value = ProgrammerEngine.ProgrammerState(currentVal, ws)
    }

    fun setProgrammerValue(value: ULong) {
        programmerState.value = ProgrammerEngine.ProgrammerState(value, programmerWordSize.value)
    }

    fun setProgrammerFromHex(hex: String) {
        ProgrammerEngine.parseInput(hex, 16, programmerWordSize.value).onSuccess {
            setProgrammerValue(it)
        }
    }

    fun setProgrammerFromDec(dec: String) {
        ProgrammerEngine.parseInput(dec, 10, programmerWordSize.value).onSuccess {
            setProgrammerValue(it)
        }
    }

    fun setProgrammerFromBin(bin: String) {
        ProgrammerEngine.parseInput(bin, 2, programmerWordSize.value).onSuccess {
            setProgrammerValue(it)
        }
    }

    fun programmerNot() {
        val cur = programmerState.value.value
        val n = ProgrammerEngine.not(cur, programmerWordSize.value)
        setProgrammerValue(n)
    }

    // --- Unit Converter State ---
    val converterCategoryIndex = MutableStateFlow(0)
    val converterFromIndex = MutableStateFlow(0)
    val converterToIndex = MutableStateFlow(1)
    val converterInputValue = MutableStateFlow("1")
    val converterOutputValue = MutableStateFlow("")

    fun updateConversion() {
        val cat = UnitConverterEngine.categories[converterCategoryIndex.value]
        val fromUnit = cat.units.getOrNull(converterFromIndex.value) ?: return
        val toUnit = cat.units.getOrNull(converterToIndex.value) ?: return
        val input = converterInputValue.value.toDoubleOrNull() ?: 0.0

        val converted = UnitConverterEngine.convert(input, fromUnit, toUnit)
        converterOutputValue.value = MathEvaluator.formatNumber(converted)
    }

    // --- Graphing State ---
    val graphFunction = MutableStateFlow("sin(x) * x")
    val graphXMin = MutableStateFlow(-10.0)
    val graphXMax = MutableStateFlow(10.0)

    fun zoomInGraph() {
        val range = (graphXMax.value - graphXMin.value) * 0.7
        val mid = (graphXMax.value + graphXMin.value) / 2.0
        graphXMin.value = mid - range / 2.0
        graphXMax.value = mid + range / 2.0
    }

    fun zoomOutGraph() {
        val range = (graphXMax.value - graphXMin.value) * 1.4
        val mid = (graphXMax.value + graphXMin.value) / 2.0
        graphXMin.value = mid - range / 2.0
        graphXMax.value = mid + range / 2.0
    }

    fun resetGraph() {
        graphXMin.value = -10.0
        graphXMax.value = 10.0
    }

    // --- Universal AI Math State ---
    private val geminiService = GeminiMathService()
    val aiQuery = MutableStateFlow("")
    val aiIsLoading = MutableStateFlow(false)
    val aiResponse = MutableStateFlow<String?>(null)
    val aiErrorMessage = MutableStateFlow<String?>(null)

    fun solveWithAI(query: String = aiQuery.value) {
        if (query.isBlank()) return
        aiIsLoading.value = true
        aiErrorMessage.value = null
        aiResponse.value = null

        viewModelScope.launch {
            when (val res = geminiService.solveMathProblem(query)) {
                is GeminiMathService.ResultState.Success -> {
                    aiResponse.value = res.solution
                    repository.saveCalculation(query, res.solution.take(100), "IA")
                }
                is GeminiMathService.ResultState.Error -> {
                    aiErrorMessage.value = res.message
                }
            }
            aiIsLoading.value = false
        }
    }
}
