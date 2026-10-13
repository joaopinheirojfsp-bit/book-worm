package com.example.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Book
import com.example.data.model.BookCollection
import com.example.data.model.ReadingStatus
import com.example.ui.BookViewModel
import com.example.ui.TargetListOption
import com.example.ui.components.BookCardItem
import com.example.ui.components.BookGridCard
import com.example.ui.components.BookWormTopLeftMenu
import com.example.ui.theme.StarGold
import com.example.ui.theme.WarmAmber
import com.example.util.PdfExporter

/**
 * Data item representing each tab in the horizontal sliding menu
 */
data class HomeTabItem(
    val id: String,
    val title: String,
    val count: Int?,
    val isTemporary: Boolean = false,
    val collection: BookCollection? = null
)

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
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val strings = remember(currentLanguage) { com.example.util.LanguageManager.getStrings(currentLanguage) }
    val allBooks by viewModel.allBooksRaw.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val crossRefs by viewModel.allCrossRefsRaw.collectAsState()

    // View mode toggle: list vs grid (persisted during recomposition)
    var isGridView by rememberSaveable { mutableStateOf(false) }

    // Currently selected tab ID
    var selectedTabId by rememberSaveable { mutableStateOf("minha_biblioteca") }

    // Dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddOptionsDialog by remember { mutableStateOf(false) }
    var showPickReadingBookDialog by remember { mutableStateOf(false) }
    var showCreateTabDialog by remember { mutableStateOf(false) }
    var showPersonalizeDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<BookCollection?>(null) }
    var deletingCollection by remember { mutableStateOf<BookCollection?>(null) }
    var isbnDialogBook by remember { mutableStateOf<Book?>(null) }
    var moveToListBook by remember { mutableStateOf<Book?>(null) }
    var deleteBookConfirm by remember { mutableStateOf<Book?>(null) }

    // 1. "A Minha Biblioteca": All books that are NOT strictly temporary
    val minhaBibliotecaBooks = remember(allBooks) {
        allBooks.filter { !it.isTemporary && it.readingStatus != ReadingStatus.QUERO_LER }
    }

    // 2. "Lidos"
    val readBooks = remember(allBooks) {
        allBooks.filter { it.readingStatus == ReadingStatus.LIDO }
    }

    // 3. "A Ler"
    val readingBooks = remember(allBooks) {
        allBooks.filter { it.readingStatus == ReadingStatus.LENDO }
    }

    // 4. "Quero Ler" (Temporary list by rule)
    val wantToReadBooks = remember(allBooks) {
        allBooks.filter { it.readingStatus == ReadingStatus.QUERO_LER || it.isTemporary }
    }

    var currentReadingIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeReadingBook = if (readingBooks.isNotEmpty()) {
        val safeIndex = currentReadingIndex.coerceIn(0, readingBooks.lastIndex)
        readingBooks[safeIndex]
    } else null

    // Build the dynamic tabs list:
    // Da barra deslizante deixa apenas "a minha biblioteca" "lidos" "a ler" e "personalizar"
    val tabs = remember(minhaBibliotecaBooks, readBooks, readingBooks, collections, strings) {
        listOf(
            HomeTabItem("minha_biblioteca", strings.tabMyLibrary, minhaBibliotecaBooks.size, isTemporary = false),
            HomeTabItem("lidos", strings.tabRead, readBooks.size, isTemporary = false),
            HomeTabItem("a_ler", strings.tabReading, readingBooks.size, isTemporary = false),
            HomeTabItem("personalizar", strings.tabCustomise, collections.size + 1, isTemporary = false)
        )
    }

    val selectedTabIndex = remember(tabs, selectedTabId) {
        val idx = tabs.indexOfFirst { it.id == selectedTabId }
        if (idx >= 0) idx else 0
    }

    val currentTab = tabs.getOrNull(selectedTabIndex) ?: tabs.first()

    // Filter books for the currently active tab
    val currentTabBooks: List<Book> = remember(selectedTabId, allBooks, crossRefs) {
        when (selectedTabId) {
            "minha_biblioteca" -> minhaBibliotecaBooks
            "lidos" -> readBooks
            "a_ler" -> readingBooks
            else -> emptyList()
        }
    }

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

    // Dialog: Criar Nova Aba / Personalizar Aba
    if (showCreateTabDialog || showPersonalizeDialog || editingCollection != null) {
        val isEditing = editingCollection != null
        var tabName by remember { mutableStateOf(editingCollection?.name ?: "") }
        var isTabTemporary by remember { mutableStateOf(editingCollection?.isTemporary ?: false) }
        var selectedColorHex by remember { mutableStateOf(editingCollection?.colorHex ?: "#1E3A5F") }
        var nameError by remember { mutableStateOf(false) }

        val presetColors = listOf(
            "#1E3A5F", "#D97706", "#0D9488", "#DC2626", "#7C3AED", "#DB2777", "#2563EB", "#059669"
        )

        AlertDialog(
            onDismissRequest = {
                showCreateTabDialog = false
                showPersonalizeDialog = false
                editingCollection = null
            },
            title = {
                Text(
                    if (isEditing) "Editar Aba / Lista"
                    else if (showPersonalizeDialog) "Nomear Aba Personalizada"
                    else "Criar Nova Aba"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = tabName,
                        onValueChange = {
                            tabName = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nome da Lista / Aba *") },
                        placeholder = { Text("Ex: Mangás, Clube do Livro, Emprestados") },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("O nome da lista não pode ficar em branco.") } } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tab_name")
                    )

                    // Rule selector: Lista Permanente vs Lista Temporária
                    Column {
                        Text(
                            text = "Tipo de Funcionamento da Lista:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Opção: Lista Permanente
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isTabTemporary) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (!isTabTemporary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTabTemporary = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = !isTabTemporary, onClick = { isTabTemporary = false })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Lista Permanente (Padrão)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Os livros desta lista são incluídos em 'A Minha Biblioteca' permanentemente.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opção: Lista Temporária
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTabTemporary) Color(0xFFFEF3C7).copy(alpha = 0.6f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isTabTemporary) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTabTemporary = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isTabTemporary, onClick = { isTabTemporary = true })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Lista Temporária", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "Os livros permanecem apenas nesta lista e só entrarão em 'A Minha Biblioteca' quando forem movidos para outra lista (como 'Quero Ler').",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Cor do marcador
                    Column {
                        Text(
                            text = "Cor da Aba:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetColors.forEach { colorStr ->
                                val color = Color(android.graphics.Color.parseColor(colorStr))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColorHex = colorStr }
                                        .border(
                                            width = if (selectedColorHex == colorStr) 3.dp else 0.dp,
                                            color = if (selectedColorHex == colorStr) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tabName.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        if (isEditing) {
                            val updated = editingCollection!!.copy(
                                name = tabName.trim(),
                                isTemporary = isTabTemporary,
                                colorHex = selectedColorHex
                            )
                            viewModel.updateCustomTab(updated)
                            Toast.makeText(context, "Aba '${tabName.trim()}' atualizada!", Toast.LENGTH_SHORT).show()
                            editingCollection = null
                        } else {
                            viewModel.createCustomTab(
                                name = tabName.trim(),
                                isTemporary = isTabTemporary,
                                colorHex = selectedColorHex
                            )
                            Toast.makeText(context, "Nova aba '${tabName.trim()}' criada com sucesso!", Toast.LENGTH_SHORT).show()
                            showCreateTabDialog = false
                            showPersonalizeDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_tab_button")
                ) {
                    Text(if (isEditing) "Salvar Alterações" else "Salvar e Ativar Aba")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateTabDialog = false
                        showPersonalizeDialog = false
                        editingCollection = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Ver Código ISBN
    if (isbnDialogBook != null) {
        val book = isbnDialogBook!!
        var manualIsbn by remember { mutableStateOf("") }
        var isEditingIsbn by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { isbnDialogBook = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Código ISBN")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(book.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (book.isbn.isNotBlank() && !isEditingIsbn) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                BarcodeVisualRepresentation(isbn = book.isbn)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ISBN: ${book.isbn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                        if (book.publisher.isNotBlank()) {
                            Text("Editora: ${book.publisher}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (book.isbn.isBlank()) "Este livro ainda não possui código ISBN cadastrado." else "Editar código ISBN:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = manualIsbn,
                                    onValueChange = { manualIsbn = it },
                                    label = { Text("Código ISBN") },
                                    placeholder = { Text("Ex: 9788535914849") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (manualIsbn.isNotBlank()) {
                                            viewModel.updateBookIsbn(book, manualIsbn)
                                            isEditingIsbn = false
                                            isbnDialogBook = null
                                            Toast.makeText(context, "ISBN cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Salvar ISBN")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (book.isbn.isNotBlank() && !isEditingIsbn) {
                    Row {
                        TextButton(onClick = {
                            manualIsbn = book.isbn
                            isEditingIsbn = true
                        }) {
                            Text("Editar")
                        }
                        Button(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ISBN", book.isbn)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Código ISBN copiado!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isbnDialogBook = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Dialog: Mover para Outra Lista
    if (moveToListBook != null) {
        val book = moveToListBook!!
        AlertDialog(
            onDismissRequest = { moveToListBook = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mover Livro")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecione a lista de destino para \"${book.title}\":",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (book.isTemporary || book.readingStatus == ReadingStatus.QUERO_LER) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Este livro está numa lista temporária. Ao movê-lo para 'A Minha Biblioteca', 'A Ler', 'Lidos' ou outra lista permanente, ele passará a integrar 'A Minha Biblioteca'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Destino: A Minha Biblioteca
                        item {
                            DestinationItemCard(
                                title = "A Minha Biblioteca",
                                subtitle = "Lista Permanente • Visível em todas as abas",
                                tag = "Permanente",
                                color = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    viewModel.moveBookToList(book, TargetListOption.MinhaBiblioteca) {
                                        Toast.makeText(context, "Movido para 'A Minha Biblioteca'!", Toast.LENGTH_SHORT).show()
                                        moveToListBook = null
                                    }
                                }
                            )
                        }

                        // 2. Destino: A Ler
                        item {
                            DestinationItemCard(
                                title = "A Ler",
                                subtitle = "Lista Permanente • Leitura em andamento",
                                tag = "Permanente",
                                color = Color(0xFF2563EB),
                                onClick = {
                                    viewModel.moveBookToList(book, TargetListOption.ALer) {
                                        Toast.makeText(context, "Movido para 'A Ler'!", Toast.LENGTH_SHORT).show()
                                        moveToListBook = null
                                    }
                                }
                            )
                        }

                        // 3. Destino: Lidos
                        item {
                            DestinationItemCard(
                                title = "Lidos",
                                subtitle = "Lista Permanente • Livro concluído",
                                tag = "Permanente",
                                color = Color(0xFF059669),
                                onClick = {
                                    viewModel.moveBookToList(book, TargetListOption.Lidos) {
                                        Toast.makeText(context, "Movido para 'Lidos'!", Toast.LENGTH_SHORT).show()
                                        moveToListBook = null
                                    }
                                }
                            )
                        }

                        // 4. Destino: Quero Ler
                        item {
                            DestinationItemCard(
                                title = "Quero Ler",
                                subtitle = "Lista Temporária • Desejos de leitura futura",
                                tag = "Temporária",
                                color = Color(0xFFD97706),
                                onClick = {
                                    viewModel.moveBookToList(book, TargetListOption.QueroLer) {
                                        Toast.makeText(context, "Movido para 'Quero Ler'!", Toast.LENGTH_SHORT).show()
                                        moveToListBook = null
                                    }
                                }
                            )
                        }

                        // Listas Personalizadas
                        items(collections) { col ->
                            DestinationItemCard(
                                title = col.name,
                                subtitle = if (col.isTemporary) "Lista Temporária personalizada" else "Lista Permanente personalizada",
                                tag = if (col.isTemporary) "Temporária" else "Permanente",
                                color = Color(android.graphics.Color.parseColor(col.colorHex.ifBlank { "#1E3A5F" })),
                                onClick = {
                                    viewModel.moveBookToList(book, TargetListOption.Custom(col)) {
                                        Toast.makeText(context, "Movido para '${col.name}'!", Toast.LENGTH_SHORT).show()
                                        moveToListBook = null
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { moveToListBook = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Remover Livro Permanentemente
    if (deleteBookConfirm != null) {
        val book = deleteBookConfirm!!
        AlertDialog(
            onDismissRequest = { deleteBookConfirm = null },
            title = { Text("Remover Livro Permanentemente") },
            text = {
                Text("Tem certeza de que deseja remover \"${book.title}\" permanentemente? Ele será excluído de 'A Minha Biblioteca' e de todas as abas e listas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBook(book)
                        deleteBookConfirm = null
                        Toast.makeText(context, "Livro removido permanentemente.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remover Definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteBookConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog: Excluir Aba Personalizada
    if (deletingCollection != null) {
        val col = deletingCollection!!
        AlertDialog(
            onDismissRequest = { deletingCollection = null },
            title = { Text("Excluir Aba '${col.name}'") },
            text = {
                Text("Deseja excluir a aba '${col.name}'? Os livros não serão apagados do seu catálogo, apenas desvinculados desta lista.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomTab(col)
                        deletingCollection = null
                        selectedTabId = "minha_biblioteca"
                        Toast.makeText(context, "Aba excluída.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Aba")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCollection = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 3-dots menu on TOP-LEFT corner with ALL app options + Criar Nova Aba
                    BookWormTopLeftMenu(
                        currentRoute = "home",
                        onNavigateToHome = { /* Already on Home */ },
                        onNavigateToLibrary = onNavigateToLibrary,
                        onNavigateToScan = onNavigateToScan,
                        onNavigateToPublicSearch = onNavigateToPublicSearch,
                        onNavigateToCollections = onNavigateToCollections,
                        onNavigateToStats = onNavigateToStats,
                        onNavigateToAddBook = { showAddOptionsDialog = true },
                        onExportPdf = { showExportDialog = true },
                        onCreateNewTab = { showCreateTabDialog = true }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // 2. QUICK ACTION HUB (Apenas "Escanear", "Pesquisar", "Estatísticas" - Sem Coleções)
            item {
                QuickActionHub(
                    onScan = onNavigateToScan,
                    onSearchPublic = onNavigateToPublicSearch,
                    onStats = onNavigateToStats
                )
            }

            // 3. SISTEMA DE ABAS / MENU DE DESLIZE HORIZONTAL
            // Ficando logo após as opções "Escanear", "Pesquisar" e "Estatísticas", substituindo os quadrados antigos
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth().testTag("horizontal_tabs_row")
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 8.dp,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTabIndex in tabPositions.indices) {
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = {
                                    selectedTabId = tab.id
                                    if (tab.id == "personalizar") {
                                        showPersonalizeDialog = true
                                    }
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        if (tab.id == "personalizar") {
                                            Icon(
                                                Icons.Default.Tune,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = tab.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (tab.count != null) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${tab.count}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (tab.isTemporary) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFFEF3C7),
                                                modifier = Modifier.padding(start = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Temp.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 4. CABEÇALHO DA ABA SELECIONADA & ALTERNADOR DE VISUALIZAÇÃO
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentTab.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentTab.collection != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { editingCollection = currentTab.collection },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Aba", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { deletingCollection = currentTab.collection },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir Aba", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        val subtitleText = when {
                            selectedTabId == "minha_biblioteca" ->
                                "${currentTabBooks.size} livros • Todos os livros das abas permanentes"
                            selectedTabId == "a_ler" ->
                                "${currentTabBooks.size} livros • Em leitura ativa"
                            selectedTabId == "lidos" ->
                                "${currentTabBooks.size} livros • Leituras concluídas"
                            selectedTabId == "quero_ler" ->
                                "${currentTabBooks.size} livros • Lista Temporária (entram na biblioteca quando movidos)"
                            selectedTabId.startsWith("custom_") ->
                                if (currentTab.isTemporary) "${currentTabBooks.size} livros • Lista Temporária"
                                else "${currentTabBooks.size} livros • Lista Permanente"
                            else -> "Gerenciador de Abas e Listas"
                        }

                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedTabId != "personalizar") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Alternador Lista vs Grelha
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.testTag("home_view_mode_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Vista de Lista
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

                                    // Vista de Grelha
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
                        }
                    }
                }
            }

            // 5. CONTEÚDO DA ABA SELECIONADA
            if (selectedTabId == "personalizar") {
                item {
                    PersonalizeTabsView(
                        collections = collections,
                        allBooks = allBooks,
                        crossRefs = crossRefs,
                        strings = strings,
                        isGridView = isGridView,
                        onNameNewTab = { showPersonalizeDialog = true },
                        onEditCollection = { col -> editingCollection = col },
                        onDeleteCollection = { col -> deletingCollection = col },
                        onViewIsbn = { b -> isbnDialogBook = b },
                        onMoveToList = { b -> moveToListBook = b },
                        onEditBook = { b -> onNavigateToDetail(b.id) },
                        onDeleteBook = { b -> deleteBookConfirm = b },
                        onToggleFavorite = { b -> viewModel.toggleFavorite(b) },
                        onScan = onNavigateToScan,
                        onSearchPublic = onNavigateToPublicSearch,
                        onAddManual = onNavigateToAddManual
                    )
                }
            } else if (currentTabBooks.isEmpty()) {
                item {
                    EmptyTabState(
                        tabTitle = currentTab.title,
                        isTemporary = currentTab.isTemporary,
                        onScan = onNavigateToScan,
                        onSearchPublic = onNavigateToPublicSearch,
                        onAddManual = onNavigateToAddManual
                    )
                }
            } else if (isGridView) {
                // Vista de Grelha (2 Colunas) com menu de 3 pontinhos em todos os livros
                val bookRows = currentTabBooks.chunked(2)
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
                                    onToggleFavorite = { viewModel.toggleFavorite(book) },
                                    onViewIsbn = { b -> isbnDialogBook = b },
                                    onMoveToList = { b -> moveToListBook = b },
                                    onEditBook = { b -> onNavigateToDetail(b.id) },
                                    onDeleteBook = { b -> deleteBookConfirm = b }
                                )
                            }
                        }
                        if (rowBooks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Vista de Lista com menu de 3 pontinhos em todos os livros
                items(currentTabBooks, key = { it.id }) { book ->
                    BookCardItem(
                        book = book,
                        onClick = { onNavigateToDetail(book.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(book) },
                        onShare = { PdfExporter.shareBookText(context, book) },
                        onViewIsbn = { b -> isbnDialogBook = b },
                        onMoveToList = { b -> moveToListBook = b },
                        onEditBook = { b -> onNavigateToDetail(b.id) },
                        onDeleteBook = { b -> deleteBookConfirm = b }
                    )
                }
            }
        }
    }
}

/**
 * Visual barcode representation drawing vertical bars of varying thickness
 */
@Composable
fun BarcodeVisualRepresentation(isbn: String, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 16.dp)
    ) {
        val width = size.width
        val height = size.height
        val cleanIsbn = isbn.replace("-", "").ifBlank { "9788535914849" }
        val numBars = (cleanIsbn.length * 4).coerceAtLeast(36)
        val step = width / numBars

        var currentX = 0f
        for (i in 0 until numBars) {
            val charCode = cleanIsbn[i % cleanIsbn.length].code
            val isThick = (charCode + i) % 3 == 0
            val strokeWidth = if (isThick) step * 0.75f else step * 0.35f
            val isDrawn = (charCode + i * 2) % 5 != 0

            if (isDrawn) {
                drawLine(
                    color = Color.Black,
                    start = Offset(currentX + strokeWidth / 2, 0f),
                    end = Offset(currentX + strokeWidth / 2, height),
                    strokeWidth = strokeWidth
                )
            }
            currentX += step
        }
    }
}

/**
 * Destination selection card when moving a book between lists
 */
@Composable
fun DestinationItemCard(
    title: String,
    subtitle: String,
    tag: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (tag == "Temporária") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tag == "Temporária") Color(0xFF92400E) else Color(0xFF065F46),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Dedicated view for the "Personalizar" tab:
 * Allows creating/naming custom lists, selecting any list (including "Quero Ler" and custom lists),
 * and viewing its books while enforcing the rule:
 * "Na aba 'quero ler' e todas as abas temporárias, a opção deve ser sempre não lido. Essa opção só é alterável quando se muda para lista permanente."
 */
@Composable
private fun PersonalizeTabsView(
    collections: List<BookCollection>,
    allBooks: List<Book>,
    crossRefs: List<com.example.data.model.BookCollectionCrossRef>,
    strings: com.example.util.AppStrings,
    isGridView: Boolean,
    onNameNewTab: () -> Unit,
    onEditCollection: (BookCollection) -> Unit,
    onDeleteCollection: (BookCollection) -> Unit,
    onViewIsbn: (Book) -> Unit,
    onMoveToList: (Book) -> Unit,
    onEditBook: (Book) -> Unit,
    onDeleteBook: (Book) -> Unit,
    onToggleFavorite: (Book) -> Unit,
    onScan: () -> Unit,
    onSearchPublic: () -> Unit,
    onAddManual: () -> Unit
) {
    var selectedListId by rememberSaveable { mutableStateOf("quero_ler") }

    // Quero Ler books
    val wantToReadBooks = remember(allBooks) {
        allBooks.filter { it.readingStatus == ReadingStatus.QUERO_LER || it.isTemporary }
    }

    // Active collection if one is selected
    val activeCollection = remember(selectedListId, collections) {
        if (selectedListId.startsWith("custom_")) {
            val colId = selectedListId.removePrefix("custom_").toLongOrNull()
            collections.find { it.id == colId }
        } else null
    }

    // Books for the currently active selected list inside Personalizar
    val activeListBooks = remember(selectedListId, allBooks, crossRefs) {
        if (selectedListId == "quero_ler") {
            wantToReadBooks
        } else if (selectedListId.startsWith("custom_")) {
            val colId = selectedListId.removePrefix("custom_").toLongOrNull()
            if (colId != null) {
                val bookIds = crossRefs.filter { it.collectionId == colId }.map { it.bookId }.toSet()
                allBooks.filter { it.id in bookIds }
            } else emptyList()
        } else emptyList()
    }

    val isCurrentListTemporary = selectedListId == "quero_ler" || (activeCollection?.isTemporary == true)

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. Welcome and creation header card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.customiseTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.customiseDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• ${strings.permanentOptionTitle}: ${strings.permanentOptionDesc}\n• ${strings.temporaryOptionTitle}: ${strings.temporaryOptionDesc}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onNameNewTab,
                    modifier = Modifier.fillMaxWidth().testTag("create_custom_list_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.createListButton)
                }
            }
        }

        // 2. Selector of Personalized Lists (Quero Ler + Custom Lists)
        Text(
            text = "${strings.yourCustomLists} (${collections.size + 1}):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Built-in list: Quero Ler (Temporary)
            item {
                val isSelected = selectedListId == "quero_ler"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedListId = "quero_ler" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFD97706), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quero Ler",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${wantToReadBooks.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = strings.temporaryBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Custom user collections
            items(collections) { col ->
                val isSelected = selectedListId == "custom_${col.id}"
                val colBookIds = crossRefs.filter { it.collectionId == col.id }.map { it.bookId }.toSet()
                val count = allBooks.count { it.id in colBookIds }
                val colColor = try {
                    Color(android.graphics.Color.parseColor(col.colorHex.ifBlank { "#1E3A5F" }))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable { selectedListId = "custom_${col.id}" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(colColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = col.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (col.isTemporary) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (col.isTemporary) strings.temporaryBadge else "Perm.",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (col.isTemporary) Color(0xFF92400E) else Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Information Card for Active Selected List
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val dotColor = if (selectedListId == "quero_ler") Color(0xFFD97706)
                        else try {
                            Color(android.graphics.Color.parseColor(activeCollection?.colorHex ?: "#1E3A5F"))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(dotColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedListId == "quero_ler") "Quero Ler" else (activeCollection?.name ?: "Lista Personalizada"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCurrentListTemporary) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (isCurrentListTemporary) strings.temporaryList else strings.permanentList,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentListTemporary) Color(0xFF92400E) else Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (activeCollection != null) {
                        Row {
                            IconButton(onClick = { onEditCollection(activeCollection) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = strings.editList, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteCollection(activeCollection) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = strings.deleteList, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Enforced temporary list rule banner
                if (isCurrentListTemporary) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7).copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔒 ${strings.temporaryStatusLockNotice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD1FAE5).copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFF059669)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ ${strings.permanentOptionDesc}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // 4. Books inside the Selected List
        if (activeListBooks.isEmpty()) {
            EmptyTabState(
                tabTitle = if (selectedListId == "quero_ler") "Quero Ler" else (activeCollection?.name ?: "Lista"),
                isTemporary = isCurrentListTemporary,
                onScan = onScan,
                onSearchPublic = onSearchPublic,
                onAddManual = onAddManual
            )
        } else if (isGridView) {
            val bookRows = activeListBooks.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (rowBooks in bookRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (book in rowBooks) {
                            Box(modifier = Modifier.weight(1f)) {
                                BookGridCard(
                                    book = book,
                                    onClick = { onEditBook(book) },
                                    onToggleFavorite = { onToggleFavorite(book) },
                                    onViewIsbn = onViewIsbn,
                                    onMoveToList = onMoveToList,
                                    onEditBook = onEditBook,
                                    onDeleteBook = onDeleteBook
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activeListBooks.forEach { book ->
                    BookCardItem(
                        book = book,
                        onClick = { onEditBook(book) },
                        onToggleFavorite = { onToggleFavorite(book) },
                        onViewIsbn = onViewIsbn,
                        onMoveToList = onMoveToList,
                        onEditBook = onEditBook,
                        onDeleteBook = onDeleteBook
                    )
                }
            }
        }
    }
}

/**
 * Empty state for when a tab has no books
 */
@Composable
private fun EmptyTabState(
    tabTitle: String,
    isTemporary: Boolean,
    onScan: () -> Unit,
    onSearchPublic: () -> Unit,
    onAddManual: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Nenhum livro na aba '$tabTitle'",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isTemporary)
                    "Esta é uma lista temporária. Você pode mover livros para cá ou cadastrar novos títulos."
                else
                    "Adicione livros com a câmera de código de barras ou use o menu de 3 pontinhos em qualquer livro para movê-lo para esta aba.",
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

/**
 * Quick Action Hub: Exactly 3 options (Escanear, Pesquisar, Estatísticas)
 * "Coleções" was eliminated from these options as requested.
 */
@Composable
private fun QuickActionHub(
    onScan: () -> Unit,
    onSearchPublic: () -> Unit,
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
            title = "Pesquisar",
            icon = Icons.Default.Search,
            tint = Color(0xFF0D9488),
            modifier = Modifier.weight(1f),
            onClick = onSearchPublic
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
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
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
                .testTag("featured_reading_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header of Hero: Status badge + Reading switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFDBEAFE)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF2563EB), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "A Ler Atualmente",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    if (readingCount > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Livro anterior",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${currentIndex + 1}/$readingCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Próximo livro",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Book Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp, 122.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
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
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeBook.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Nenhum livro em leitura no momento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Escolha um exemplar do seu catálogo para começar a ler ou cadastre um novo título agora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPickBook,
                        modifier = Modifier.testTag("btn_pick_reading_book")
                    ) {
                        Text("Escolher do Catálogo")
                    }

                    OutlinedButton(
                        onClick = onAddBook,
                        modifier = Modifier.testTag("btn_add_first_book")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cadastrar")
                    }
                }
            }
        }
    }
}
