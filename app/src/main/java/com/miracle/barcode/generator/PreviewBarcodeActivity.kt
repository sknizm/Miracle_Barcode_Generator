package com.miracle.barcode.generator

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.miracle.barcode.generator.databinding.ActivityPreviewBarcodeBinding

class PreviewBarcodeActivity : AppCompatActivity() {
    lateinit var binding : ActivityPreviewBarcodeBinding
    private lateinit var barcodeBitmap : Bitmap

    // Permissions required for Bluetooth functionality
    private val bluetoothPermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
        else -> arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    // Register a permission request callback
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                printBarcode()
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth permissions are required to print the barcode.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityPreviewBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
            val barcodeData = intent.getStringExtra("BARCODE_DATA") ?:""
//        Make Barcode Btn
        binding.getBarcode.setOnClickListener {
            makeBarcode(barcodeData)
        }


//        Print Barcode Btn
        binding.printBarcodeBtn.setOnClickListener {
          checkAndRequestBluetoothPermissions()


        }

    }

    private fun checkAndRequestBluetoothPermissions() {
        if (bluetoothPermissions.all { permission ->
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            }) {
            printBarcode()
        } else {
            // Request the permissions
            requestPermissionsLauncher.launch(bluetoothPermissions)
        }
    }


    private fun makeBarcode(barcodeData : String) {
        val width = binding.widthTextInput.text.toString().toIntOrNull()
        val height = binding.heightTextInput.text.toString().toIntOrNull()

        if (width == null || height == null || barcodeData.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val bitmap = BarcodeEncoder().encodeBitmap(
                barcodeData,
                BarcodeFormat.CODE_128,
                width,
                height
            )
//            binding.previewBarcodeImageView.setImageBitmap(bitmap)
            barcodeBitmap = bitmap
            // Display the generated barcode in the ImageView
            binding.previewBarcodeImageView.setImageBitmap(barcodeBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate barcode", Toast.LENGTH_SHORT).show()
        }
    }



    private fun printBarcode() {
        val printAmount = binding.printTextInput.text.toString().toIntOrNull()

        if (printAmount == null || printAmount <= 0) {
            Toast.makeText(this, "Enter a valid print amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (barcodeBitmap == null) {
            Toast.makeText(this, "Generate a barcode first", Toast.LENGTH_SHORT).show()
            return
        }

        // Find the Bluetooth printer
        val bluetoothConnection: BluetoothConnection? = BluetoothPrintersConnections.selectFirstPaired()

        if (bluetoothConnection == null) {
            Toast.makeText(this, "No Bluetooth printer found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Create a printer instance
            val printer = EscPosPrinter(bluetoothConnection, 203, 48f, 32)

            // Print the barcode for the specified number of times
            for (i in 0 until printAmount) {
                printer.printFormattedTextAndCut(
                    "[C]<img>${PrinterTextParserImg.bitmapToHexadecimalString(printer, barcodeBitmap)}</img>"
                )
            }

            Toast.makeText(this, "Printing complete", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to print barcode", Toast.LENGTH_SHORT).show()
        }
    }
}