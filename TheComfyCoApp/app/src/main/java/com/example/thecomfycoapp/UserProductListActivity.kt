package com.example.thecomfycoapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserProductListActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvState: TextView
    private lateinit var etSearch: TextInputEditText

    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var fullList: List<Product> = emptyList()
    private var adapter: ProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product_list)

        toolbar = findViewById(R.id.topAppBar)
        recyclerView = findViewById(R.id.recyclerViewUserProducts)
        progress = findViewById(R.id.progress)
        tvState = findViewById(R.id.tvState)
        etSearch = findViewById(R.id.etSearchList)

        // Toolbar with back arrow (like Settings page)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Grid layout + spacing
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.addItemDecoration(GridSpacingDecoration(2, 16, includeEdge = true))

        // Ensure Retrofit header/token ready
        RetrofitClient.init(applicationContext)

        // Search as you type (client-side filter)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s?.toString().orEmpty())
            }
        })

        fetchProducts()
    }

    private fun fetchProducts() {
        showLoading(true)
        uiScope.launch(Dispatchers.IO) {
            try {
                val products: List<Product> = RetrofitClient.api.getProducts()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    fullList = products
                    if (products.isEmpty()) {
                        showState("No products available.")
                    } else {
                        bindAdapter(products)
                        tvState.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showState("Error: ${e.message}")
                }
            }
        }
    }

    private fun bindAdapter(items: List<Product>) {
        adapter = ProductAdapter(items) { product ->
            val intent = Intent(this, UserProductDetailActivity::class.java)
            intent.putExtra("product", Gson().toJson(product))
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun filterList(queryRaw: String) {
        val q = queryRaw.trim().lowercase()
        val shown = if (q.isEmpty()) fullList else fullList.filter {
            it.name.lowercase().contains(q) || it.description.lowercase().contains(q)
        }
        bindAdapter(shown)
        tvState.visibility = if (shown.isEmpty()) View.VISIBLE else View.GONE
        if (shown.isEmpty()) tvState.text = "No results found."
    }

    private fun showLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) tvState.visibility = View.GONE
    }

    private fun showState(message: String) {
        tvState.text = message
        tvState.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        uiScope.cancel()
    }
}

/** Simple dp-based grid spacing decoration */
class GridSpacingDecoration(
    private val spanCount: Int,
    private val spacingDp: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: android.view.View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spacingPx = (view.context.resources.displayMetrics.density * spacingDp).toInt()
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacingPx - column * spacingPx / spanCount
            outRect.right = (column + 1) * spacingPx / spanCount
            if (position < spanCount) outRect.top = spacingPx
            outRect.bottom = spacingPx
        } else {
            outRect.left = column * spacingPx / spanCount
            outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
            if (position >= spanCount) outRect.top = spacingPx
        }
    }
}
