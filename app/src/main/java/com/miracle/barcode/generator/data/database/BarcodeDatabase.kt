package com.miracle.barcode.generator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miracle.barcode.generator.data.database.dao.BarcodeDAO
import com.miracle.barcode.generator.data.database.entity.BarcodeEntity

@Database(entities = [BarcodeEntity::class], version = 1, exportSchema = false)
abstract class BarcodeDatabase : RoomDatabase() {

    abstract fun barCodeDAO() : BarcodeDAO

    companion object{

        @Volatile
        private var INSTANCE : BarcodeDatabase ? = null


        fun getDatabase (context : Context): BarcodeDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarcodeDatabase::class.java,
                    "barcode_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}