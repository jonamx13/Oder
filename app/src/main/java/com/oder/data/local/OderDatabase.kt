package com.oder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oder.data.local.dao.GrammarDao
import com.oder.data.local.dao.LexemeDao
import com.oder.data.local.dao.SrsStateDao
import com.oder.data.local.entity.GrammarModuleEntity
import com.oder.data.local.entity.GrammarRuleEntity
import com.oder.data.local.entity.LexemeEntity
import com.oder.data.local.entity.SrsStateEntity

@Database(
    entities = [
        LexemeEntity::class,
        SrsStateEntity::class,
        GrammarModuleEntity::class,
        GrammarRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OderDatabase : RoomDatabase() {

    abstract fun lexemeDao(): LexemeDao
    abstract fun srsStateDao(): SrsStateDao
    abstract fun grammarDao(): GrammarDao

    companion object {
        private const val DATABASE_NAME = "oder_database"

        @Volatile
        private var INSTANCE: OderDatabase? = null

        fun getInstance(context: Context): OderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OderDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
