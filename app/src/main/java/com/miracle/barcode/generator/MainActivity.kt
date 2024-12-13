package com.miracle.barcode.generator

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.miracle.barcode.generator.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val preferences by lazy { getSharedPreferences("BarcodeFields", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add default "Product Name" field
        if (!preferences.contains("fields")) {
            addInputField("Product Name") // Add default field if no saved data
        }

        // Restore saved fields
        restoreFields()

        // Add New Button
        binding.addNewFieldBtn.setOnClickListener {
            openAddNewInputTextDialog()
        }

        // Preview Barcode Button
        binding.previewBarcodeBtn.setOnClickListener {
            val result = getAllFieldData()
            if(result.isNotEmpty()){
            val intent = Intent(this, PreviewBarcodeActivity::class.java)
            intent.putExtra("BARCODE_DATA", result)
            startActivity(intent)
            }
            else{
                Toast.makeText(this, "Please Fill the Form", Toast.LENGTH_SHORT).show()
            }

        }

        // Remove All Fields Except "Product Name"
        binding.removeAllFieldsBtn.setOnClickListener {
            removeAllFieldsExceptDefault()
        }
    }

    private fun openAddNewInputTextDialog() {
        val dialog = layoutInflater.inflate(R.layout.dialog_add_field, null)
        val fieldNameInput: TextInputEditText = dialog.findViewById(R.id.fieldNameInput)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_new_field))
            .setView(dialog)
            .setPositiveButton("Add") { _, _ ->
                val fieldName = fieldNameInput.text.toString().trim()

                if (fieldName.isNotEmpty()) {
                    addInputField(fieldName)
                    saveFields() // Save fields after adding a new one
                } else {
                    Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addInputField(fieldName: String, fieldValue: String = "") {
        val textInputLayout = TextInputLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val textInputEditText = TextInputEditText(
            ContextThemeWrapper(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_TextInputEditText_OutlinedBox_Dense)
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            hint = fieldName
            setText(fieldValue) // Restore previous value if available
        }

        textInputLayout.addView(textInputEditText)
        binding.textInputLinearLayout.addView(textInputLayout)
    }

    private fun getAllFieldData(): String {
        val data = StringBuilder()
        for (i in 0 until binding.textInputLinearLayout.childCount) {
            val textInputLayout = binding.textInputLinearLayout.getChildAt(i) as TextInputLayout
            val textInputEditText = textInputLayout.editText
            val fieldName = textInputEditText?.hint?.toString()
            val fieldValue = textInputEditText?.text?.toString()

            if (!fieldName.isNullOrEmpty() && !fieldValue.isNullOrEmpty()) {
                data.append("$fieldName: $fieldValue \n ")
            }
        }
        return data.toString().trimEnd(',', ' ')
    }

    private fun saveFields() {
        val jsonArray = JSONArray()
        for (i in 0 until binding.textInputLinearLayout.childCount) {
            val textInputLayout = binding.textInputLinearLayout.getChildAt(i) as TextInputLayout
            val textInputEditText = textInputLayout.editText
            val fieldName = textInputEditText?.hint?.toString()
            val fieldValue = textInputEditText?.text?.toString()

            if (!fieldName.isNullOrEmpty()) {
                val fieldObject = JSONObject().apply {
                    put("name", fieldName)
                    put("value", "")
                }
                jsonArray.put(fieldObject)
            }
        }
        preferences.edit().putString("fields", jsonArray.toString()).apply()
    }

    private fun restoreFields() {
        val savedFields = preferences.getString("fields", null) ?: return
        val jsonArray = JSONArray(savedFields)
        for (i in 0 until jsonArray.length()) {
            val fieldObject = jsonArray.getJSONObject(i)
            val fieldName = fieldObject.getString("name")
            val fieldValue = fieldObject.getString("value")
            addInputField(fieldName, fieldValue)
        }
    }

    private fun removeAllFieldsExceptDefault() {
        val childCount = binding.textInputLinearLayout.childCount
        for (i in childCount - 1 downTo 0) {
            val textInputLayout = binding.textInputLinearLayout.getChildAt(i) as TextInputLayout
            val textInputEditText = textInputLayout.editText
            val fieldName = textInputEditText?.hint?.toString()
            if (fieldName != "Product Name") {
                binding.textInputLinearLayout.removeViewAt(i)
            }
        }
        saveFields() // Save updated fields
    }
}
