package com.miracle.barcode.generator.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val productName : String,
    val dataBarcode : String
)