package com.example.data.repository

import com.example.data.local.BookDao
import com.example.data.model.Book
import com.example.data.model.BookCollection
import com.example.data.model.BookCollectionCrossRef
import com.example.data.remote.PublicBookResult
import com.example.data.remote.PublicLibraryService
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
    private val publicLibraryService: PublicLibraryService = PublicLibraryService()
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allCollections: Flow<List<BookCollection>> = bookDao.getAllCollections()
    val allCrossRefs: Flow<List<BookCollectionCrossRef>> = bookDao.getAllCrossRefs()

    fun getBookById(id: Long): Flow<Book?> = bookDao.getBookById(id)

    suspend fun getBookByIdSync(id: Long): Book? = bookDao.getBookByIdSync(id)

    suspend fun findBookByIsbn(isbn: String): Book? = bookDao.findBookByIsbn(isbn)

    fun searchCatalog(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    suspend fun insertBook(book: Book): Long = bookDao.insertBook(book)

    suspend fun updateBook(book: Book) = bookDao.updateBook(book)

    suspend fun deleteBook(book: Book) {
        bookDao.deleteCrossRefsForBook(book.id)
        bookDao.deleteBook(book)
    }

    suspend fun deleteBookById(id: Long) {
        bookDao.deleteCrossRefsForBook(id)
        bookDao.deleteBookById(id)
    }

    // Collections
    suspend fun insertCollection(collection: BookCollection): Long = bookDao.insertCollection(collection)

    suspend fun updateCollection(collection: BookCollection) = bookDao.updateCollection(collection)

    suspend fun deleteCollection(collection: BookCollection) {
        bookDao.deleteCrossRefsForCollection(collection.id)
        bookDao.deleteCollection(collection)
    }

    suspend fun addBookToCollection(bookId: Long, collectionId: Long) {
        bookDao.insertBookCollectionCrossRef(BookCollectionCrossRef(bookId, collectionId))
    }

    suspend fun removeBookFromCollection(bookId: Long, collectionId: Long) {
        bookDao.removeBookFromCollection(bookId, collectionId)
    }

    fun getCollectionsForBook(bookId: Long): Flow<List<BookCollection>> = bookDao.getCollectionsForBook(bookId)

    suspend fun getCollectionsForBookSync(bookId: Long): List<BookCollection> = bookDao.getCollectionsForBookSync(bookId)

    fun getBooksInCollection(collectionId: Long): Flow<List<Book>> = bookDao.getBooksInCollection(collectionId)

    suspend fun updateBookCollections(bookId: Long, targetCollectionIds: Set<Long>) {
        bookDao.deleteCrossRefsForBook(bookId)
        for (colId in targetCollectionIds) {
            bookDao.insertBookCollectionCrossRef(BookCollectionCrossRef(bookId, colId))
        }
    }

    // Remote Public Library
    suspend fun searchPublicLibraries(query: String): List<PublicBookResult> {
        return publicLibraryService.searchBooks(query)
    }

    suspend fun lookupIsbnOnline(isbn: String): PublicBookResult? {
        return publicLibraryService.lookupByIsbn(isbn)
    }
}
