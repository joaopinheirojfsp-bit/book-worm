package com.example.ui.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Book
import com.example.data.model.ReadingStatus
import com.example.ui.BookViewModel
import com.example.ui.SortOption
import com.example.ui.components.BookCardItem
import com.example.ui.components.BookGridCard
import com.example.ui.components.BookWormTopLeftMenu
import com.example.ui.theme.StarGold
import com.example.ui.theme.WarmAmber
import com.example.util.PdfExporter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    viewModel: BookViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPublicSearch: () -> Unit,
    onNavigateToAddManual: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val context = LocalContext.current
    val books by viewModel.books.collectAsState()
    val filterCriteria by viewModel.filterCriteria.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val collections by viewModel.collections.collectAsState()

    // View mode toggle: List View vs Grid View
    var isGridView by rememberSaveable { mutableStateOf(false) }

    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddOptionsDialog by remember { mutableStateOf(false) }

    // Export PDF Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar Biblioteca") },
            text = {
                Text("Deseja gerar um arquivo PDF com layout formatado ou compartilhar os dados como texto com amigos?")
            },
            confirmButton = {
                Button(onClick = {
                    showExportDialog = false
                    PdfExporter.exportCatalogPdf(context, books)
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
                        PdfExporter.shareCatalogText(context, books)
                    }) {
                        Text("Texto")
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
            title = { Text("Como deseja adicionar o livro?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        Icon(Icons.Default.LocalLibrary, contentDescription = null)
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

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 3-dots menu in the top-left corner with ALL options of the app
                    BookWormTopLeftMenu(
                        currentRoute = "catalog",
                        onNavigateToHome = onNavigateToHome,
                        onNavigateToLibrary = { /* Already in catalog */ },
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A Minha Biblioteca",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // View Mode Toggle (Lista ↔ Grelha)
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier.testTag("catalog_view_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Mudar para Lista" else "Mudar para Grelha",
                            tint = if (isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Collections button
                    IconButton(
                        onClick = onNavigateToCollections,
                        modifier = Modifier.testTag("nav_collections_button")
                    ) {
                        Icon(
                            Icons.Default.Bookmarks,
                            contentDescription = "Coleções",
                            tint = if (filterCriteria.selectedCollectionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Statistics button
                    IconButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier.testTag("nav_stats_button")
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Estatísticas & Relatórios"
                        )
                    }

                    // Export PDF button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_catalog_button")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sort button with dropdown
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Ordenar")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Toggle Advanced Filters
                    IconButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        modifier = Modifier.testTag("toggle_filters_button")
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtros Detalhados",
                            tint = if (showAdvancedFilters || filterCriteria.author.isNotBlank() || filterCriteria.publisher.isNotBlank() || filterCriteria.year.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurface
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
                modifier = Modifier.testTag("fab_add_book")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Active collection filter indicator banner
            if (filterCriteria.selectedCollectionId != null) {
                val activeCol = collections.find { it.id == filterCriteria.selectedCollectionId }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmarks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Coleção: ${activeCol?.name ?: "Filtrada"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setCollectionFilter(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar Filtro de Coleção", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Active tag filter indicator banner
            if (filterCriteria.selectedTag != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tag: #${filterCriteria.selectedTag}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(
                            onClick = { viewModel.setTagFilter(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar Filtro de Tag", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Universal Search Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = filterCriteria.query,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_field"),
                        placeholder = { Text("Pesquisar título, autor, editora, ano...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                        },
                        trailingIcon = {
                            if (filterCriteria.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Expandable Advanced Filters (Author, Publisher, Year, Favorites)
                    AnimatedVisibility(
                        visible = showAdvancedFilters,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = filterCriteria.author,
                                    onValueChange = { viewModel.setAuthorFilter(it) },
                                    label = { Text("Autor") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("filter_author_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = filterCriteria.publisher,
                                    onValueChange = { viewModel.setPublisherFilter(it) },
                                    label = { Text("Editora") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("filter_publisher_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = filterCriteria.year,
                                    onValueChange = { viewModel.setYearFilter(it) },
                                    label = { Text("Ano") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("filter_year_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                FilterChip(
                                    selected = filterCriteria.onlyFavorites,
                                    onClick = { viewModel.toggleOnlyFavorites() },
                                    label = { Text("Favoritos") },
                                    leadingIcon = {
                                        Icon(
                                            if (filterCriteria.onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (filterCriteria.onlyFavorites) Color.Red else MaterialTheme.colorScheme.outline
                                        )
                                    },
                                    modifier = Modifier.testTag("filter_favorites_chip")
                                )

                                if (filterCriteria.author.isNotBlank() || filterCriteria.publisher.isNotBlank() || filterCriteria.year.isNotBlank() || filterCriteria.onlyFavorites) {
                                    TextButton(onClick = { viewModel.clearAdvancedFilters() }) {
                                        Text("Limpar")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reading Status filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = filterCriteria.status == null,
                                onClick = { viewModel.setStatusFilter(null) },
                                label = { Text("Todos") },
                                modifier = Modifier.testTag("filter_chip_all")
                            )
                        }
                        items(ReadingStatus.values()) { status ->
                            FilterChip(
                                selected = filterCriteria.status == status,
                                onClick = {
                                    viewModel.setStatusFilter(if (filterCriteria.status == status) null else status)
                                },
                                label = { Text(status.label) },
                                modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                            )
                        }
                    }

                    // Custom Tags row (if catalog has tags)
                    if (allTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tags:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allTags) { tag ->
                                    val isSelected = filterCriteria.selectedTag?.lowercase() == tag.lowercase()
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setTagFilter(if (isSelected) null else tag)
                                        },
                                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stats mini bar
            val totalBooks = books.size
            val readCount = books.count { it.readingStatus == ReadingStatus.LIDO }
            val ratedBooks = books.filter { it.rating > 0 }
            val avgRating = if (ratedBooks.isNotEmpty()) ratedBooks.map { it.rating }.average() else 0.0

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exemplares: $totalBooks  |  Lidos: $readCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (avgRating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", avgRating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Book List / Grid or Empty State
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (filterCriteria.query.isNotEmpty() || filterCriteria.author.isNotEmpty() || filterCriteria.selectedTag != null || filterCriteria.selectedCollectionId != null) {
                                "Nenhum livro encontrado para estes filtros."
                            } else {
                                "A sua biblioteca está vazia."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Escaneie códigos de barras com a câmera, busque em bibliotecas públicas ou adicione manualmente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onNavigateToScan) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Escanear")
                            }
                            OutlinedButton(onClick = onNavigateToPublicSearch) {
                                Icon(Icons.Default.LocalLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buscar Online")
                            }
                        }
                    }
                }
            } else if (isGridView) {
                // Vista de Grelha (2 Colunas)
                val bookRows = books.chunked(2)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            if (rowBooks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // Vista de Lista (Padrão)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books, key = { it.id }) { book ->
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
}
