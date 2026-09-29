package com.example.ui.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Book
import com.example.data.model.ReadingStatus
import com.example.data.remote.PublicBookResult
import com.example.ui.BookViewModel
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditBookScreen(
    viewModel: BookViewModel,
    bookId: Long?,
    initialResult: PublicBookResult?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialResult?.title ?: "") }
    var author by remember { mutableStateOf(initialResult?.authors ?: "") }
    var publisher by remember { mutableStateOf(initialResult?.publisher ?: "") }
    var publishYearStr by remember { mutableStateOf(initialResult?.publishYear?.toString() ?: "") }
    var isbn by remember { mutableStateOf(initialResult?.isbn ?: "") }
    var description by remember { mutableStateOf(initialResult?.description ?: "") }
    var pageCountStr by remember { mutableStateOf(initialResult?.pageCount?.toString() ?: "") }
    var coverUrl by remember { mutableStateOf(initialResult?.coverUrl ?: "") }
    var rating by remember { mutableIntStateOf(0) }
    var personalReview by remember { mutableStateOf("") }
    var readingStatus by remember { mutableStateOf(ReadingStatus.QUERO_LER) }
    var isFavorite by remember { mutableStateOf(false) }

    // Custom Tags
    var tagsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var newTagInput by remember { mutableStateOf("") }
    val allCatalogTags by viewModel.allTags.collectAsState()

    // Collections
    val allCollections by viewModel.collections.collectAsState()
    var selectedCollectionIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    // Cover tab: 0 = Link da Web, 1 = Upload de Imagem
    var coverTab by remember { mutableIntStateOf(if (coverUrl.startsWith("content://") || coverUrl.startsWith("file://")) 1 else 0) }
    var webUrlInput by remember { mutableStateOf(if (coverUrl.startsWith("http")) coverUrl else "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUrl = uri.toString()
        }
    }

    // Load existing book if editing
    LaunchedEffect(bookId) {
        if (bookId != null && bookId > 0) {
            viewModel.getBookById(bookId).collect { existingBook ->
                if (existingBook != null) {
                    title = existingBook.title
                    author = existingBook.author
                    publisher = existingBook.publisher
                    publishYearStr = existingBook.publishYear?.toString() ?: ""
                    isbn = existingBook.isbn
                    description = existingBook.description
                    pageCountStr = existingBook.pageCount?.toString() ?: ""
                    coverUrl = existingBook.coverUrl
                    rating = existingBook.rating
                    personalReview = existingBook.personalReview
                    readingStatus = existingBook.readingStatus
                    isFavorite = existingBook.isFavorite
                    tagsList = existingBook.getTagsList()
                    if (existingBook.coverUrl.startsWith("http")) {
                        webUrlInput = existingBook.coverUrl
                        coverTab = 0
                    } else if (existingBook.coverUrl.isNotBlank()) {
                        coverTab = 1
                    }
                }
            }
        }
    }

    // Load existing collections for book
    LaunchedEffect(bookId) {
        if (bookId != null && bookId > 0) {
            viewModel.getCollectionsForBook(bookId).collect { cols ->
                selectedCollectionIds = cols.map { it.id }.toSet()
            }
        }
    }

    var titleError by remember { mutableStateOf(false) }
    var authorError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (bookId != null && bookId > 0) "Editar Exemplar" else "Catalogar Livro")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.testTag("favorite_toggle_button")
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Cover Image (Capa Personalizada)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Capa do Livro (Personalizada)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cover preview box
                    Box(
                        modifier = Modifier
                            .size(130.dp, 190.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .testTag("book_cover_preview"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(coverUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Capa do livro",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { coverUrl = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color(0x99000000), RoundedCornerShape(bottomStart = 8.dp))
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remover capa",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sem Capa",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab to choose Web Link or File Upload
                    TabRow(
                        selectedTabIndex = coverTab,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = coverTab == 0,
                            onClick = { coverTab = 0 },
                            text = { Text("Link da Web") },
                            icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = coverTab == 1,
                            onClick = { coverTab = 1 },
                            text = { Text("Upload Imagem") },
                            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (coverTab == 0) {
                        // Web Link input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = webUrlInput,
                                onValueChange = { webUrlInput = it },
                                label = { Text("URL da Imagem da Web") },
                                placeholder = { Text("https://exemplo.com/capa.jpg") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cover_url_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { coverUrl = webUrlInput.trim() },
                                enabled = webUrlInput.isNotBlank(),
                                modifier = Modifier.testTag("apply_cover_url_button")
                            ) {
                                Text("Aplicar")
                            }
                        }
                    } else {
                        // Upload file from device
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Escolha uma imagem da sua galeria ou arquivos do celular:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.testTag("pick_cover_from_gallery_button")
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Selecionar Foto da Galeria")
                            }
                        }
                    }
                }
            }

            // Section: Informações do Livro
            Text(
                text = "Dados do Livro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("Título do Livro *") },
                isError = titleError,
                supportingText = if (titleError) { { Text("Título é obrigatório") } } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_title_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = author,
                onValueChange = {
                    author = it
                    if (it.isNotBlank()) authorError = false
                },
                label = { Text("Autor(es) *") },
                isError = authorError,
                supportingText = if (authorError) { { Text("Autor é obrigatório") } } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_author_input"),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Editora") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("book_publisher_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = publishYearStr,
                    onValueChange = { publishYearStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Ano") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("book_year_input"),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN / Código de Barras") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("book_isbn_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pageCountStr,
                    onValueChange = { pageCountStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Páginas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("book_pages_input"),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Sinopse / Descrição") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_description_input"),
                minLines = 3,
                maxLines = 5
            )

            // Section: Custom Tags (Múltiplas tags personalizadas)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tags Personalizadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Display assigned tags
                    if (tagsList.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tagsList.forEach { tag ->
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        tagsList = tagsList.filter { it != tag }
                                    },
                                    label = { Text(tag) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Remover tag", modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Nenhuma tag atribuída. Adicione tags como 'Ficção Científica', 'Kindle', 'Clássico' ou crie as suas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Add new tag row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTagInput,
                            onValueChange = { newTagInput = it },
                            placeholder = { Text("Nova tag...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = newTagInput.trim()
                                if (trimmed.isNotBlank() && !tagsList.contains(trimmed)) {
                                    tagsList = tagsList + trimmed
                                    newTagInput = ""
                                }
                            },
                            enabled = newTagInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Tag")
                        }
                    }

                    // Quick suggestion chips from existing tags
                    val suggestions = allCatalogTags.filter { !tagsList.contains(it) }.take(6)
                    if (suggestions.isNotEmpty()) {
                        Text(
                            text = "Sugestões rápidas do seu acervo:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            suggestions.forEach { tag ->
                                SuggestionChip(
                                    onClick = {
                                        if (!tagsList.contains(tag)) {
                                            tagsList = tagsList + tag
                                        }
                                    },
                                    label = { Text("+ $tag", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            // Section: Coleções (User-defined collections)
            if (allCollections.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmarks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coleções",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Selecione as coleções para agrupar este exemplar:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            allCollections.forEach { col ->
                                val isSelected = selectedCollectionIds.contains(col.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCollectionIds = if (isSelected) {
                                            selectedCollectionIds - col.id
                                        } else {
                                            selectedCollectionIds + col.id
                                        }
                                    },
                                    label = { Text(col.name) },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section: Status de Leitura
            Text(
                text = "Status de Leitura",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReadingStatus.values().forEach { status ->
                    FilterChip(
                        selected = readingStatus == status,
                        onClick = { readingStatus = status },
                        label = { Text(status.label, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.testTag("status_chip_${status.name.lowercase()}")
                    )
                }
            }

            // Section: Avaliação Pessoal e Comentários Curto
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sua Avaliação Pessoal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Interactive Star Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nota:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "$star estrelas",
                                tint = if (star <= rating) StarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        rating = if (rating == star) 0 else star
                                    }
                                    .testTag("star_rating_$star")
                            )
                        }
                        if (rating > 0) {
                            Text(
                                text = "$rating/5",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Short review / comment
                    OutlinedTextField(
                        value = personalReview,
                        onValueChange = { personalReview = it },
                        label = { Text("Comentário Curto / Impressão Pessoal") },
                        placeholder = { Text("Ex: Leitura envolvente, final surpreendente! Personagens memoráveis...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("personal_review_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (author.isBlank()) {
                        authorError = true
                        return@Button
                    }

                    val tagsFormatted = tagsList.joinToString(", ")

                    val book = Book(
                        id = bookId ?: 0,
                        title = title.trim(),
                        author = author.trim(),
                        publisher = publisher.trim(),
                        publishYear = publishYearStr.toIntOrNull(),
                        isbn = isbn.trim(),
                        description = description.trim(),
                        pageCount = pageCountStr.toIntOrNull(),
                        coverUrl = coverUrl.trim(),
                        rating = rating,
                        personalReview = personalReview.trim(),
                        readingStatus = readingStatus,
                        isFavorite = isFavorite,
                        dateFinished = if (readingStatus == ReadingStatus.LIDO) System.currentTimeMillis() else null,
                        tags = tagsFormatted
                    )

                    if (bookId != null && bookId > 0) {
                        viewModel.updateBook(book, selectedCollectionIds)
                    } else {
                        viewModel.addBook(book, selectedCollectionIds)
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_book_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (bookId != null && bookId > 0) "Salvar Alterações" else "Adicionar ao Catálogo",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
