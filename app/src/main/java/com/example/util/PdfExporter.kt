package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Book
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportCatalogPdf(context: Context, books: List<Book>, catalogTitle: String = "BookWorm - Minha Biblioteca") {
        generateAndShareCatalogPdf(context, books, catalogTitle)
    }

    fun generateAndShareCatalogPdf(
        context: Context,
        books: List<Book>,
        catalogTitle: String = "BookWorm - Minha Biblioteca"
    ) {
        if (books.isEmpty()) {
            Toast.makeText(context, "Nenhum livro para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val file = createCatalogPdfFile(context, books, catalogTitle)
            sharePdfFile(context, file, catalogTitle)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareBookText(context: Context, book: Book) {
        val stars = if (book.rating > 0) "★".repeat(book.rating) + "☆".repeat(5 - book.rating) else "Não avaliado"
        val shareText = buildString {
            appendLine("📚 Recomendação de Leitura: *${book.title}*")
            appendLine("✍️ Autor: ${book.author}")
            if (book.publisher.isNotEmpty()) appendLine("🏢 Editora: ${book.publisher}")
            if (book.publishYear != null) appendLine("📅 Ano: ${book.publishYear}")
            if (book.isbn.isNotEmpty()) appendLine("🔢 ISBN: ${book.isbn}")
            appendLine("📖 Status: ${book.readingStatus.label}")
            appendLine("⭐ Avaliação pessoal: $stars")
            if (book.personalReview.isNotEmpty()) {
                appendLine()
                appendLine("💬 Meu comentário:")
                appendLine("\"${book.personalReview}\"")
            }
            appendLine()
            appendLine("Catalogado no BookWorm")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartilhar recomendação")
        context.startActivity(shareIntent)
    }

    fun shareCatalogText(context: Context, books: List<Book>) {
        if (books.isEmpty()) {
            Toast.makeText(context, "Nenhum livro para compartilhar.", Toast.LENGTH_SHORT).show()
            return
        }

        val text = buildString {
            appendLine("📚 Minha Biblioteca BookWorm (${books.size} exemplares)")
            appendLine("Gerado pelo BookWorm")
            appendLine("----------------------------------")
            books.forEachIndexed { index, b ->
                val stars = if (b.rating > 0) " [" + "★".repeat(b.rating) + "]" else ""
                appendLine("${index + 1}. ${b.title} - ${b.author} (${b.publishYear ?: "S/A"})$stars - ${b.readingStatus.label}")
                if (b.personalReview.isNotBlank()) {
                    appendLine("   \"${b.personalReview}\"")
                }
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Catálogo de Livros"))
    }

    private fun createCatalogPdfFile(context: Context, books: List<Book>, catalogTitle: String): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width (pt)
        val pageHeight = 842 // A4 standard height (pt)

        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 58, 95) // InkNavy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val bookTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val reviewPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val starPaint = Paint().apply {
            color = Color.rgb(217, 119, 6) // Warm amber
            textSize = 12f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        val bannerPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = margin + 15f

        fun drawHeader(c: Canvas) {
            c.drawText(catalogTitle, margin, y, titlePaint)
            y += 18f
            val dateStr = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date())
            val lidosCount = books.count { it.readingStatus.name == "LIDO" }
            val statsStr = "Gerado em: $dateStr | Total: ${books.size} livros | Lidos: $lidosCount"
            c.drawText(statsStr, margin, y, subtitlePaint)
            y += 15f

            // Separator bar
            c.drawLine(margin, y, margin + contentWidth, y, linePaint)
            y += 20f
        }

        fun drawFooter(c: Canvas, pageNum: Int) {
            val footerY = pageHeight - 20f
            c.drawText("BiblioCatalog — Página $pageNum", pageWidth / 2f, footerY, footerPaint)
        }

        drawHeader(canvas)

        for (book in books) {
            // Estimate height needed for this book entry
            val estimatedHeight = 70f + if (book.personalReview.isNotBlank()) 30f else 0f

            if (y + estimatedHeight > pageHeight - 50f) {
                // Finish current page and start a new one
                drawFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = margin + 15f
                drawHeader(canvas)
            }

            // Draw book background card container
            val cardTop = y - 10f
            val cardHeight = estimatedHeight - 10f
            canvas.drawRoundRect(
                margin,
                cardTop,
                margin + contentWidth,
                cardTop + cardHeight,
                8f,
                8f,
                bannerPaint
            )

            // Title
            val displayTitle = truncate(book.title, 55)
            canvas.drawText(displayTitle, margin + 12f, y + 6f, bookTitlePaint)

            // Rating Stars on top right
            if (book.rating > 0) {
                val starsText = "★".repeat(book.rating) + "☆".repeat(5 - book.rating)
                val starsWidth = starPaint.measureText(starsText)
                canvas.drawText(starsText, margin + contentWidth - starsWidth - 12f, y + 6f, starPaint)
            }

            y += 22f

            // Meta line: Author, Year, Publisher, ISBN
            val metaBuilder = StringBuilder()
            metaBuilder.append("Autor: ${truncate(book.author, 35)}")
            if (book.publishYear != null) metaBuilder.append(" • Ano: ${book.publishYear}")
            if (book.publisher.isNotBlank()) metaBuilder.append(" • Ed: ${truncate(book.publisher, 20)}")
            if (book.isbn.isNotBlank()) metaBuilder.append(" • ISBN: ${book.isbn}")
            metaBuilder.append(" • Status: ${book.readingStatus.label}")

            canvas.drawText(metaBuilder.toString(), margin + 12f, y, metaPaint)
            y += 18f

            // Personal review / comments
            if (book.personalReview.isNotBlank()) {
                val reviewText = "Avaliação: \"${truncate(book.personalReview, 80)}\""
                canvas.drawText(reviewText, margin + 12f, y, reviewPaint)
                y += 18f
            }

            y += 15f // Space between books
        }

        drawFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        // Save to cache directory
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, "Catalogo_Livros_${System.currentTimeMillis()}.pdf")
        val out = FileOutputStream(file)
        pdfDocument.writeTo(out)
        out.flush()
        out.close()
        pdfDocument.close()

        return file
    }

    private fun sharePdfFile(context: Context, file: File, title: String) {
        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Compartilhando meu catálogo de livros exportado do BiblioCatalog.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Exportar / Compartilhar PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun truncate(str: String, maxLength: Int): String {
        return if (str.length > maxLength) {
            str.substring(0, maxLength - 3) + "..."
        } else {
            str
        }
    }
}
