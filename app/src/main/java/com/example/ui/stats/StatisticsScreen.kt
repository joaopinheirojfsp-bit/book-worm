package com.example.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ReadingStatus
import com.example.ui.BookViewModel
import com.example.ui.theme.StarGold
import com.example.ui.theme.WarmAmber
import com.example.util.PdfExporter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allBooks by viewModel.allBooksRaw.collectAsState()

    // 0 = Geral, 1 = Resumo Mensal, 2 = Resumo Anual
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-11

    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth) }

    val topGenres = remember(allBooks) { viewModel.computeTopGenres(allBooks) }
    val topAuthors = remember(allBooks) { viewModel.computeTopAuthors(allBooks) }
    val timeProgress = remember(allBooks) { viewModel.computeReadingProgressOverTime(allBooks) }
    val ratingDist = remember(allBooks) { viewModel.computeRatingDistribution(allBooks) }

    val periodSummary = remember(allBooks, selectedTab, selectedYear, selectedMonth) {
        if (selectedTab == 1) {
            viewModel.generatePeriodSummary(allBooks, isYearly = false, targetYearOrMonth = selectedMonth)
        } else if (selectedTab == 2) {
            viewModel.generatePeriodSummary(allBooks, isYearly = true, targetYearOrMonth = selectedYear)
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas & Relatórios") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Geral") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mensal") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Anual") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Controls for Monthly or Yearly tabs
                if (selectedTab == 1) {
                    item {
                        MonthlySelectorCard(
                            selectedMonth = selectedMonth,
                            onMonthSelected = { selectedMonth = it }
                        )
                    }
                } else if (selectedTab == 2) {
                    item {
                        YearlySelectorCard(
                            selectedYear = selectedYear,
                            currentYear = currentYear,
                            onYearSelected = { selectedYear = it }
                        )
                    }
                }

                // If in Monthly or Yearly view, display Period Summary Card on top
                if (periodSummary != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("period_summary_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = periodSummary.periodTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = WarmAmber)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SummaryMetric(
                                        label = "Livros Lidos",
                                        value = "${periodSummary.totalRead}",
                                        icon = Icons.Default.Book
                                    )
                                    SummaryMetric(
                                        label = "Páginas Lidas",
                                        value = "${periodSummary.totalPages}",
                                        icon = Icons.Default.MenuBook
                                    )
                                    SummaryMetric(
                                        label = "Nota Média",
                                        value = if (periodSummary.avgRating > 0) String.format("%.1f ★", periodSummary.avgRating) else "—",
                                        icon = Icons.Default.Star
                                    )
                                }

                                if (periodSummary.bestBook != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StarGold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Livro Destaque:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${periodSummary.bestBook.title} (${periodSummary.bestBook.rating}★)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val cal = Calendar.getInstance()
                                            val periodBooks = if (selectedTab == 2) {
                                                allBooks.filter { b ->
                                                    val ts = b.dateFinished ?: b.dateAdded
                                                    cal.timeInMillis = ts
                                                    cal.get(Calendar.YEAR) == selectedYear
                                                }
                                            } else {
                                                allBooks.filter { b ->
                                                    val ts = b.dateFinished ?: b.dateAdded
                                                    cal.timeInMillis = ts
                                                    cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == selectedMonth
                                                }
                                            }
                                            PdfExporter.generateAndShareCatalogPdf(
                                                context = context,
                                                books = periodBooks,
                                                catalogTitle = periodSummary.periodTitle
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Exportar PDF")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val summaryText = buildString {
                                                appendLine("📊 ${periodSummary.periodTitle}")
                                                appendLine("📖 Livros lidos: ${periodSummary.totalRead}")
                                                appendLine("📄 Páginas lidas: ${periodSummary.totalPages}")
                                                if (periodSummary.avgRating > 0) appendLine("⭐ Nota média: ${String.format("%.1f", periodSummary.avgRating)} / 5.0")
                                                appendLine("🏆 Gênero favorito: ${periodSummary.topGenre}")
                                                if (periodSummary.bestBook != null) {
                                                    appendLine("✨ Livro favorito: ${periodSummary.bestBook.title} (${periodSummary.bestBook.author})")
                                                }
                                                appendLine("\nGerado pelo BiblioCatalog")
                                            }
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, summaryText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Compartilhar Resumo"))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compartilhar")
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Overview Cards (shown on General tab)
                if (selectedTab == 0) {
                    item {
                        OverviewHeader(allBooks = allBooks)
                    }

                    // Reading Progress Over Time (Chart)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reading_progress_chart_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Progresso de Leitura no Tempo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Livros concluídos nos últimos 6 meses:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Custom Canvas Bar Chart for Time Progress
                                ReadingProgressBarChart(
                                    data = timeProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                            }
                        }
                    }

                    // Top Genres Visualization
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("top_genres_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Principais Gêneros Literários",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (topGenres.isEmpty()) {
                                    Text("Nenhum dado de gênero cadastrado.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    topGenres.forEach { genreStat ->
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = genreStat.genre,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${genreStat.count} livro(s) (${String.format("%.0f", genreStat.percentage)}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { genreStat.percentage / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Most Read Authors
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("top_authors_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Autores Mais Lidos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (topAuthors.isEmpty()) {
                                    Text("Nenhum autor registrado.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    topAuthors.forEachIndexed { index, authorStat ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (index == 0) StarGold else MaterialTheme.colorScheme.primaryContainer
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (index == 0) Color(0xFF78350F) else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = authorStat.author,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${authorStat.booksRead} lido(s) de ${authorStat.count} no catálogo",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Rating Distribution & Average
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ratings_distribution_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val ratedList = allBooks.filter { it.rating > 0 }
                                val overallAvg = if (ratedList.isNotEmpty()) ratedList.map { it.rating }.average() else 0.0

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Distribuição de Avaliações",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = String.format("%.1f", overallAvg),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                ratingDist.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.star} ★",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.width(32.dp)
                                        )
                                        LinearProgressIndicator(
                                            progress = { item.percentage / 100f },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = StarGold,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${item.count}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(24.dp),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewHeader(allBooks: List<com.example.data.model.Book>) {
    val total = allBooks.size
    val read = allBooks.count { it.readingStatus == ReadingStatus.LIDO }
    val reading = allBooks.count { it.readingStatus == ReadingStatus.LENDO }
    val wantToRead = allBooks.count { it.readingStatus == ReadingStatus.QUERO_LER }
    val pages = allBooks.filter { it.readingStatus == ReadingStatus.LIDO }.sumOf { it.pageCount ?: 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumo do Seu Acervo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Total", value = "$total")
                MetricColumn(title = "Lidos", value = "$read")
                MetricColumn(title = "Lendo", value = "$reading")
                MetricColumn(title = "Quero Ler", value = "$wantToRead")
                MetricColumn(title = "Páginas", value = "$pages")
            }
        }
    }
}

@Composable
fun MetricColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SummaryMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MonthlySelectorCard(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    val months = listOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Selecione o Mês:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                months.take(6).forEachIndexed { idx, name ->
                    FilterChip(
                        selected = selectedMonth == idx,
                        onClick = { onMonthSelected(idx) },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                months.drop(6).forEachIndexed { idx, name ->
                    val actualMonth = idx + 6
                    FilterChip(
                        selected = selectedMonth == actualMonth,
                        onClick = { onMonthSelected(actualMonth) },
                        label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun YearlySelectorCard(selectedYear: Int, currentYear: Int, onYearSelected: (Int) -> Unit) {
    val years = listOf(currentYear, currentYear - 1, currentYear - 2, currentYear - 3)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ano:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            years.forEach { year ->
                FilterChip(
                    selected = selectedYear == year,
                    onClick = { onYearSelected(year) },
                    label = { Text("$year") }
                )
            }
        }
    }
}

@Composable
fun ReadingProgressBarChart(
    data: List<com.example.ui.TimeProgressStat>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxCount = (data.maxOfOrNull { it.booksCount } ?: 1).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val totalHeight = size.height
        val bottomLabelHeight = 24.dp.toPx()
        val chartHeight = totalHeight - bottomLabelHeight

        val barCount = data.size
        val barSlotWidth = totalWidth / barCount
        val barWidth = barSlotWidth * 0.55f

        data.forEachIndexed { index, stat ->
            val x = (index * barSlotWidth) + (barSlotWidth - barWidth) / 2f
            val barFraction = stat.booksCount.toFloat() / maxCount.toFloat()
            val barHeight = (chartHeight * barFraction).coerceAtLeast(6f)
            val barTop = chartHeight - barHeight

            // Background bar track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(x, 10f),
                size = Size(barWidth, chartHeight - 10f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Active bar
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(x, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Value text above bar
            if (stat.booksCount > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${stat.booksCount}",
                    x + (barWidth / 2f),
                    (barTop - 6f).coerceAtLeast(12f),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                )
            }

            // Month label at bottom
            drawContext.canvas.nativeCanvas.drawText(
                stat.periodLabel,
                x + (barWidth / 2f),
                totalHeight - 4f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
    }
}
