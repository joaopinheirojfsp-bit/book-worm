package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Book
import com.example.data.model.BookCollection
import com.example.data.model.BookCollectionCrossRef
import com.example.data.model.ReadingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Book::class,
        BookCollection::class,
        BookCollectionCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biblio_catalog.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate with starter books & collections
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).bookDao()
                            val books = getStarterBooks()
                            val insertedIds = mutableListOf<Long>()
                            for (book in books) {
                                val id = dao.insertBook(book)
                                insertedIds.add(id)
                            }

                            // Starter collections
                            val col1Id = dao.insertCollection(
                                BookCollection(
                                    name = "Clássicos Fundamentais",
                                    description = "Grandes obras da literatura mundial e brasileira.",
                                    colorHex = "#1E3A5F"
                                )
                            )
                            val col2Id = dao.insertCollection(
                                BookCollection(
                                    name = "Favoritos da Vida",
                                    description = "Livros marcantes que merecem releitura constante.",
                                    colorHex = "#D97706"
                                )
                            )
                            val col3Id = dao.insertCollection(
                                BookCollection(
                                    name = "Meta de Leitura 2026",
                                    description = "Leituras planejadas para o ano corrente.",
                                    colorHex = "#0D9488"
                                )
                            )

                            // Associate starter books with collections
                            if (insertedIds.size >= 5) {
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[0], col1Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[0], col2Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[1], col1Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[1], col2Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[2], col1Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[3], col1Id))
                                dao.insertBookCollectionCrossRef(BookCollectionCrossRef(insertedIds[4], col3Id))
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getStarterBooks(): List<Book> {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 3600 * 1000)
            val sixtyDaysAgo = now - (60L * 24 * 3600 * 1000)

            return listOf(
                Book(
                    title = "Dom Casmurro",
                    author = "Machado de Assis",
                    publisher = "Garnier",
                    publishYear = 1899,
                    isbn = "9788535914849",
                    description = "Um dos maiores clássicos da literatura brasileira. Bentinho narra sua história de amor com Capitu e suas dúvidas eternas.",
                    pageCount = 256,
                    coverUrl = "https://covers.openlibrary.org/b/id/10523455-L.jpg",
                    rating = 5,
                    personalReview = "Obra-prima atemporal. A ambiguidade de Capitu continua fascinante!",
                    readingStatus = ReadingStatus.LIDO,
                    isFavorite = true,
                    genre = "Clássico",
                    tags = "Literatura Brasileira, Romance, Século XIX",
                    dateAdded = sixtyDaysAgo,
                    dateFinished = sixtyDaysAgo + (10L * 24 * 3600 * 1000)
                ),
                Book(
                    title = "1984",
                    author = "George Orwell",
                    publisher = "Companhia das Letras",
                    publishYear = 1949,
                    isbn = "9788535914849",
                    description = "Distopia perturbadora sobre vigilância em massa, totalitarismo e controle do pensamento pelo Grande Irmão.",
                    pageCount = 416,
                    coverUrl = "https://covers.openlibrary.org/b/id/12695535-L.jpg",
                    rating = 5,
                    personalReview = "Visão profética e assustadoramente atual. Leitura obrigatória.",
                    readingStatus = ReadingStatus.LIDO,
                    isFavorite = true,
                    genre = "Ficção Científica",
                    tags = "Distopia, Política, Clássico Moderno",
                    dateAdded = thirtyDaysAgo,
                    dateFinished = thirtyDaysAgo + (8L * 24 * 3600 * 1000)
                ),
                Book(
                    title = "O Pequeno Príncipe",
                    author = "Antoine de Saint-Exupéry",
                    publisher = "Agir",
                    publishYear = 1943,
                    isbn = "9788522031443",
                    description = "Uma fábula poética sobre o amor, amizade e o verdadeiro sentido da vida vista pelos olhos de uma criança.",
                    pageCount = 96,
                    coverUrl = "https://covers.openlibrary.org/b/id/10427848-L.jpg",
                    rating = 4,
                    personalReview = "O essencial é invisível aos olhos. Emocionante a cada releitura.",
                    readingStatus = ReadingStatus.LIDO,
                    isFavorite = false,
                    genre = "Filosofia",
                    tags = "Fábula, Infanto-Juvenil, Filosofia",
                    dateAdded = now - (15L * 24 * 3600 * 1000),
                    dateFinished = now - (14L * 24 * 3600 * 1000)
                ),
                Book(
                    title = "Capitães da Areia",
                    author = "Jorge Amado",
                    publisher = "Companhia das Letras",
                    publishYear = 1937,
                    isbn = "9788535911695",
                    description = "A comovente história de um grupo de meninos abandonados que sobrevivem nas ruas de Salvador.",
                    pageCount = 280,
                    coverUrl = "https://covers.openlibrary.org/b/id/8301740-L.jpg",
                    rating = 4,
                    personalReview = "Narrativa viva e marcante de Jorge Amado sobre a infância desassistida.",
                    readingStatus = ReadingStatus.LENDO,
                    isFavorite = true,
                    genre = "Romance Social",
                    tags = "Bahia, Modernismo, Literatura Brasileira",
                    dateAdded = now - (7L * 24 * 3600 * 1000)
                ),
                Book(
                    title = "Cem Anos de Solidão",
                    author = "Gabriel García Márquez",
                    publisher = "Record",
                    publishYear = 1967,
                    isbn = "9788501012074",
                    description = "A saga fabulosa da família Buendía na mítica aldeia de Macondo, expoente máximo do realismo mágico.",
                    pageCount = 448,
                    coverUrl = "https://covers.openlibrary.org/b/id/10531555-L.jpg",
                    rating = 0,
                    personalReview = "Próximo da fila para ler este mês!",
                    readingStatus = ReadingStatus.QUERO_LER,
                    isFavorite = false,
                    genre = "Realismo Mágico",
                    tags = "América Latina, Épico, Nobel",
                    dateAdded = now
                )
            )
        }
    }
}
