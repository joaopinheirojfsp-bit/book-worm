package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.WarmAmber

@Composable
fun BookWormTopLeftMenu(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPublicSearch: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("top_left_menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu Principal BookWorm",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(290.dp)
                .testTag("app_overflow_dropdown_menu")
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Opções do Aplicativo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // 1. Menu Inicial
            BookWormMenuItem(
                icon = Icons.Default.Home,
                title = "Menu Inicial",
                subtitle = "Livro a ler e destaques",
                isSelected = currentRoute == "home",
                testTag = "menu_item_home",
                onClick = {
                    expanded = false
                    onNavigateToHome()
                }
            )

            // 2. A Minha Biblioteca
            BookWormMenuItem(
                icon = Icons.Default.LocalLibrary,
                title = "A Minha Biblioteca",
                subtitle = "Todos os meus livros e filtros",
                isSelected = currentRoute == "catalog",
                testTag = "menu_item_library",
                onClick = {
                    expanded = false
                    onNavigateToLibrary()
                }
            )

            // 3. Leitor de Código de Barras
            BookWormMenuItem(
                icon = Icons.Default.QrCodeScanner,
                title = "Leitor de Código de Barras",
                subtitle = "Escanear ISBN com câmera",
                isSelected = currentRoute == "scanner",
                testTag = "menu_item_scan",
                onClick = {
                    expanded = false
                    onNavigateToScan()
                }
            )

            // 4. Buscar em Bibliotecas Públicas
            BookWormMenuItem(
                icon = Icons.Default.Search,
                title = "Bibliotecas Públicas",
                subtitle = "Open Library & Google Books",
                isSelected = currentRoute == "public_search",
                testTag = "menu_item_search",
                onClick = {
                    expanded = false
                    onNavigateToPublicSearch()
                }
            )

            // 5. Coleções & Tags
            BookWormMenuItem(
                icon = Icons.Default.Bookmarks,
                title = "Coleções & Tags",
                subtitle = "Organizar suas estantes",
                isSelected = currentRoute == "collections",
                testTag = "menu_item_collections",
                onClick = {
                    expanded = false
                    onNavigateToCollections()
                }
            )

            // 6. Estatísticas & Relatórios
            BookWormMenuItem(
                icon = Icons.Default.BarChart,
                title = "Estatísticas & Relatórios",
                subtitle = "Gráficos, metas e resumos",
                isSelected = currentRoute == "statistics",
                testTag = "menu_item_stats",
                onClick = {
                    expanded = false
                    onNavigateToStats()
                }
            )

            Divider()

            // 7. Adicionar Novo Livro
            BookWormMenuItem(
                icon = Icons.Default.Add,
                title = "Adicionar Livro",
                subtitle = "Novo cadastro no catálogo",
                isSelected = false,
                testTag = "menu_item_add_book",
                onClick = {
                    expanded = false
                    onNavigateToAddBook()
                }
            )

            // 8. Exportar em PDF
            BookWormMenuItem(
                icon = Icons.Default.PictureAsPdf,
                title = "Exportar Biblioteca (PDF)",
                subtitle = "Gerar documento ou compartilhar",
                isSelected = false,
                testTag = "menu_item_export_pdf",
                onClick = {
                    expanded = false
                    onExportPdf()
                }
            )
        }
    }
}

@Composable
private fun BookWormMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        onClick = onClick,
        modifier = Modifier
            .testTag(testTag)
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                else Modifier
            )
    )
}
