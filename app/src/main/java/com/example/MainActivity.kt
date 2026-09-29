package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.remote.PublicBookResult
import com.example.ui.BookViewModel
import com.example.ui.catalog.CatalogScreen
import com.example.ui.collections.CollectionsScreen
import com.example.ui.detail.BookDetailScreen
import com.example.ui.edit.AddEditBookScreen
import com.example.ui.home.HomeScreen
import com.example.ui.publicsearch.PublicLibrarySearchScreen
import com.example.ui.scanner.BarcodeScannerScreen
import com.example.ui.stats.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BookViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: BookViewModel) {
    val navController = rememberNavController()
    var pendingImportResult by remember { mutableStateOf<PublicBookResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // Menu Inicial
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLibrary = { navController.navigate("catalog") },
                onNavigateToScan = { navController.navigate("scanner") },
                onNavigateToPublicSearch = { navController.navigate("public_search") },
                onNavigateToAddManual = {
                    pendingImportResult = null
                    navController.navigate("add_edit?bookId=-1")
                },
                onNavigateToDetail = { bookId ->
                    navController.navigate("detail/$bookId")
                },
                onNavigateToCollections = {
                    navController.navigate("collections")
                },
                onNavigateToStats = {
                    navController.navigate("statistics")
                }
            )
        }

        // A Minha Biblioteca
        composable("catalog") {
            CatalogScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToScan = { navController.navigate("scanner") },
                onNavigateToPublicSearch = { navController.navigate("public_search") },
                onNavigateToAddManual = {
                    pendingImportResult = null
                    navController.navigate("add_edit?bookId=-1")
                },
                onNavigateToDetail = { bookId ->
                    navController.navigate("detail/$bookId")
                },
                onNavigateToCollections = {
                    navController.navigate("collections")
                },
                onNavigateToStats = {
                    navController.navigate("statistics")
                }
            )
        }

        composable("collections") {
            CollectionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectCollection = { collectionId ->
                    viewModel.setCollectionFilter(collectionId)
                    navController.navigate("catalog") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }

        composable("statistics") {
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("scanner") {
            BarcodeScannerScreen(
                viewModel = viewModel,
                onBookFound = { result ->
                    pendingImportResult = result
                    navController.navigate("add_edit?bookId=-1")
                },
                onManualEntry = { isbn ->
                    pendingImportResult = PublicBookResult(
                        isbn = isbn,
                        title = "",
                        authors = ""
                    )
                    navController.navigate("add_edit?bookId=-1")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("public_search") {
            PublicLibrarySearchScreen(
                viewModel = viewModel,
                onImportBook = { result ->
                    pendingImportResult = result
                    navController.navigate("add_edit?bookId=-1")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_edit?bookId={bookId}",
            arguments = listOf(
                navArgument("bookId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId")?.takeIf { it > 0 }
            AddEditBookScreen(
                viewModel = viewModel,
                bookId = bookId,
                initialResult = if (bookId == null) pendingImportResult else null,
                onNavigateBack = {
                    pendingImportResult = null
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail/{bookId}",
            arguments = listOf(
                navArgument("bookId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: -1L
            BookDetailScreen(
                viewModel = viewModel,
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("add_edit?bookId=$id")
                }
            )
        }
    }
}
