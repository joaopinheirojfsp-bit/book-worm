package com.example.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Book
import com.example.data.model.ReadingStatus
import com.example.ui.BookViewModel
import com.example.ui.components.BookCardItem
import com.example.ui.components.BookGridCard
import com.example.ui.components.BookWormTopLeftMenu
import com.example.ui.theme.StarGold
import com.example.ui.theme.WarmAmber
import com.example.util.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BookViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPublicSearch: () -> Unit,
    onNavigateToAddManual: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val allBooks by viewModel.allBooksRaw.collectAsState()

    // View mode toggle: list vs grid (persisted during recomposition)
    var isGridView by rememberSaveable { mutableStateOf(false) }

    // Dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddOptionsDialog by remember { mutableStateOf(false) }
    var showPickReadingBookDialog by remember { mutableStateOf(false) }

    // Books currently in reading status
    val readingBooks = allBooks.filter { it.readingStatus == ReadingStatus.LENDO }
    val readBooks = allBooks.filter { it.readingStatus == ReadingStatus.LIDO }
    val wantToReadBooks = allBooks.filter { it.readingStatus == ReadingStatus.QUERO_LER }

    var currentReadingIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeReadingBook = if (readingBooks.isNotEmpty()) {
        val safeIndex = currentReadingIndex.coerceIn(0, readingBooks.lastIndex)
        readingBooks[safeIndex]
    } else null

    // Export PDF Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Biblioteca") },
            text = {
                Text("Deseja gerar um documento PDF completo da sua biblioteca BookWorm ou compartilhar o catálogo como texto formatado?")
            },
            confirmButton = {
                Button(onClick = {
                    showExportDialog = false
                    PdfExporter.exportCatalogPdf(context, allBooks)
                }) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gerar PDF")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        showExportDialog = false
                        PdfExporter.shareCatalogText(context, allBooks)
                    }) {
                        Text("Compartilhar Texto")
                    }
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

    // Add Options Dialog
    if (showAddOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showAddOptionsDialog = false },
            title = { Text("Adicionar Livro ao BookWorm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showAddOptionsDialog = false
                            onNavigateToScan()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_scan_barcode")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear Código de Barras (Câmera)")
                    }

                    OutlinedButton(
                        onClick = {
                            showAddOptionsDialog = false
                            onNavigateToPublicSearch()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_search_public_library")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buscar em Bibliotecas Públicas")
                    }

                    OutlinedButton(
                        onClick = {
                            showAddOptionsDialog = false
                            onNavigateToAddManual()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("action_add_manual")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadastrar Manualmente")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddOptionsDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Pick Reading Book Dialog
    if (showPickReadingBookDialog) {
        val nonReadingBooks = allBooks.filter { it.readingStatus != ReadingStatus.LENDO }
        AlertDialog(
            onDismissRequest = { showPickReadingBookDialog = false },
            title = { Text("Escolher Livro para Leitura") },
            text = {
                if (nonReadingBooks.isEmpty()) {
                    Text("Todos os livros da sua biblioteca já estão marcados ou você não possui exemplares cadastrados.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(nonReadingBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateReadingStatus(book, ReadingStatus.LENDO)
                                        showPickReadingBookDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(book.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(book.author, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPickReadingBookDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 3-dots menu on the TOP-LEFT corner with ALL app options
                    BookWormTopLeftMenu(
                        currentRoute = "home",
                        onNavigateToHome = { /* Already on Home */ },
                        onNavigateToLibrary = onNavigateToLibrary,
                        onNavigateToScan = onNavigateToScan,
                        onNavigateToPublicSearch = onNavigateToPublicSearch,
                        onNavigateToCollections = onNavigateToCollections,
                        onNavigateToStats = onNavigateToStats,
                        onNavigateToAddBook = { showAddOptionsDialog = true },
                        onExportPdf = { showExportDialog = true }
                    )
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.bookworm_logo_1790265940738),
                            contentDescription = "Logo BookWorm",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BookWorm",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Fast scanner button
                    IconButton(
                        onClick = onNavigateToScan,
                        modifier = Modifier.testTag("home_scanner_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear Código de Barras"
                        )
                    }

                    // My Library shortcut
                    IconButton(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier.testTag("home_library_button")
                    ) {
                        Icon(
                            Icons.Default.LocalLibrary,
                            contentDescription = "A Minha Biblioteca",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptionsDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Adicionar Livro") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("home_fab_add_book")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. HERO SECTION: Livro "A Ler"
            item {
                FeaturedReadingSection(
                    activeBook = activeReadingBook,
                    readingCount = readingBooks.size,
                    currentIndex = currentReadingIndex,
                    onNext = {
                        if (currentReadingIndex < readingBooks.lastIndex) {
                            currentReadingIndex++
                        } else {
                            currentReadingIndex = 0
                        }
                    },
                    onPrevious = {
                        if (currentReadingIndex > 0) {
                            currentReadingIndex--
                        } else {
                            currentReadingIndex = readingBooks.lastIndex
                        }
                    },
                    onContinueReading = { book -> onNavigateToDetail(book.id) },
                    onMarkAsRead = { book -> viewModel.updateReadingStatus(book, ReadingStatus.LIDO) },
                    onPickBook = { showPickReadingBookDialog = true },
                    onAddBook = { showAddOptionsDialog = true }
                )
            }

            // 2. Reading Snapshot Counters Row
            item {
                ReadingSnapshotRow(
                    readingCount = readingBooks.size,
                    readCount = readBooks.size,
                    wantToReadCount = wantToReadBooks.size,
                    totalCount = allBooks.size,
                    onNavigateToLibrary = onNavigateToLibrary
                )
            }

            // 3. Quick Action Hub
            item {
                QuickActionHub(
                    onScan = onNavigateToScan,
                    onSearchPublic = onNavigateToPublicSearch,
                    onCollections = onNavigateToCollections,
                    onStats = onNavigateToStats
                )
            }

            // 4. Section Header with List vs Grid Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "A Minha Biblioteca",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${allBooks.size} livros catalogados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Toggle view mode: Lista vs Grelha
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.testTag("home_view_mode_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Vista de Lista button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (!isGridView) MaterialTheme.colorScheme.surface
                                            else Color.Transparent
                                        )
                                        .clickable { isGridView = false }
                                        .testTag("toggle_view_mode_list"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewList,
                                        contentDescription = "Vista de Lista",
                                        tint = if (!isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Vista de Grelha button
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isGridView) MaterialTheme.colorScheme.surface
                                            else Color.Transparent
                                        )
                                        .clickable { isGridView = true }
                                        .testTag("toggle_view_mode_grid"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = "Vista de Grelha",
                                        tint = if (isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Link to full library
                        TextButton(
                            onClick = onNavigateToLibrary,
                            modifier = Modifier.testTag("see_all_library_button")
                        ) {
                            Text("Ver Todos →", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 5. Books Content: List View vs Grid View
            if (allBooks.isEmpty()) {
                item {
                    EmptyHomeState(
                        onScan = onNavigateToScan,
                        onSearchPublic = onNavigateToPublicSearch,
                        onAddManual = onNavigateToAddManual
                    )
                }
            } else if (isGridView) {
                // Render books in a 2-column grid inside LazyColumn
                val bookRows = allBooks.chunked(2)
                items(bookRows) { rowBooks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (book in rowBooks) {
                            Box(modifier = Modifier.weight(1f)) {
                                BookGridCard(
                                    book = book,
                                    onClick = { onNavigateToDetail(book.id) },
                                    onToggleFavorite = { viewModel.toggleFavorite(book) }
                                )
                            }
                        }
                        // If row has an odd number of items, insert spacer for balance
                        if (rowBooks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Render books in List View
                items(allBooks, key = { it.id }) { book ->
                    BookCardItem(
                        book = book,
                        onClick = { onNavigateToDetail(book.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(book) },
                        onShare = { PdfExporter.shareBookText(context, book) }
                    )
                }
            }
        }
    }
}

/**
 * Featured Hero section highlighting the book currently in "A Ler" status
 */
@Composable
private fun FeaturedReadingSection(
    activeBook: Book?,
    readingCount: Int,
    currentIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onContinueReading: (Book) -> Unit,
    onMarkAsRead: (Book) -> Unit,
    onPickBook: () -> Unit,
    onAddBook: () -> Unit
) {
    val context = LocalContext.current

    if (activeBook != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("featured_reading_book_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header badge + Navigation if multiple books are "A ler"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(WarmAmber, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "A LER AGORA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (readingCount > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentIndex + 1} de $readingCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Livro Anterior", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Próximo Livro", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Book Cover & Details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Large Book Cover
                    Box(
                        modifier = Modifier
                            .size(105.dp, 155.dp)
                            .shadow(6.dp, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onContinueReading(activeBook) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeBook.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(activeBook.coverUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Capa de ${activeBook.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeBook.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Book Details Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeBook.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = activeBook.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (activeBook.pageCount != null && activeBook.pageCount > 0) {
                            Text(
                                text = "${activeBook.pageCount} páginas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Rating preview
                        if (activeBook.rating > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= activeBook.rating) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = null,
                                        tint = if (star <= activeBook.rating) StarGold else MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Short review preview if present
                        if (activeBook.personalReview.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\"${activeBook.personalReview}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = FontStyle.Italic,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onContinueReading(activeBook) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_continue_reading"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Continuar", style = MaterialTheme.typography.labelMedium)
                            }

                            FilledTonalButton(
                                onClick = { onMarkAsRead(activeBook) },
                                modifier = Modifier.testTag("btn_mark_as_read"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lido", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // No book currently "A ler" -> Friendly inviting card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("no_reading_book_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bookworm_logo_1790265940738),
                    contentDescription = "Logo BookWorm",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nenhum livro em leitura no momento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escolha um livro da sua biblioteca para iniciar ou adicione um novo exemplar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPickBook,
                        modifier = Modifier.testTag("btn_pick_reading_book")
                    ) {
                        Icon(Icons.Default.LocalLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Iniciar da Biblioteca")
                    }
                    OutlinedButton(onClick = onAddBook) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Novo Livro")
                    }
                }
            }
        }
    }
}

/**
 * 4 Mini stats cards displaying reading count overview
 */
@Composable
private fun ReadingSnapshotRow(
    readingCount: Int,
    readCount: Int,
    wantToReadCount: Int,
    totalCount: Int,
    onNavigateToLibrary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBadgeCard(
            label = "A Ler",
            count = "$readingCount",
            accentColor = Color(0xFF2563EB),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToLibrary
        )
        StatBadgeCard(
            label = "Lidos",
            count = "$readCount",
            accentColor = Color(0xFF059669),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToLibrary
        )
        StatBadgeCard(
            label = "Quero Ler",
            count = "$wantToReadCount",
            accentColor = Color(0xFFD97706),
            modifier = Modifier.weight(1f),
            onClick = onNavigateToLibrary
        )
        StatBadgeCard(
            label = "Total",
            count = "$totalCount",
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToLibrary
        )
    }
}

@Composable
private fun StatBadgeCard(
    label: String,
    count: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * 4 Quick Action Cards for core features
 */
@Composable
private fun QuickActionHub(
    onScan: () -> Unit,
    onSearchPublic: () -> Unit,
    onCollections: () -> Unit,
    onStats: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            title = "Escanear",
            icon = Icons.Default.QrCodeScanner,
            tint = Color(0xFF2563EB),
            modifier = Modifier.weight(1f),
            onClick = onScan
        )
        QuickActionButton(
            title = "Buscar Online",
            icon = Icons.Default.Search,
            tint = Color(0xFF0D9488),
            modifier = Modifier.weight(1f),
            onClick = onSearchPublic
        )
        QuickActionButton(
            title = "Coleções",
            icon = Icons.Default.Bookmarks,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f),
            onClick = onCollections
        )
        QuickActionButton(
            title = "Estatísticas",
            icon = Icons.Default.BarChart,
            tint = WarmAmber,
            modifier = Modifier.weight(1f),
            onClick = onStats
        )
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Empty state for when the library is completely empty
 */
@Composable
private fun EmptyHomeState(
    onScan: () -> Unit,
    onSearchPublic: () -> Unit,
    onAddManual: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.bookworm_logo_1790265940738),
                contentDescription = "Logo BookWorm",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sua biblioteca BookWorm está vazia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cadastre seus livros favoritos usando a câmera para ler o código de barras, buscando em acervos públicos ou digitando os dados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onScan) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Escanear")
                }
                OutlinedButton(onClick = onSearchPublic) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buscar Online")
                }
            }
        }
    }
}
