package com.example.thecomfycoapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.thecomfycoapp.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.InputStream

class AddProductActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var variantContainer: LinearLayout
    private var imageUri: Uri? = null

    private val PICK_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        etName = findViewById(R.id.etName)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        etStock = findViewById(R.id.etStock)
        imgPreview = findViewById(R.id.imgPreview)
        variantContainer = findViewById(R.id.variantContainer)

        val btnChooseImage = findViewById<Button>(R.id.btnChooseImage)
        val btnUpload = findViewById<Button>(R.id.btnUploadProduct)
        val btnAddVariant = findViewById<Button>(R.id.btnAddVariant)

        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        btnAddVariant.setOnClickListener {
            addVariantField()
        }

        btnUpload.setOnClickListener {
            uploadProduct()
        }
    }

    private fun addVariantField() {
        val variantLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val etSize = EditText(this).apply {
            hint = "Size"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val etStock = EditText(this).apply {
            hint = "Stock"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        variantLayout.addView(etSize)
        variantLayout.addView(etStock)

        variantContainer.addView(variantLayout)
    }

    private fun collectVariants(): List<Map<String, Any>> {
        val variants = mutableListOf<Map<String, Any>>()

        for (i in 0 until variantContainer.childCount) {
            val layout = variantContainer.getChildAt(i) as LinearLayout
            val etSize = layout.getChildAt(0) as EditText
            val etStock = layout.getChildAt(1) as EditText

            val size = etSize.text.toString()
            val stock = etStock.text.toString().toIntOrNull() ?: 0

            if (size.isNotEmpty()) {
                variants.add(mapOf("size" to size, "stock" to stock))
            }
        }
        return variants
    }

    private fun uploadProduct() {
        val name = etName.text.toString()
        val description = etDescription.text.toString()
        val price = etPrice.text.toString().toDoubleOrNull()
        val stock = etStock.text.toString().toIntOrNull()
        val variants = collectVariants()

        if (name.isEmpty() || price == null || stock == null) {
            Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagePart = imageUri?.let { uri ->
                    val file = getFileFromUri(this@AddProductActivity, uri)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val product = RetrofitClient.api.createProduct(
                    name, description, price, stock, variants, imagePart
                )

                runOnUiThread {
                    Toast.makeText(this@AddProductActivity, "✅ Product uploaded!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AddProductActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
