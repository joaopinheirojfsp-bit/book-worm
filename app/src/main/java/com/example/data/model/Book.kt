package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReadingStatus(val label: String) {
    QUERO_LER("Quero Ler"),
    LENDO("Lendo"),
    LIDO("Lido"),
    ABANDONADO("Abandonado")
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val publisher: String = "",
    val publishYear: Int? = null,
    val isbn: String = "",
    val description: String = "",
    val pageCount: Int? = null,
    val coverUrl: String = "",
    val rating: Int = 0, // 0 to 5 stars
    val personalReview: String = "",
    val readingStatus: ReadingStatus = ReadingStatus.QUERO_LER,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateFinished: Long? = null,
    val genre: String = "",
    val tags: String = "" // comma-separated custom tags (e.g. "Ficção, Favorito, Emprestado")
) {
    fun getTagsList(): List<String> {
        return if (tags.isBlank()) emptyList()
        else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
