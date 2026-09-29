package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Book
import com.example.data.model.ReadingStatus
import com.example.ui.BookViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StatisticsUnitTest {

    private lateinit var viewModel: BookViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = BookViewModel(app)
    }

    @Test
    fun testComputeTopGenres() {
        val books = listOf(
            Book(title = "B1", author = "A1", genre = "Ficção"),
            Book(title = "B2", author = "A2", genre = "Ficção"),
            Book(title = "B3", author = "A3", genre = "Clássico")
        )
        val stats = viewModel.computeTopGenres(books)
        assertEquals(2, stats.size)
        assertEquals("Ficção", stats[0].genre)
        assertEquals(2, stats[0].count)
    }

    @Test
    fun testComputeTopAuthors() {
        val books = listOf(
            Book(title = "B1", author = "Machado de Assis", readingStatus = ReadingStatus.LIDO),
            Book(title = "B2", author = "Machado de Assis", readingStatus = ReadingStatus.LIDO),
            Book(title = "B3", author = "George Orwell", readingStatus = ReadingStatus.QUERO_LER)
        )
        val stats = viewModel.computeTopAuthors(books)
        assertEquals(2, stats.size)
        assertEquals("Machado de Assis", stats[0].author)
        assertEquals(2, stats[0].booksRead)
    }

    @Test
    fun testRatingDistribution() {
        val books = listOf(
            Book(title = "B1", author = "A1", rating = 5),
            Book(title = "B2", author = "A2", rating = 5),
            Book(title = "B3", author = "A3", rating = 4)
        )
        val dist = viewModel.computeRatingDistribution(books)
        val fiveStars = dist.first { it.star == 5 }
        assertEquals(2, fiveStars.count)
    }
}
