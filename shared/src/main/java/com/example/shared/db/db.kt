package com.example.shared.db


import com.example.shared.db.refs.BookmarkTagRef
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shared.db.daos.BookmarkDao
import com.example.shared.db.daos.CollectionDao
import com.example.shared.db.daos.HighlightDao
import com.example.shared.db.daos.TagDao
import com.example.shared.db.entity.BookmarkEntity
import com.example.shared.db.entity.CollectionEntity
import com.example.shared.db.entity.HighlightEntity
import com.example.shared.db.entity.TagEntity
import com.example.shared.db.refs.HighlightTagRef

@Database(
    entities = [
        BookmarkEntity::class,
        HighlightEntity::class,
        CollectionEntity::class,
        TagEntity::class,
        BookmarkTagRef::class,
        HighlightTagRef::class
    ],
    version = 8
)
@TypeConverters(DateTypeConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    abstract  fun highlightDao(): HighlightDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()  // This is a simple migration strategy
                    .build()
                    .also { instance = it }
            }
        }
    }
}