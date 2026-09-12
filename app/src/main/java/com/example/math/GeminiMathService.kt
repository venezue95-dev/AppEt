package com.example.math

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Universal Gemini AI Mathematical Engine.
 * Solves arbitrary mathematical problems: differential equations, symbolic integration,
 * Laplace transforms, linear algebra proofs, tensor analysis, and word problems.
 */
class GeminiMathService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    sealed class ResultState {
        data class Success(val solution: String) : ResultState()
        data class Error(val message: String) : ResultState()
    }

    suspend fun solveMathProblem(problemQuery: String): ResultState = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ResultState.Error(
                "La clave de API Gemini no está configurada o es el placeholder por defecto.\n" +
                "Configura tu GEMINI_API_KEY en el panel de Secrets de AI Studio para habilitar el Cálculo Universal IA."
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                Eres una Calculadora Matemática Universal de máxima precisión.
                El usuario te pedirá resolver cualquier cálculo matemático existente en el universo, incluyendo:
                - Ecuaciones diferenciales ordinarias y parciales (EDO, EDP).
                - Integrales simbólicas definidas e indefinidas.
                - Transformadas integrales (Laplace, Fourier, Z).
                - Álgebra abstracta, teoría de grupos, anillos y campos.
                - Análisis numérico, topología y cálculo tensorial.
                - Problemas aplicados de física teórica y matemática financiera.
                
                Estructura tu respuesta de forma clara y elegante en español:
                1. **Resultado Final**: El valor exacto o fórmula final destacada.
                2. **Procedimiento Paso a Paso**: Los pasos matemáticos clave para llegar a la solución.
                3. **Fórmulas / Propiedades**: Las identidades o teoremas utilizados.
                Usa formato markdown limpio y legible.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", problemQuery)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext ResultState.Error("Error de API (${response.code}): $responseBody")
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ResultState.Error("No se recibió respuesta del modelo matemático.")
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (text.isNotBlank()) {
                ResultState.Success(text)
            } else {
                ResultState.Error("Respuesta vacía recibida.")
            }
        } catch (e: Exception) {
            ResultState.Error("Error al procesar el cálculo: ${e.localizedMessage ?: e.message}")
        }
    }
}
