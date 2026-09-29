package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class PublicBookResult(
    val isbn: String = "",
    val title: String,
    val authors: String = "",
    val publisher: String = "",
    val publishYear: Int? = null,
    val description: String = "",
    val pageCount: Int? = null,
    val coverUrl: String = "",
    val source: String = "Biblioteca Pública"
)

class PublicLibraryService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun searchBooks(query: String): List<PublicBookResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<PublicBookResult>()

        // 1. Try Google Books API (covers vast public domain and modern library records)
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val url = "https://www.googleapis.com/books/v1/volumes?q=$encoded&maxResults=20"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val items = root.optJSONArray("items")
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val volumeInfo = item.optJSONObject("volumeInfo") ?: continue
                            
                            val title = volumeInfo.optString("title", "Sem título")
                            val authorsList = mutableListOf<String>()
                            val authorsArray = volumeInfo.optJSONArray("authors")
                            if (authorsArray != null) {
                                for (j in 0 until authorsArray.length()) {
                                    authorsList.add(authorsArray.getString(j))
                                }
                            }
                            val authors = authorsList.joinToString(", ")
                            val publisher = volumeInfo.optString("publisher", "")
                            val publishedDate = volumeInfo.optString("publishedDate", "")
                            val year = extractYear(publishedDate)
                            val description = volumeInfo.optString("description", "")
                            val pageCount = if (volumeInfo.has("pageCount")) volumeInfo.optInt("pageCount") else null

                            var isbn = ""
                            val industryIdentifiers = volumeInfo.optJSONArray("industryIdentifiers")
                            if (industryIdentifiers != null) {
                                for (k in 0 until industryIdentifiers.length()) {
                                    val idObj = industryIdentifiers.getJSONObject(k)
                                    val type = idObj.optString("type")
                                    val identifier = idObj.optString("identifier")
                                    if (type == "ISBN_13") {
                                        isbn = identifier
                                        break
                                    } else if (type == "ISBN_10" && isbn.isEmpty()) {
                                        isbn = identifier
                                    }
                                }
                            }

                            val imageLinks = volumeInfo.optJSONObject("imageLinks")
                            var coverUrl = imageLinks?.optString("thumbnail", "") ?: ""
                            if (coverUrl.startsWith("http://")) {
                                coverUrl = coverUrl.replaceFirst("http://", "https://")
                            }

                            results.add(
                                PublicBookResult(
                                    isbn = isbn,
                                    title = title,
                                    authors = authors,
                                    publisher = publisher,
                                    publishYear = year,
                                    description = description,
                                    pageCount = pageCount,
                                    coverUrl = coverUrl,
                                    source = "Google Books / Acervo Global"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Also search Open Library if results are few
        if (results.size < 5) {
            try {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val url = "https://openlibrary.org/search.json?q=$encoded&limit=15"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val root = JSONObject(body)
                        val docs = root.optJSONArray("docs")
                        if (docs != null) {
                            for (i in 0 until docs.length()) {
                                val doc = docs.getJSONObject(i)
                                val title = doc.optString("title", "Sem título")
                                val authorArray = doc.optJSONArray("author_name")
                                val authors = if (authorArray != null && authorArray.length() > 0) {
                                    val list = mutableListOf<String>()
                                    for (j in 0 until authorArray.length()) list.add(authorArray.getString(j))
                                    list.joinToString(", ")
                                } else ""

                                val publisherArray = doc.optJSONArray("publisher")
                                val publisher = if (publisherArray != null && publisherArray.length() > 0) {
                                    publisherArray.getString(0)
                                } else ""

                                val firstYear = if (doc.has("first_publish_year")) doc.optInt("first_publish_year") else null
                                val coverI = if (doc.has("cover_i")) doc.optLong("cover_i") else null
                                val coverUrl = if (coverI != null && coverI > 0) {
                                    "https://covers.openlibrary.org/b/id/$coverI-L.jpg"
                                } else ""

                                var isbn = ""
                                val isbnArray = doc.optJSONArray("isbn")
                                if (isbnArray != null && isbnArray.length() > 0) {
                                    isbn = isbnArray.getString(0)
                                }

                                results.add(
                                    PublicBookResult(
                                        isbn = isbn,
                                        title = title,
                                        authors = authors,
                                        publisher = publisher,
                                        publishYear = firstYear,
                                        description = "",
                                        pageCount = null,
                                        coverUrl = coverUrl,
                                        source = "Open Library"
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        results
    }

    suspend fun lookupByIsbn(isbn: String): PublicBookResult? = withContext(Dispatchers.IO) {
        val cleanIsbn = isbn.replace("-", "").replace(" ", "").trim()
        if (cleanIsbn.isEmpty()) return@withContext null

        // Try Open Library ISBN direct API first
        try {
            val url = "https://openlibrary.org/api/books?bibkeys=ISBN:$cleanIsbn&format=json&jscmd=data"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val key = "ISBN:$cleanIsbn"
                    if (root.has(key)) {
                        val bookObj = root.getJSONObject(key)
                        val title = bookObj.optString("title", "")
                        val authorsArray = bookObj.optJSONArray("authors")
                        val authors = if (authorsArray != null) {
                            val list = mutableListOf<String>()
                            for (j in 0 until authorsArray.length()) {
                                val aObj = authorsArray.getJSONObject(j)
                                list.add(aObj.optString("name", ""))
                            }
                            list.filter { it.isNotBlank() }.joinToString(", ")
                        } else ""

                        val publishersArray = bookObj.optJSONArray("publishers")
                        val publisher = if (publishersArray != null && publishersArray.length() > 0) {
                            publishersArray.getJSONObject(0).optString("name", "")
                        } else ""

                        val publishDate = bookObj.optString("publish_date", "")
                        val year = extractYear(publishDate)
                        val pageCount = if (bookObj.has("number_of_pages")) bookObj.optInt("number_of_pages") else null

                        val coverObj = bookObj.optJSONObject("cover")
                        var coverUrl = coverObj?.optString("large", "") ?: ""
                        if (coverUrl.isEmpty()) {
                            coverUrl = coverObj?.optString("medium", "") ?: ""
                        }
                        if (coverUrl.isEmpty()) {
                            coverUrl = "https://covers.openlibrary.org/b/isbn/$cleanIsbn-L.jpg"
                        }

                        if (title.isNotEmpty()) {
                            return@withContext PublicBookResult(
                                isbn = cleanIsbn,
                                title = title,
                                authors = authors,
                                publisher = publisher,
                                publishYear = year,
                                description = "",
                                pageCount = pageCount,
                                coverUrl = coverUrl,
                                source = "Open Library"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: Google Books ISBN lookup
        try {
            val url = "https://www.googleapis.com/books/v1/volumes?q=isbn:$cleanIsbn"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val items = root.optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val item = items.getJSONObject(0)
                        val volumeInfo = item.optJSONObject("volumeInfo")
                        if (volumeInfo != null) {
                            val title = volumeInfo.optString("title", "")
                            val authorsArray = volumeInfo.optJSONArray("authors")
                            val authors = if (authorsArray != null) {
                                val list = mutableListOf<String>()
                                for (j in 0 until authorsArray.length()) list.add(authorsArray.getString(j))
                                list.joinToString(", ")
                            } else ""
                            val publisher = volumeInfo.optString("publisher", "")
                            val publishedDate = volumeInfo.optString("publishedDate", "")
                            val year = extractYear(publishedDate)
                            val description = volumeInfo.optString("description", "")
                            val pageCount = if (volumeInfo.has("pageCount")) volumeInfo.optInt("pageCount") else null

                            val imageLinks = volumeInfo.optJSONObject("imageLinks")
                            var coverUrl = imageLinks?.optString("thumbnail", "") ?: ""
                            if (coverUrl.startsWith("http://")) {
                                coverUrl = coverUrl.replaceFirst("http://", "https://")
                            }

                            return@withContext PublicBookResult(
                                isbn = cleanIsbn,
                                title = title,
                                authors = authors,
                                publisher = publisher,
                                publishYear = year,
                                description = description,
                                pageCount = pageCount,
                                coverUrl = coverUrl,
                                source = "Google Books"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        null
    }

    private fun extractYear(dateStr: String): Int? {
        if (dateStr.isBlank()) return null
        val regex = Regex("\\b(1[89]\\d{2}|20\\d{2})\\b")
        val match = regex.find(dateStr)
        return match?.value?.toIntOrNull()
    }
}
