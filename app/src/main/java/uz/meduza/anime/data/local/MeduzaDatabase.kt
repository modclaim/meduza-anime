package uz.meduza.anime.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.meduza.anime.data.local.dao.AnimeDao
import uz.meduza.anime.data.local.dao.BookmarkDao
import uz.meduza.anime.data.local.dao.HistoryDao
import uz.meduza.anime.data.local.entities.AnimeEntity
import uz.meduza.anime.data.local.entities.BookmarkEntity
import uz.meduza.anime.data.local.entities.WatchHistoryEntity

@Database(
    entities = [AnimeEntity::class, WatchHistoryEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MeduzaDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: MeduzaDatabase? = null

        fun getInstance(context: Context): MeduzaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        MeduzaDatabase::class.java,
                        "meduza_anime.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                } catch (e: Exception) {
                    Room.inMemoryDatabaseBuilder(
                        context.applicationContext,
                        MeduzaDatabase::class.java
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }.also { INSTANCE = it }
            }
        }
    }
}
