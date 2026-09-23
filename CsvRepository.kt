package com.sandip.mymovie

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CsvRepository {

    private const val CSV_URL = "https://my-csv-api.sandip945235.workers.dev/"
    private val client = OkHttpClient()

    suspend fun fetchMovies(): List<MovieItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(CSV_URL).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()

        val lines = body.split("\n").filter { it.isNotBlank() }
        if (lines.size <= 1) return@withContext emptyList()

        // पहली लाइन हेडर है, उसे छोड़ दो
        lines.drop(1).mapNotNull { line ->
            val cols = parseCsvLine(line)
            if (cols.size < 5) return@mapNotNull null
            MovieItem(
                title = cols[0].trim(),
                poster = cols[1].trim(),
                category = cols[2].trim(),
                embedLink = cols[3].trim(),
                downloadLink = cols[4].trim()
            )
        }
    }

    // कॉमा वाली CSV लाइन को सही तरीके से तोड़ता है (quotes के अंदर के कॉमा को नज़रअंदाज़ करते हुए)
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString())
        return result
    }
}
