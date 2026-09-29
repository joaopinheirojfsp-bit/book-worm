package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Book
import com.example.data.model.BookCollection
import com.example.data.model.ReadingStatus
import com.example.data.remote.PublicBookResult
import com.example.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SortOption(val label: String) {
    RECENT("Mais Recentes"),
    TITLE("Título (A-Z)"),
    RATING("Maior Avaliação"),
    YEAR("Ano de Publicação")
}

sealed interface LookupUiState {
    object Idle : LookupUiState
    object Loading : LookupUiState
    data class Success(val result: PublicBookResult) : LookupUiState
    data class NotFound(val isbn: String) : LookupUiState
    data class Error(val message: String) : LookupUiState
}

data class FilterCriteria(
    val query: String = "",
    val status: ReadingStatus? = null,
    val onlyFavorites: Boolean = false,
    val sort: SortOption = SortOption.RECENT,
    val author: String = "",
    val publisher: String = "",
    val year: String = "",
    val selectedTag: String? = null,
    val selectedCollectionId: Long? = null
)

// Statistics Models
data class GenreStat(val genre: String, val count: Int, val percentage: Float)
data class AuthorStat(val author: String, val count: Int, val booksRead: Int)
data class TimeProgressStat(val periodLabel: String, val booksCount: Int, val pagesCount: Int)
data class RatingDistribution(val star: Int, val count: Int, val percentage: Float)

data class PeriodSummary(
    val periodTitle: String,
    val totalRead: Int,
    val totalPages: Int,
    val avgRating: Double,
    val topGenre: String,
    val topAuthor: String,
    val bestBook: Book?
)

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BookRepository(db.bookDao())
    }

    val filterCriteria = MutableStateFlow(FilterCriteria())

    val collections: StateFlow<List<BookCollection>> = repository.allCollections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBooksRaw: StateFlow<List<Book>> = repository.allBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered books state
    val books: StateFlow<List<Book>> = combine(
        repository.allBooks,
        repository.allCrossRefs,
        filterCriteria
    ) { all, crossRefs, criteria ->
        var filtered = all

        // Filter by collection if selected
        if (criteria.selectedCollectionId != null) {
            val bookIdsInCollection = crossRefs
                .filter { it.collectionId == criteria.selectedCollectionId }
                .map { it.bookId }
                .toSet()
            filtered = filtered.filter { it.id in bookIdsInCollection }
        }

        // Filter by tag if selected
        if (!criteria.selectedTag.isNullOrBlank()) {
            val tagLower = criteria.selectedTag.trim().lowercase()
            filtered = filtered.filter { book ->
                book.getTagsList().any { it.trim().lowercase() == tagLower }
            }
        }

        // Filter by universal query
        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            filtered = filtered.filter { b ->
                b.title.lowercase().contains(q) ||
                b.author.lowercase().contains(q) ||
                b.publisher.lowercase().contains(q) ||
                (b.publishYear?.toString()?.contains(q) == true) ||
                b.isbn.contains(q) ||
                b.personalReview.lowercase().contains(q) ||
                b.genre.lowercase().contains(q) ||
                b.tags.lowercase().contains(q)
            }
        }

        // Specific Author filter
        if (criteria.author.isNotBlank()) {
            val af = criteria.author.trim().lowercase()
            filtered = filtered.filter { it.author.lowercase().contains(af) }
        }

        // Specific Publisher filter
        if (criteria.publisher.isNotBlank()) {
            val pf = criteria.publisher.trim().lowercase()
            filtered = filtered.filter { it.publisher.lowercase().contains(pf) }
        }

        // Specific Year filter
        if (criteria.year.isNotBlank()) {
            val yf = criteria.year.trim()
            filtered = filtered.filter { it.publishYear?.toString()?.contains(yf) == true }
        }

        // Status filter
        if (criteria.status != null) {
            filtered = filtered.filter { it.readingStatus == criteria.status }
        }

        // Favorites filter
        if (criteria.onlyFavorites) {
            filtered = filtered.filter { it.isFavorite }
        }

        // Sorting
        when (criteria.sort) {
            SortOption.RECENT -> filtered.sortedByDescending { it.dateAdded }
            SortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOption.RATING -> filtered.sortedByDescending { it.rating }
            SortOption.YEAR -> filtered.sortedByDescending { it.publishYear ?: 0 }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Distinct tags across all books in user catalog
    val allTags: StateFlow<List<String>> = repository.allBooks
        .combine(MutableStateFlow(Unit)) { booksList, _ ->
            val set = mutableSetOf<String>()
            booksList.forEach { b ->
                set.addAll(b.getTagsList())
            }
            set.sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Public Library search state
    val publicSearchResults = MutableStateFlow<List<PublicBookResult>>(emptyList())
    val isSearchingPublic = MutableStateFlow(false)
    val publicSearchError = MutableStateFlow<String?>(null)

    // Barcode lookup state
    val barcodeLookupState = MutableStateFlow<LookupUiState>(LookupUiState.Idle)

    fun getBookById(id: Long) = repository.getBookById(id)

    fun getCollectionsForBook(bookId: Long) = repository.getCollectionsForBook(bookId)

    suspend fun getCollectionsForBookSync(bookId: Long): List<BookCollection> =
        repository.getCollectionsForBookSync(bookId)

    fun getBooksInCollection(collectionId: Long) = repository.getBooksInCollection(collectionId)

    fun addBook(book: Book, collectionIds: Set<Long> = emptySet(), onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertBook(book)
            if (collectionIds.isNotEmpty()) {
                repository.updateBookCollections(id, collectionIds)
            }
            onComplete(id)
        }
    }

    fun updateBook(book: Book, collectionIds: Set<Long>? = null) {
        viewModelScope.launch {
            repository.updateBook(book)
            if (collectionIds != null) {
                repository.updateBookCollections(book.id, collectionIds)
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    fun deleteBookById(id: Long) {
        viewModelScope.launch {
            repository.deleteBookById(id)
        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            repository.updateBook(book.copy(isFavorite = !book.isFavorite))
        }
    }

    fun updateRatingAndReview(book: Book, rating: Int, review: String) {
        viewModelScope.launch {
            repository.updateBook(book.copy(rating = rating, personalReview = review))
        }
    }

    fun updateReadingStatus(book: Book, status: ReadingStatus) {
        viewModelScope.launch {
            val finishedDate = if (status == ReadingStatus.LIDO && book.dateFinished == null) {
                System.currentTimeMillis()
            } else book.dateFinished
            repository.updateBook(book.copy(readingStatus = status, dateFinished = finishedDate))
        }
    }

    // Collections CRUD
    fun createCollection(name: String, description: String = "", colorHex: String = "#1E3A5F") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCollection(
                BookCollection(
                    name = name.trim(),
                    description = description.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateCollection(collection: BookCollection) {
        viewModelScope.launch {
            repository.updateCollection(collection)
        }
    }

    fun deleteCollection(collection: BookCollection) {
        viewModelScope.launch {
            if (filterCriteria.value.selectedCollectionId == collection.id) {
                filterCriteria.value = filterCriteria.value.copy(selectedCollectionId = null)
            }
            repository.deleteCollection(collection)
        }
    }

    // Filter controls
    fun setQuery(q: String) {
        filterCriteria.value = filterCriteria.value.copy(query = q)
    }

    fun setSearchQuery(q: String) = setQuery(q)

    fun setStatusFilter(status: ReadingStatus?) {
        filterCriteria.value = filterCriteria.value.copy(status = status)
    }

    fun setFavoritesOnly(fav: Boolean) {
        filterCriteria.value = filterCriteria.value.copy(onlyFavorites = fav)
    }

    fun toggleOnlyFavorites() {
        filterCriteria.value = filterCriteria.value.copy(onlyFavorites = !filterCriteria.value.onlyFavorites)
    }

    fun setAuthorFilter(author: String) {
        filterCriteria.value = filterCriteria.value.copy(author = author)
    }

    fun setPublisherFilter(publisher: String) {
        filterCriteria.value = filterCriteria.value.copy(publisher = publisher)
    }

    fun setYearFilter(year: String) {
        filterCriteria.value = filterCriteria.value.copy(year = year)
    }

    fun clearAdvancedFilters() {
        filterCriteria.value = filterCriteria.value.copy(author = "", publisher = "", year = "", onlyFavorites = false)
    }

    fun setSortOption(sort: SortOption) {
        filterCriteria.value = filterCriteria.value.copy(sort = sort)
    }

    fun setTagFilter(tag: String?) {
        filterCriteria.value = filterCriteria.value.copy(selectedTag = tag)
    }

    fun setCollectionFilter(collectionId: Long?) {
        filterCriteria.value = filterCriteria.value.copy(selectedCollectionId = collectionId)
    }

    fun setAdvancedFilters(author: String, publisher: String, year: String) {
        filterCriteria.value = filterCriteria.value.copy(
            author = author,
            publisher = publisher,
            year = year
        )
    }

    fun resetAllFilters() {
        filterCriteria.value = FilterCriteria()
    }

    // Online search & Barcode
    fun searchPublicLibraries(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            isSearchingPublic.value = true
            publicSearchError.value = null
            try {
                val results = repository.searchPublicLibraries(query)
                publicSearchResults.value = results
                if (results.isEmpty()) {
                    publicSearchError.value = "Nenhum livro encontrado nas bibliotecas públicas."
                }
            } catch (e: Exception) {
                publicSearchError.value = "Erro na busca: ${e.localizedMessage}"
            } finally {
                isSearchingPublic.value = false
            }
        }
    }

    fun lookupIsbn(isbn: String) {
        val clean = isbn.replace("-", "").replace(" ", "").trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            barcodeLookupState.value = LookupUiState.Loading
            try {
                val localBook = repository.findBookByIsbn(clean)
                if (localBook != null) {
                    barcodeLookupState.value = LookupUiState.Success(
                        PublicBookResult(
                            isbn = localBook.isbn,
                            title = localBook.title,
                            authors = localBook.author,
                            publisher = localBook.publisher,
                            publishYear = localBook.publishYear,
                            description = localBook.description,
                            pageCount = localBook.pageCount,
                            coverUrl = localBook.coverUrl,
                            source = "Já no seu catálogo"
                        )
                    )
                    return@launch
                }

                val onlineResult = repository.lookupIsbnOnline(clean)
                if (onlineResult != null) {
                    barcodeLookupState.value = LookupUiState.Success(onlineResult)
                } else {
                    barcodeLookupState.value = LookupUiState.NotFound(clean)
                }
            } catch (e: Exception) {
                barcodeLookupState.value = LookupUiState.Error(e.localizedMessage ?: "Erro ao buscar ISBN")
            }
        }
    }

    fun resetLookupState() {
        barcodeLookupState.value = LookupUiState.Idle
    }

    // --- Statistics Computation Methods ---

    fun computeTopGenres(booksList: List<Book>): List<GenreStat> {
        val map = mutableMapOf<String, Int>()
        var total = 0
        for (b in booksList) {
            val genre = if (b.genre.isNotBlank()) b.genre.trim() else {
                val tags = b.getTagsList()
                if (tags.isNotEmpty()) tags.first() else "Geral"
            }
            map[genre] = (map[genre] ?: 0) + 1
            total++
        }
        if (total == 0) return emptyList()

        return map.entries
            .sortedByDescending { it.value }
            .take(6)
            .map { entry ->
                GenreStat(
                    genre = entry.key,
                    count = entry.value,
                    percentage = (entry.value.toFloat() / total.toFloat()) * 100f
                )
            }
    }

    fun computeTopAuthors(booksList: List<Book>): List<AuthorStat> {
        val totalMap = mutableMapOf<String, Int>()
        val readMap = mutableMapOf<String, Int>()

        for (b in booksList) {
            val author = if (b.author.isNotBlank()) b.author.trim() else "Desconhecido"
            totalMap[author] = (totalMap[author] ?: 0) + 1
            if (b.readingStatus == ReadingStatus.LIDO) {
                readMap[author] = (readMap[author] ?: 0) + 1
            }
        }

        return totalMap.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { entry ->
                AuthorStat(
                    author = entry.key,
                    count = entry.value,
                    booksRead = readMap[entry.key] ?: 0
                )
            }
    }

    fun computeReadingProgressOverTime(booksList: List<Book>): List<TimeProgressStat> {
        // Group read books by month (last 6 months)
        val readBooks = booksList.filter { it.readingStatus == ReadingStatus.LIDO }
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))

        val result = mutableListOf<TimeProgressStat>()

        // Generate the last 6 months buckets
        val monthBuckets = mutableListOf<Pair<Long, Long>>() // startMs, endMs
        val monthLabels = mutableListOf<String>()

        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startMs = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val endMs = cal.timeInMillis

            monthBuckets.add(startMs to endMs)
            monthLabels.add(monthFormat.format(Date(startMs)).replace(".", "").uppercase())
        }

        for (idx in monthBuckets.indices) {
            val (start, end) = monthBuckets[idx]
            val label = monthLabels[idx]
            val booksInMonth = readBooks.filter { b ->
                val date = b.dateFinished ?: b.dateAdded
                date in start..end
            }
            val count = booksInMonth.size
            val pages = booksInMonth.sumOf { it.pageCount ?: 0 }
            result.add(TimeProgressStat(periodLabel = label, booksCount = count, pagesCount = pages))
        }

        return result
    }

    fun computeRatingDistribution(booksList: List<Book>): List<RatingDistribution> {
        val rated = booksList.filter { it.rating in 1..5 }
        val total = rated.size
        return (5 downTo 1).map { star ->
            val count = rated.count { it.rating == star }
            val pct = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f
            RatingDistribution(star = star, count = count, percentage = pct)
        }
    }

    fun generatePeriodSummary(booksList: List<Book>, isYearly: Boolean, targetYearOrMonth: Int): PeriodSummary {
        val cal = Calendar.getInstance()
        val filteredBooks = if (isYearly) {
            booksList.filter { b ->
                val ts = b.dateFinished ?: b.dateAdded
                cal.timeInMillis = ts
                cal.get(Calendar.YEAR) == targetYearOrMonth
            }
        } else {
            // targetYearOrMonth is Calendar month (0-11)
            val currentYear = cal.get(Calendar.YEAR)
            booksList.filter { b ->
                val ts = b.dateFinished ?: b.dateAdded
                cal.timeInMillis = ts
                cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == targetYearOrMonth
            }
        }

        val readBooks = filteredBooks.filter { it.readingStatus == ReadingStatus.LIDO }
        val totalRead = readBooks.size
        val totalPages = readBooks.sumOf { it.pageCount ?: 0 }

        val rated = readBooks.filter { it.rating > 0 }
        val avgRating = if (rated.isNotEmpty()) rated.map { it.rating }.average() else 0.0

        val genreStats = computeTopGenres(readBooks)
        val topGenre = genreStats.firstOrNull()?.genre ?: "Variado"

        val authorStats = computeTopAuthors(readBooks)
        val topAuthor = authorStats.firstOrNull()?.author ?: "Diversos"

        val bestBook = readBooks.maxByOrNull { it.rating }

        val title = if (isYearly) {
            "Resumo Anual de $targetYearOrMonth"
        } else {
            val monthNames = arrayOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
            "Resumo de ${monthNames.getOrElse(targetYearOrMonth) { "Mês" }} de ${Calendar.getInstance().get(Calendar.YEAR)}"
        }

        return PeriodSummary(
            periodTitle = title,
            totalRead = totalRead,
            totalPages = totalPages,
            avgRating = avgRating,
            topGenre = topGenre,
            topAuthor = topAuthor,
            bestBook = bestBook
        )
    }
}
