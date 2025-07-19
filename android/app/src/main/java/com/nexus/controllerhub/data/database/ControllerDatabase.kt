package com.nexus.controllerhub.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.nexus.controllerhub.data.model.ControllerProfile
import com.nexus.controllerhub.data.model.Macro

@Database(
    entities = [ControllerProfile::class, Macro::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ControllerDatabase : RoomDatabase() {
    
    abstract fun profileDao(): ControllerProfileDao
    abstract fun macroDao(): MacroDao
    
    companion object {
        @Volatile
        private var INSTANCE: ControllerDatabase? = null
        
        fun getDatabase(context: Context): ControllerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ControllerDatabase::class.java,
                    "controller_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}