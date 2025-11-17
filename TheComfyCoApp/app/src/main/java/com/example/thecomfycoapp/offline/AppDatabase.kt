package com.example.thecomfycoapp.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OfflineProduct::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun offlineProductDao(): OfflineProductDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext, // Using applicationContext
                    AppDatabase::class.java,
                    "thecomfyco_offline_db"
                ).build()
                instance = db
                db
            }
        }
    }
}
