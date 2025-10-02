package com.example.thecomfycoapp

import com.google.gson.Gson // Add this import
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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream



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
                variants.add(
                    com.example.thecomfycoapp.models.Variant(
                        size = size,
                        color = null,
                        stock = stock
                    )
                )
            }
        }
        return variants
    }

    // IMPROVED getFileFromUri to use 'use' block for better resource management
    fun getFileFromUri(context: Context, uri: Uri): File {
        val contentResolver = context.contentResolver
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)

        // Use 'use' blocks to ensure streams are closed even if an exception occurs
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw RuntimeException("Could not open input stream from URI: $uri")

        return tempFile
    }

    private fun uploadProduct() {
        val nameStr = etName.text.toString()
        val descriptionStr = etDescription.text.toString()
        val priceDbl = etPrice.text.toString().toDoubleOrNull()
        val stockInt = etStock.text.toString().toIntOrNull()
        val variantsList = collectVariants()
        val activityContext = this@AddProductActivity // Save context for use in coroutine

        // --- VALIDATION (Stays on Main Thread) ---
        if (nameStr.isEmpty() || priceDbl == null || stockInt == null) {
            Toast.makeText(activityContext, "Fill in all required fields (Name, Price, Stock).", Toast.LENGTH_SHORT).show()
            return
        }

        val totalVariantStock = variantsList.sumOf { it.stock ?: 0 }
        if (totalVariantStock != stockInt) {
            Toast.makeText(activityContext, "Error: Total variant stock ($totalVariantStock) must equal Initial Stock ($stockInt).", Toast.LENGTH_LONG).show()
            return
        }

        // Check image selection immediately on the main thread
        val currentImageUri = imageUri
        if (currentImageUri == null) {
            Toast.makeText(activityContext, "Please select an image.", Toast.LENGTH_SHORT).show()
            return
        }
        // --- END VALIDATION ---

        // Helper function for text parts
        fun createTextRequestBody(text: String) =
            RequestBody.create("text/plain".toMediaTypeOrNull(), text)

        // Convert data to RequestBody parts (Fast operations, stay on Main Thread)
        val namePart = createTextRequestBody(nameStr)
        val descriptionPart = createTextRequestBody(descriptionStr)
        val pricePart = createTextRequestBody(priceDbl.toString())
        val stockPart = createTextRequestBody(stockInt.toString())

        val variantsJson = Gson().toJson(variantsList)
        val variantsPart = RequestBody.create("text/plain".toMediaTypeOrNull(), variantsJson)


        // --- NETWORK AND I/O (Moved to Background Thread) ---
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. FILE I/O - Must be in Dispatchers.IO
                val file = getFileFromUri(activityContext, currentImageUri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

                // Field name "image" must match backend expectation (e.g., upload.single('image'))
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // 2. NETWORK CALL - Use corrected variable names
                val response = RetrofitClient.api.createProduct( // Assuming function is 'createProduct'
                    namePart,
                    descriptionPart,
                    pricePart,
                    stockPart,
                    variantsPart,
                    imagePart
                )

                // 3. UI UPDATE - Switch to Main Dispatcher
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(activityContext, "✅ Product uploaded!", Toast.LENGTH_SHORT).show()
                        activityContext.finish()
                    } else {
                        val errorMsg = response.errorBody()?.string()
                        Toast.makeText(activityContext, "❌ Upload failed: ${response.code()} - $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // 4. ERROR HANDLING - Switch to Main Dispatcher
                withContext(Dispatchers.Main) {
                    Toast.makeText(activityContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            // 1. Set the class-level variable
            imageUri = data?.data

            // 2. Display the image in the ImageView
            imgPreview.setImageURI(imageUri)

            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
        }
    }
    }



