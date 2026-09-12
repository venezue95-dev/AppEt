package com.example.math

/**
 * Universal Unit Converter Engine.
 * Covers 10 distinct physical and engineering categories.
 */
object UnitConverterEngine {

    data class UnitCategory(
        val name: String,
        val units: List<ConversionUnit>
    )

    data class ConversionUnit(
        val name: String,
        val symbol: String,
        val factorToBase: Double, // valueInBase = value * factorToBase
        val isTemperature: Boolean = false
    )

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            name = "Longitud",
            units = listOf(
                ConversionUnit("Metro", "m", 1.0),
                ConversionUnit("Kilómetro", "km", 1000.0),
                ConversionUnit("Centímetro", "cm", 0.01),
                ConversionUnit("Milímetro", "mm", 0.001),
                ConversionUnit("Milla", "mi", 1609.344),
                ConversionUnit("Yarda", "yd", 0.9144),
                ConversionUnit("Pie", "ft", 0.3048),
                ConversionUnit("Pulgada", "in", 0.0254),
                ConversionUnit("Milla náutica", "NM", 1852.0),
                ConversionUnit("Año luz", "ly", 9.461e15)
            )
        ),
        UnitCategory(
            name = "Masa",
            units = listOf(
                ConversionUnit("Kilogramo", "kg", 1.0),
                ConversionUnit("Gramo", "g", 0.001),
                ConversionUnit("Miligramo", "mg", 0.000001),
                ConversionUnit("Libra", "lb", 0.45359237),
                ConversionUnit("Onza", "oz", 0.028349523125),
                ConversionUnit("Tonelada métrica", "t", 1000.0)
            )
        ),
        UnitCategory(
            name = "Temperatura",
            units = listOf(
                ConversionUnit("Celsius", "°C", 1.0, isTemperature = true),
                ConversionUnit("Fahrenheit", "°F", 1.0, isTemperature = true),
                ConversionUnit("Kelvin", "K", 1.0, isTemperature = true)
            )
        ),
        UnitCategory(
            name = "Área",
            units = listOf(
                ConversionUnit("Metro cuadrado", "m²", 1.0),
                ConversionUnit("Kilómetro cuadrado", "km²", 1e6),
                ConversionUnit("Centímetro cuadrado", "cm²", 1e-4),
                ConversionUnit("Hectárea", "ha", 10000.0),
                ConversionUnit("Acre", "ac", 4046.8564224),
                ConversionUnit("Pie cuadrado", "ft²", 0.092903)
            )
        ),
        UnitCategory(
            name = "Volumen",
            units = listOf(
                ConversionUnit("Litro", "L", 1.0),
                ConversionUnit("Mililitro", "mL", 0.001),
                ConversionUnit("Metro cúbico", "m³", 1000.0),
                ConversionUnit("Galón (US)", "gal", 3.78541),
                ConversionUnit("Pinta (US)", "pt", 0.473176),
                ConversionUnit("Taza", "cup", 0.236588)
            )
        ),
        UnitCategory(
            name = "Velocidad",
            units = listOf(
                ConversionUnit("Metro por segundo", "m/s", 1.0),
                ConversionUnit("Kilómetro por hora", "km/h", 0.277778),
                ConversionUnit("Milla por hora", "mph", 0.44704),
                ConversionUnit("Nudo", "kn", 0.514444),
                ConversionUnit("Mach", "M", 343.0)
            )
        ),
        UnitCategory(
            name = "Tiempo",
            units = listOf(
                ConversionUnit("Segundo", "s", 1.0),
                ConversionUnit("Minuto", "min", 60.0),
                ConversionUnit("Hora", "h", 3600.0),
                ConversionUnit("Día", "d", 86400.0),
                ConversionUnit("Semana", "sem", 604800.0),
                ConversionUnit("Año", "año", 31557600.0)
            )
        ),
        UnitCategory(
            name = "Presión",
            units = listOf(
                ConversionUnit("Pascal", "Pa", 1.0),
                ConversionUnit("Bar", "bar", 100000.0),
                ConversionUnit("Atmósfera", "atm", 101325.0),
                ConversionUnit("PSI", "psi", 6894.76),
                ConversionUnit("Torr (mmHg)", "mmHg", 133.322)
            )
        ),
        UnitCategory(
            name = "Energía",
            units = listOf(
                ConversionUnit("Joule", "J", 1.0),
                ConversionUnit("Kilojoule", "kJ", 1000.0),
                ConversionUnit("Caloría", "cal", 4.184),
                ConversionUnit("Kilocaloría", "kcal", 4184.0),
                ConversionUnit("Kilovatio-hora", "kWh", 3.6e6),
                ConversionUnit("Electrón-voltio", "eV", 1.602176634e-19)
            )
        ),
        UnitCategory(
            name = "Almacenamiento",
            units = listOf(
                ConversionUnit("Byte", "B", 1.0),
                ConversionUnit("Kilobyte", "KB", 1024.0),
                ConversionUnit("Megabyte", "MB", 1024.0 * 1024.0),
                ConversionUnit("Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
                ConversionUnit("Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
                ConversionUnit("Petabyte", "PB", 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0)
            )
        )
    )

    fun convert(
        value: Double,
        from: ConversionUnit,
        to: ConversionUnit
    ): Double {
        if (from.isTemperature || to.isTemperature) {
            // Convert to Celsius first
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            // Convert Celsius to destination
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> (celsius * 9.0 / 5.0) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        val baseValue = value * from.factorToBase
        return baseValue / to.factorToBase
    }
}
