package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TranslationService {

    private val cache = ConcurrentHashMap<String, String>()

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    /**
     * Translates text into target language code (e.g., "es" for Spanish, "en" for English).
     * If offline or fails, returns original text.
     */
    suspend fun translate(text: String, targetLang: String = "es"): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text

        val cacheKey = "${targetLang}_${text.hashCode()}"
        cache[cacheKey]?.let { return@withContext it }

        try {
            val encodedQuery = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext text
                val jsonArray = JSONArray(body)
                val sentencesArray = jsonArray.optJSONArray(0) ?: return@withContext text

                val sb = StringBuilder()
                for (i in 0 until sentencesArray.length()) {
                    val sentence = sentencesArray.optJSONArray(i)
                    val translatedChunk = sentence?.optString(0)
                    if (!translatedChunk.isNullOrEmpty()) {
                        sb.append(translatedChunk)
                    }
                }

                val result = sb.toString().trim()
                if (result.isNotEmpty()) {
                    cache[cacheKey] = result
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.w("TranslationService", "Translation failed, using original text: ${e.message}")
        }

        text
    }
}
