package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.util.AppLanguage
import com.example.util.LanguageManager

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
    onCreateNewTab: (() -> Unit)? = null,
    onShowPrivacyPolicy: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val strings = remember(currentLanguage) { LanguageManager.getStrings(currentLanguage) }

    var expanded by remember { mutableStateOf(false) }
    var showInternalPrivacyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

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
                    text = strings.menuSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider()

            // 1. Menu Inicial
            BookWormMenuItem(
                icon = Icons.Default.Home,
                title = strings.home,
                subtitle = strings.homeSubtitle,
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
                title = strings.myLibrary,
                subtitle = strings.myLibrarySubtitle,
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
                title = strings.barcodeScanner,
                subtitle = strings.barcodeScannerSubtitle,
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
                title = strings.publicLibraries,
                subtitle = strings.publicLibrariesSubtitle,
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
                title = strings.collectionsTags,
                subtitle = strings.collectionsTagsSubtitle,
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
                title = strings.statsReports,
                subtitle = strings.statsReportsSubtitle,
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
                title = strings.addBook,
                subtitle = strings.addBookSubtitle,
                isSelected = false,
                testTag = "menu_item_add_book",
                onClick = {
                    expanded = false
                    onNavigateToAddBook()
                }
            )

            // Criar Nova Aba (Lista Permanente ou Temporária)
            if (onCreateNewTab != null) {
                BookWormMenuItem(
                    icon = Icons.Default.Tab,
                    title = strings.createNewTab,
                    subtitle = strings.createNewTabSubtitle,
                    isSelected = false,
                    testTag = "menu_item_create_new_tab",
                    onClick = {
                        expanded = false
                        onCreateNewTab()
                    }
                )
            }

            // 8. Exportar em PDF
            BookWormMenuItem(
                icon = Icons.Default.PictureAsPdf,
                title = strings.exportPdf,
                subtitle = strings.exportPdfSubtitle,
                isSelected = false,
                testTag = "menu_item_export_pdf",
                onClick = {
                    expanded = false
                    onExportPdf()
                }
            )

            Divider()

            // 9. Política de Privacidade & Armazenamento Local
            BookWormMenuItem(
                icon = Icons.Default.Security,
                title = strings.privacyPolicy,
                subtitle = strings.privacyPolicySubtitle,
                isSelected = false,
                testTag = "menu_item_privacy_policy",
                onClick = {
                    expanded = false
                    if (onShowPrivacyPolicy != null) {
                        onShowPrivacyPolicy()
                    } else {
                        showInternalPrivacyDialog = true
                    }
                }
            )

            // 10. Mudar Idioma (Português-PT ↔ English-UK)
            BookWormMenuItem(
                icon = Icons.Default.Language,
                title = strings.changeLanguage,
                subtitle = strings.changeLanguageSubtitle,
                isSelected = false,
                testTag = "menu_item_change_language",
                onClick = {
                    expanded = false
                    showLanguageDialog = true
                }
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.selectLanguage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    LanguageManager.setLanguage(context, lang)
                                    showLanguageDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        LanguageManager.setLanguage(context, lang)
                                        showLanguageDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = lang.flag,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = if (lang == AppLanguage.PT) "Menus e navegação em Português de Portugal" else "Menus and navigation in UK English",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showInternalPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showInternalPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.privacyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.privacyStorageLocal,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.privacyStorageLocalDesc,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Divider()
                    Text(
                        text = strings.privacyGoogleNotice,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://policies.google.com/privacy")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_google_privacy_policy_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.privacyGoogleButton)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showInternalPrivacyDialog = false },
                    modifier = Modifier.testTag("close_privacy_dialog_button")
                ) {
                    Text(strings.close)
                }
            }
        )
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
