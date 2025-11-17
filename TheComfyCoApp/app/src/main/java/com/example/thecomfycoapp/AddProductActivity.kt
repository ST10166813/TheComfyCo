package com.example.thecomfycoapp

import com.google.gson.Gson
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.thecomfycoapp.models.Variant
import com.example.thecomfycoapp.network.RetrofitClient
import com.example.thecomfycoapp.offline.AppDatabase
import com.example.thecomfycoapp.offline.OfflineProduct
import com.example.thecomfycoapp.offline.OfflineSyncManager
import com.example.thecomfycoapp.utils.InternetCheck
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

        btnAddVariant.setOnClickListener { addVariantField() }
        btnUpload.setOnClickListener { uploadProduct() }
    }

    private fun addVariantField() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val size = EditText(this).apply {
            hint = "Size"
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }

        val stock = EditText(this).apply {
            hint = "Stock"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }

        layout.addView(size)
        layout.addView(stock)

        variantContainer.addView(layout)
    }

    private fun collectVariants(): List<Variant> {
        val list = mutableListOf<Variant>()

        for (i in 0 until variantContainer.childCount) {
            val layout = variantContainer.getChildAt(i) as LinearLayout
            val size = (layout.getChildAt(0) as EditText).text.toString()
            val stock = (layout.getChildAt(1) as EditText).text.toString().toIntOrNull()

            if (size.isNotEmpty() && stock != null) {
                list.add(Variant(size = size, color = null, stock = stock))
            }
        }
        return list
    }

    fun getFileFromUri(context: Context, uri: Uri): File {
        val temp = File.createTempFile("upload", ".jpg", context.cacheDir)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(temp).use { output -> input.copyTo(output) }
        }
        return temp
    }

    private fun uploadProduct() {
        val nameStr = etName.text.toString()
        val descStr = etDescription.text.toString()
        val price = etPrice.text.toString().toDoubleOrNull()
        val stock = etStock.text.toString().toIntOrNull()
        val variants = collectVariants()
        val ctx = this

        if (nameStr.isEmpty() || price == null || stock == null) {
            Toast.makeText(ctx, "Fill required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalVariantStock = variants.sumOf { it.stock ?: 0 }
        if (totalVariantStock != stock) {
            Toast.makeText(ctx, "Variant stock must equal total stock.", Toast.LENGTH_LONG).show()
            return
        }

        val img = imageUri
        if (img == null) {
            Toast.makeText(ctx, "Select an image.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {

            // 🛑 CRITICAL TEMPORARY CHANGE FOR TESTING:
            // Change this to 'val isOnline = false' to force the offline save and test Room DB.
            val isOnline = InternetCheck.isOnline(ctx)


            // If OFFLINE → Save to RoomDB
            if (!isOnline) {
                try {
                    val offlineImage = getFileFromUri(ctx, img).absolutePath

                    AppDatabase.getDatabase(ctx)
                        .offlineProductDao()
                        .insert(
                            OfflineProduct(
                                name = nameStr,
                                description = descStr,
                                price = price,
                                stock = stock,
                                variants = Gson().toJson(variants),
                                imagePath = offlineImage
                            )
                        )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, "Saved offline! Will sync later.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("OfflineInsert", "Failed to insert product offline", e)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, "Offline save failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                return@launch
            }


            // ONLINE → Normal upload (Wrapped in try-catch to prevent crash on network failure)
            try {
                val file = getFileFromUri(ctx, img)
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)

                val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), nameStr)
                val descPart = RequestBody.create("text/plain".toMediaTypeOrNull(), descStr)
                val pricePart = RequestBody.create("text/plain".toMediaTypeOrNull(), price.toString())
                val stockPart = RequestBody.create("text/plain".toMediaTypeOrNull(), stock.toString())
                val variantsPart = RequestBody.create("text/plain".toMediaTypeOrNull(), Gson().toJson(variants))

                val response = RetrofitClient.api.createProduct(
                    namePart, descPart, pricePart, stockPart, variantsPart, imagePart
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(ctx, "Product uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(ctx, "Upload failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Catches IOException (e.g., Unable to resolve host) and other network issues
                Log.e("OnlineUpload", "Network error during online upload attempt: ${e.message}", e)

                withContext(Dispatchers.Main) {
                    // Show a non-crashing failure message
                    Toast.makeText(ctx, "Upload failed: Check your internet connection.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data?.data
            imgPreview.setImageURI(imageUri)
            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        // Auto-sync any offline products
        lifecycleScope.launch {
            if (InternetCheck.isOnline(this@AddProductActivity)) {
                // The sync manager should also have try-catch blocks internally
                OfflineSyncManager.syncProducts(this@AddProductActivity)
            }
        }
    }
}