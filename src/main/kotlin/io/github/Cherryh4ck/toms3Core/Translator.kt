package io.github.Cherryh4ck.toms3Core

import com.google.gson.JsonParser
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

// Unofficial free Google Translate endpoint (the same one Python's "googletrans" library scrapes).
// No API key, but it's not a supported API - it can break or start throttling us at any time.
object Translator {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun translate(text: String, targetLang: String, sourceLang: String = "en"): String? {
        if (text.isBlank()) return text

        val cacheKey = "$sourceLang:$targetLang:$text"
        cache[cacheKey]?.let { return it }

        return try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val uri = URI("https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encodedText")
            val connection = uri.toURL().openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.addRequestProperty("User-Agent", "Mozilla/5.0")

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                connection.disconnect()
                return null
            }

            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            connection.disconnect()

            val segments = JsonParser.parseString(body).asJsonArray[0].asJsonArray
            val translated = segments.joinToString("") { segment -> segment.asJsonArray[0].asString }

            cache[cacheKey] = translated
            translated
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
