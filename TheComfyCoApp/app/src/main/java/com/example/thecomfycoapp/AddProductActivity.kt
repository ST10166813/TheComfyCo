package com.example.thecomfycoapp

import com.google.gson.Gson // Add this import
import com.example.thecomfycoapp.models.Variant
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

    private fun collectVariants(): List<com.example.thecomfycoapp.models.Variant> {
        // Note: If you imported com.example.thecomfycoapp.models.Variant,
        // you don't need the full path.
        val variants = mutableListOf<com.example.thecomfycoapp.models.Variant>()

        for (i in 0 until variantContainer.childCount) {
            val layout = variantContainer.getChildAt(i) as LinearLayout
            val etSize = layout.getChildAt(0) as EditText
            val etStock = layout.getChildAt(1) as EditText
            // Note: Your layout does not have a color field, so we assume 'color' is null/empty.

            val size = etSize.text.toString()
            val stock = etStock.text.toString().toIntOrNull()

            if (size.isNotEmpty() && stock != null) {
                // Use your actual Variant data class
                variants.add(com.example.thecomfycoapp.models.Variant(size = size, color = null, stock = stock))
            }
        }
        return variants
    }
    fun getFileFromUri(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        // Create a temporary file
        val file = File(context.cacheDir, "temp_upload_file_${System.currentTimeMillis()}")

        try {
            // Open an InputStream from the URI
            contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                // Open a FileOutputStream for the temporary file
                file.outputStream().use { outputStream ->
                    // Copy the data from the InputStream to the FileOutputStream
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error appropriately, e.g., throw a custom exception
            throw RuntimeException("Could not create file from URI", e)
        }

        return file
    }
    private fun uploadProduct() {
        val nameStr = etName.text.toString()
        val descriptionStr = etDescription.text.toString()
        val priceDbl = etPrice.text.toString().toDoubleOrNull()
        val stockInt = etStock.text.toString().toIntOrNull() // Initial Stock
        val variantsList = collectVariants()

        // 1. Basic field validation
        if (nameStr.isEmpty() || priceDbl == null || stockInt == null) {
            Toast.makeText(this, "Fill in all required fields (Name, Price, Stock).", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. STOCK VALIDATION LOGIC
        // Calculate the total stock from all variants
        val totalVariantStock = variantsList.sumOf { it.stock ?: 0 }

        // Compare the total variant stock with the initial stock
        if (totalVariantStock != stockInt) {
            // If they don't match, show the Toast and stop the upload
            Toast.makeText(
                this,
                "Error: Total variant stock ($totalVariantStock) must equal Initial Stock ($stockInt).",
                Toast.LENGTH_LONG
            ).show()
            return // Exit the function, preventing the API call
        }
        // END STOCK VALIDATION LOGIC

        // Helper function to create a basic text RequestBody
        fun createTextRequestBody(text: String) =
            RequestBody.create("text/plain".toMediaTypeOrNull(), text)

        // Convert fields to RequestBody parts
        val namePart = createTextRequestBody(nameStr)
        val descriptionPart = createTextRequestBody(descriptionStr)
        val pricePart = createTextRequestBody(priceDbl.toString())
        val stockPart = createTextRequestBody(stockInt.toString())

        // Convert variants list to JSON string and then to RequestBody part
        val variantsJson = Gson().toJson(variantsList)
        val variantsPart = RequestBody.create("application/json".toMediaTypeOrNull(), variantsJson)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagePart = imageUri?.let { uri ->
                    val file = getFileFromUri(this@AddProductActivity, uri)
                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    // Field name "image" must match upload.single('image') in Node.js
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val product = RetrofitClient.api.createProduct(
                    namePart,
                    descriptionPart,
                    pricePart,
                    stockPart,
                    variantsPart,
                    imagePart
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



