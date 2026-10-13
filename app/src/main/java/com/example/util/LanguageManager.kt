package com.example.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    PT("pt", "Português (PT)", "🇵🇹"),
    EN("en", "English (UK)", "🇬🇧")
}

data class AppStrings(
    // App & Navigation
    val appTitle: String,
    val menuTitle: String,
    val menuSubtitle: String,
    val home: String,
    val homeSubtitle: String,
    val myLibrary: String,
    val myLibrarySubtitle: String,
    val barcodeScanner: String,
    val barcodeScannerSubtitle: String,
    val publicLibraries: String,
    val publicLibrariesSubtitle: String,
    val collectionsTags: String,
    val collectionsTagsSubtitle: String,
    val statsReports: String,
    val statsReportsSubtitle: String,
    val addBook: String,
    val addBookSubtitle: String,
    val createNewTab: String,
    val createNewTabSubtitle: String,
    val exportPdf: String,
    val exportPdfSubtitle: String,
    val privacyPolicy: String,
    val privacyPolicySubtitle: String,
    val changeLanguage: String,
    val changeLanguageSubtitle: String,
    val selectLanguage: String,

    // Home Tabs (Only 4)
    val tabMyLibrary: String,
    val tabRead: String,
    val tabReading: String,
    val tabCustomise: String,

    // Quick Actions
    val scanAction: String,
    val searchAction: String,
    val statsAction: String,

    // Hero Reading
    val currentReading: String,
    val markAsRead: String,
    val continueReading: String,
    val pickAnotherBook: String,
    val noReadingBookTitle: String,
    val noReadingBookSubtitle: String,

    // Status
    val statusUnread: String,
    val statusReading: String,
    val statusRead: String,
    val statusAbandoned: String,

    // Temporary & Permanent Lists
    val permanentList: String,
    val temporaryList: String,
    val temporaryBadge: String,
    val temporaryStatusLockNotice: String,
    val moveToPermanentToChangeStatus: String,
    val moveToPermanentButton: String,
    val moveToList: String,
    val destinationListPrompt: String,

    // Personalize Tab
    val customiseTitle: String,
    val customiseDescription: String,
    val createListButton: String,
    val listNameLabel: String,
    val listTypePrompt: String,
    val permanentOptionTitle: String,
    val permanentOptionDesc: String,
    val temporaryOptionTitle: String,
    val temporaryOptionDesc: String,
    val tabColor: String,
    val saveListButton: String,
    val editList: String,
    val deleteList: String,
    val yourCustomLists: String,
    val viewBooks: String,
    val wantToReadTitle: String,
    val wantToReadDesc: String,

    // Common
    val cancel: String,
    val close: String,
    val save: String,
    val delete: String,
    val confirm: String,
    val edit: String,
    val isbnCode: String,
    val viewIsbn: String,
    val copyIsbn: String,
    val booksCountSuffix: String,
    val noBooksInTab: String,
    val privacyTitle: String,
    val privacyStorageLocal: String,
    val privacyStorageLocalDesc: String,
    val privacyGoogleNotice: String,
    val privacyGoogleButton: String
)

val StringsPT = AppStrings(
    appTitle = "BookWorm",
    menuTitle = "BookWorm",
    menuSubtitle = "Opções do Aplicativo",
    home = "Menu Inicial",
    homeSubtitle = "Livro a ler e destaques",
    myLibrary = "A Minha Biblioteca",
    myLibrarySubtitle = "Todos os meus livros e filtros",
    barcodeScanner = "Leitor de Código de Barras",
    barcodeScannerSubtitle = "Escanear ISBN com câmara",
    publicLibraries = "Bibliotecas Públicas",
    publicLibrariesSubtitle = "Open Library & Google Books",
    collectionsTags = "Coleções & Tags",
    collectionsTagsSubtitle = "Organizar suas estantes",
    statsReports = "Estatísticas & Relatórios",
    statsReportsSubtitle = "Gráficos, metas e resumos",
    addBook = "Adicionar Livro",
    addBookSubtitle = "Novo exemplar no catálogo",
    createNewTab = "Criar Nova Aba",
    createNewTabSubtitle = "Lista permanente ou temporária",
    exportPdf = "Exportar Biblioteca (PDF)",
    exportPdfSubtitle = "Gerar documento ou partilhar",
    privacyPolicy = "Política de Privacidade",
    privacyPolicySubtitle = "Armazenamento local & Google",
    changeLanguage = "Mudar Idioma",
    changeLanguageSubtitle = "Português-PT ↔ English-UK",
    selectLanguage = "Selecionar Idioma da Aplicação",

    tabMyLibrary = "A Minha Biblioteca",
    tabRead = "Lidos",
    tabReading = "A Ler",
    tabCustomise = "Personalizar",

    scanAction = "Escanear",
    searchAction = "Pesquisar",
    statsAction = "Estatísticas",

    currentReading = "A Ler Atualmente",
    markAsRead = "Marcar como Lido",
    continueReading = "Continuar a Ler",
    pickAnotherBook = "Escolher Outro Livro",
    noReadingBookTitle = "Nenhum livro em leitura ativa",
    noReadingBookSubtitle = "Selecione um livro da biblioteca para acompanhar o progresso de leitura.",

    statusUnread = "Não lido",
    statusReading = "A ler",
    statusRead = "Lido",
    statusAbandoned = "Abandonado",

    permanentList = "Lista Permanente",
    temporaryList = "Lista Temporária",
    temporaryBadge = "Temp.",
    temporaryStatusLockNotice = "Em listas temporárias, a opção é sempre 'Não lido'. Esta opção só é alterável quando se muda para lista permanente.",
    moveToPermanentToChangeStatus = "Mova o livro para 'A Minha Biblioteca', 'Lidos', 'A Ler' ou outra lista permanente para alterar o status.",
    moveToPermanentButton = "Mover para Lista Permanente",
    moveToList = "Mover para outra Lista",
    destinationListPrompt = "Selecione a lista de destino:",

    customiseTitle = "Personalizar Abas & Listas",
    customiseDescription = "Crie e nomeie listas personalizadas consoante as suas necessidades (ex: Mangás, Clube do Livro, Empréstimos).",
    createListButton = "Criar Nova Lista / Aba",
    listNameLabel = "Nome da Lista / Aba *",
    listTypePrompt = "Tipo de Funcionamento da Lista:",
    permanentOptionTitle = "Lista Permanente (Padrão)",
    permanentOptionDesc = "Os livros desta lista são incluídos em 'A Minha Biblioteca' permanentemente.",
    temporaryOptionTitle = "Lista Temporária",
    temporaryOptionDesc = "Os livros têm opção fixa como 'Não lido' e só entrarão em 'A Minha Biblioteca' quando forem movidos para uma lista permanente.",
    tabColor = "Cor da Aba:",
    saveListButton = "Salvar e Ativar Aba",
    editList = "Editar Lista",
    deleteList = "Excluir Lista",
    yourCustomLists = "As Suas Listas Personalizadas",
    viewBooks = "Ver Livros",
    wantToReadTitle = "Quero Ler (Lista Temporária Padrão)",
    wantToReadDesc = "Desejos de leitura futura. Os livros aqui são 'Não lidos' até serem movidos para lista permanente.",

    cancel = "Cancelar",
    close = "Fechar",
    save = "Salvar",
    delete = "Excluir",
    confirm = "Confirmar",
    edit = "Editar",
    isbnCode = "Código ISBN",
    viewIsbn = "Ver Código ISBN",
    copyIsbn = "Copiar ISBN",
    booksCountSuffix = "livros",
    noBooksInTab = "Nenhum livro nesta lista.",
    privacyTitle = "Política de Privacidade & Armazenamento",
    privacyStorageLocal = "Armazenamento 100% Local",
    privacyStorageLocalDesc = "Todo o conteúdo da sua biblioteca BookWorm é guardado exclusivamente de forma local no seu dispositivo através de banco de dados offline (SQLite / Room). Nenhum dado pessoal ou catálogo é enviado para a nuvem.",
    privacyGoogleNotice = "Consulte a declaração de privacidade simplificada e normas da Google:",
    privacyGoogleButton = "Ver Declaração da Google"
)

val StringsEN = AppStrings(
    appTitle = "BookWorm",
    menuTitle = "BookWorm",
    menuSubtitle = "Application Options",
    home = "Home",
    homeSubtitle = "Featured & reading book",
    myLibrary = "My Library",
    myLibrarySubtitle = "All my books and filters",
    barcodeScanner = "Barcode Scanner",
    barcodeScannerSubtitle = "Scan ISBN with camera",
    publicLibraries = "Public Libraries",
    publicLibrariesSubtitle = "Open Library & Google Books",
    collectionsTags = "Collections & Tags",
    collectionsTagsSubtitle = "Organise your bookshelves",
    statsReports = "Statistics & Reports",
    statsReportsSubtitle = "Charts, targets & summaries",
    addBook = "Add Book",
    addBookSubtitle = "Register new book in catalogue",
    createNewTab = "Create New Tab",
    createNewTabSubtitle = "Permanent or temporary list",
    exportPdf = "Export Library (PDF)",
    exportPdfSubtitle = "Generate document or share",
    privacyPolicy = "Privacy Policy",
    privacyPolicySubtitle = "Local storage & Google",
    changeLanguage = "Change Language",
    changeLanguageSubtitle = "Português-PT ↔ English-UK",
    selectLanguage = "Select Application Language",

    tabMyLibrary = "My Library",
    tabRead = "Read",
    tabReading = "Reading",
    tabCustomise = "Customise",

    scanAction = "Scan",
    searchAction = "Search",
    statsAction = "Statistics",

    currentReading = "Currently Reading",
    markAsRead = "Mark as Read",
    continueReading = "Continue Reading",
    pickAnotherBook = "Pick Another Book",
    noReadingBookTitle = "No book currently being read",
    noReadingBookSubtitle = "Select a book from your library to track reading progress.",

    statusUnread = "Unread",
    statusReading = "Reading",
    statusRead = "Read",
    statusAbandoned = "Abandoned",

    permanentList = "Permanent List",
    temporaryList = "Temporary List",
    temporaryBadge = "Temp.",
    temporaryStatusLockNotice = "In temporary lists, the option is always 'Unread'. This option can only be changed when moving to a permanent list.",
    moveToPermanentToChangeStatus = "Move this book to 'My Library', 'Read', 'Reading' or another permanent list to change status.",
    moveToPermanentButton = "Move to Permanent List",
    moveToList = "Move to another List",
    destinationListPrompt = "Select destination list:",

    customiseTitle = "Customise Tabs & Lists",
    customiseDescription = "Create and name custom lists tailored to your reading needs (e.g., Manga, Book Club, Borrowed).",
    createListButton = "Create New List / Tab",
    listNameLabel = "List / Tab Name *",
    listTypePrompt = "List Functionality Type:",
    permanentOptionTitle = "Permanent List (Default)",
    permanentOptionDesc = "Books in this list are permanently included in 'My Library'.",
    temporaryOptionTitle = "Temporary List",
    temporaryOptionDesc = "Books are locked to 'Unread' and only enter 'My Library' once moved to a permanent list.",
    tabColor = "Tab Colour:",
    saveListButton = "Save and Enable Tab",
    editList = "Edit List",
    deleteList = "Delete List",
    yourCustomLists = "Your Custom Lists",
    viewBooks = "View Books",
    wantToReadTitle = "Want to Read (Default Temporary List)",
    wantToReadDesc = "Future reading wishes. Books here remain 'Unread' until moved to a permanent list.",

    cancel = "Cancel",
    close = "Close",
    save = "Save",
    delete = "Delete",
    confirm = "Confirm",
    edit = "Edit",
    isbnCode = "ISBN Code",
    viewIsbn = "View ISBN Code",
    copyIsbn = "Copy ISBN",
    booksCountSuffix = "books",
    noBooksInTab = "No books in this list.",
    privacyTitle = "Privacy Policy & Storage",
    privacyStorageLocal = "100% Local Storage",
    privacyStorageLocalDesc = "All content in your BookWorm library is kept strictly local on your device using offline SQLite (Room) database. No personal data or books are sent to the cloud.",
    privacyGoogleNotice = "Check Google's simple privacy statement and standards:",
    privacyGoogleButton = "View Google Privacy Statement"
)

object LanguageManager {
    private const val PREFS_NAME = "bookworm_lang_prefs"
    private const val KEY_LANG = "selected_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.PT)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANG, AppLanguage.PT.code) ?: AppLanguage.PT.code
        _currentLanguage.value = if (code == AppLanguage.EN.code) AppLanguage.EN else AppLanguage.PT
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        _currentLanguage.value = language
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language.code).apply()
    }

    fun getStrings(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.PT -> StringsPT
            AppLanguage.EN -> StringsEN
        }
    }
}

val LocalAppStrings = compositionLocalOf { StringsPT }
