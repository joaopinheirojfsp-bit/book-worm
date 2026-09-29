package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Book
import com.example.data.model.ReadingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BookWorm", appName)
    }

    @Test
    fun `book tags splitting works correctly`() {
        val book = Book(
            title = "Dom Casmurro",
            author = "Machado de Assis",
            tags = "Literatura Brasileira, Clássico, Romance"
        )
        val tags = book.getTagsList()
        assertEquals(3, tags.size)
        assertTrue(tags.contains("Literatura Brasileira"))
        assertTrue(tags.contains("Clássico"))
        assertTrue(tags.contains("Romance"))
    }

    @Test
    fun `book reading status default and label`() {
        val book = Book(
            title = "1984",
            author = "George Orwell",
            readingStatus = ReadingStatus.LIDO,
            rating = 5,
            personalReview = "Excelente distopia"
        )
        assertEquals("Lido", book.readingStatus.label)
        assertEquals(5, book.rating)
        assertEquals("Excelente distopia", book.personalReview)
    }
}
