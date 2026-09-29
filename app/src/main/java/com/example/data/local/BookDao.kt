package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Book
import com.example.data.model.BookCollection
import com.example.data.model.BookCollectionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdSync(id: Long): Book?

    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    suspend fun findBookByIsbn(isbn: String): Book?

    @Query("""
        SELECT * FROM books 
        WHERE title LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%' 
           OR publisher LIKE '%' || :query || '%' 
           OR CAST(publishYear AS TEXT) LIKE '%' || :query || '%'
           OR isbn LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY dateAdded DESC
    """)
    fun searchBooks(query: String): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    // Collections
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<BookCollection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getCollectionById(id: Long): Flow<BookCollection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: BookCollection): Long

    @Update
    suspend fun updateCollection(collection: BookCollection)

    @Delete
    suspend fun deleteCollection(collection: BookCollection)

    @Query("DELETE FROM book_collection_cross_ref WHERE collectionId = :collectionId")
    suspend fun deleteCrossRefsForCollection(collectionId: Long)

    @Query("DELETE FROM book_collection_cross_ref WHERE bookId = :bookId")
    suspend fun deleteCrossRefsForBook(bookId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookCollectionCrossRef(crossRef: BookCollectionCrossRef)

    @Query("DELETE FROM book_collection_cross_ref WHERE bookId = :bookId AND collectionId = :collectionId")
    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long)

    @Query("""
        SELECT c.* FROM collections c
        INNER JOIN book_collection_cross_ref x ON c.id = x.collectionId
        WHERE x.bookId = :bookId
    """)
    fun getCollectionsForBook(bookId: Long): Flow<List<BookCollection>>

    @Query("""
        SELECT c.* FROM collections c
        INNER JOIN book_collection_cross_ref x ON c.id = x.collectionId
        WHERE x.bookId = :bookId
    """)
    suspend fun getCollectionsForBookSync(bookId: Long): List<BookCollection>

    @Query("""
        SELECT b.* FROM books b
        INNER JOIN book_collection_cross_ref x ON b.id = x.bookId
        WHERE x.collectionId = :collectionId
        ORDER BY b.title ASC
    """)
    fun getBooksInCollection(collectionId: Long): Flow<List<Book>>

    @Query("SELECT * FROM book_collection_cross_ref")
    fun getAllCrossRefs(): Flow<List<BookCollectionCrossRef>>
}
