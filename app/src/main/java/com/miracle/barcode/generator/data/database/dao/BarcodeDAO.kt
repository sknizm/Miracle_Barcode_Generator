package com.miracle.barcode.generator.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.miracle.barcode.generator.data.database.entity.BarcodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDAO {

    @Insert
    suspend fun upsertBarcode(barcodeEntity: BarcodeEntity)

    @Delete
    suspend fun deleteBarcode(barcodeEntity: BarcodeEntity)

    @Query("SELECT * FROM barcodeEntity ORDER BY id ASC")
    fun getBarcodeFromId(): Flow<List<BarcodeEntity>>
}