package com.antonelli.servipedia.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.antonelli.servipedia.entity.ServiModel

@Database(entities = [ServiModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DBHelper : RoomDatabase() {

    abstract fun userDao(): ServisDao

    companion object {
        @Volatile
        private var INSTANCE: DBHelper? = null

        fun getDatabase(context: Context): DBHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DBHelper::class.java,
                    "favs"
                )
                    .allowMainThreadQueries()
                    .addTypeConverter(Converters())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
