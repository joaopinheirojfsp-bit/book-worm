package com.example.ui.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BookCollection
import com.example.ui.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit,
    onSelectCollection: (Long) -> Unit
) {
    val collections by viewModel.collections.collectAsState()
    val allBooks by viewModel.allBooksRaw.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCollection by remember { mutableStateOf<BookCollection?>(null) }
    var deletingCollection by remember { mutableStateOf<BookCollection?>(null) }

    // Dialog for creating or editing a collection
    if (showCreateDialog || editingCollection != null) {
        val isEditing = editingCollection != null
        var name by remember { mutableStateOf(editingCollection?.name ?: "") }
        var description by remember { mutableStateOf(editingCollection?.description ?: "") }
        var selectedColor by remember { mutableStateOf(editingCollection?.colorHex ?: "#1E3A5F") }
        var nameError by remember { mutableStateOf(false) }

        val presetColors = listOf(
            "#1E3A5F", "#D97706", "#0D9488", "#DC2626", "#7C3AED", "#DB2777", "#2563EB", "#059669"
        )

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                editingCollection = null
            },
            title = { Text(if (isEditing) "Editar Coleção" else "Nova Coleção de Livros") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Nome da Coleção *") },
                        placeholder = { Text("Ex: Ficção Científica, Clube do Livro...") },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("O nome é obrigatório") } } else null,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collection_name_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição / Objetivo (Opcional)") },
                        placeholder = { Text("Ex: Livros para ler durante as férias...") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("collection_desc_input")
                    )

                    Text(
                        text = "Cor de Destaque:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        presetColors.forEach { colorHex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (e: Exception) {
                                Color(0xFF1E3A5F)
                            }
                            val isSelected = selectedColor == colorHex

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = colorHex }
                                    .then(
                                        if (isSelected) {
                                            Modifier.padding(2.dp)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        if (isEditing) {
                            viewModel.updateCollection(
                                editingCollection!!.copy(
                                    name = name.trim(),
                                    description = description.trim(),
                                    colorHex = selectedColor
                                )
                            )
                        } else {
                            viewModel.createCollection(
                                name = name.trim(),
                                description = description.trim(),
                                colorHex = selectedColor
                            )
                        }
                        showCreateDialog = false
                        editingCollection = null
                    },
                    modifier = Modifier.testTag("save_collection_button")
                ) {
                    Text(if (isEditing) "Salvar" else "Criar Coleção")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    editingCollection = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (deletingCollection != null) {
        val col = deletingCollection!!
        AlertDialog(
            onDismissRequest = { deletingCollection = null },
            title = { Text("Excluir Coleção?") },
            text = { Text("Tem certeza que deseja remover a coleção \"${col.name}\"? Os livros permanecerão no seu catálogo geral.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCollection(col)
                        deletingCollection = null
                    },
                    modifier = Modifier.testTag("confirm_delete_collection_button")
                ) {
                    Text("Excluir")
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
                title = { Text("Minhas Coleções") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Coleção") },
                modifier = Modifier.testTag("fab_add_collection")
            )
        }
    ) { paddingValues ->
        if (collections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bookmarks,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma coleção criada ainda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Agrupe seus livros em coleções personalizadas como \"Favoritos\", \"Clube do Livro\" ou \"Meta de Leitura\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("create_first_collection_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Criar Primeira Coleção")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(collections, key = { it.id }) { collection ->
                    CollectionCardItem(
                        collection = collection,
                        onClick = {
                            viewModel.setCollectionFilter(collection.id)
                            onSelectCollection(collection.id)
                        },
                        onEdit = { editingCollection = collection },
                        onDelete = { deletingCollection = collection }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionCardItem(
    collection: BookCollection,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tagColor = try {
        Color(android.graphics.Color.parseColor(collection.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("collection_card_${collection.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tagColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (collection.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toque para filtrar no catálogo",
                    style = MaterialTheme.typography.labelSmall,
                    color = tagColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_col_${collection.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_col_${collection.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
