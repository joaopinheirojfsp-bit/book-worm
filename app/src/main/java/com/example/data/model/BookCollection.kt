package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class BookCollection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#1E3A5F",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "book_collection_cross_ref",
    primaryKeys = ["bookId", "collectionId"]
)
data class BookCollectionCrossRef(
    val bookId: Long,
    val collectionId: Long
)
