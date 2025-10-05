////package com.example.thecomfycoapp.Fragments
////
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import android.widget.ImageView
////import android.widget.TextView
////import androidx.fragment.app.Fragment
////import com.example.thecomfycoapp.HomeActivity
////import com.example.thecomfycoapp.R
////
////class HomeFragment : Fragment() {
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? = inflater.inflate(R.layout.fragment_home, container, false)
////
////    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
////        super.onViewCreated(view, savedInstanceState)
////
////        // Hamburger icon
////        val ivMenu = view.findViewById<ImageView>(R.id.ivMenu)
////        ivMenu.setOnClickListener {
////            (requireActivity() as? HomeActivity)?.openDrawer()
////        }
////
////        // Welcome text
////        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
////        val fullName = requireActivity().intent.getStringExtra("name") ?: "User"
////        tvWelcome.text = "HI,\n$fullName!"
////    }
////}
//package com.example.thecomfycoapp.Fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import com.example.thecomfycoapp.HomeActivity
//import com.example.thecomfycoapp.R
//
//class HomeFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? = inflater.inflate(R.layout.fragment_home, container, false)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
////
////        // Hamburger
////        view.findViewById<ImageView>(R.id.ivMenu).setOnClickListener {
////            (requireActivity() as? HomeActivity)?.openDrawer()
////        }
//
//        // Welcome text (optional)
//        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
//        val fullName = requireActivity().intent.getStringExtra("name") ?: "USER"
//        tvWelcome.text = "HI,\n$fullName!"
//    }
//}
//package com.example.thecomfycoapp.Fragments
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ProgressBar
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.thecomfycoapp.R
//import com.example.thecomfycoapp.UserProductDetailActivity
//import com.example.thecomfycoapp.models.Product
//import com.example.thecomfycoapp.network.RetrofitClient
//import com.example.thecomfycoapp.ProductAdapter
//import com.google.gson.Gson
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class HomeFragment : Fragment() {
//
//    private lateinit var tvWelcome: TextView
//    private lateinit var rvHomeProducts: RecyclerView
//    private lateinit var progress: ProgressBar
//    private lateinit var tvError: TextView
//
//    private var adapter: ProductAdapter? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        return inflater.inflate(R.layout.fragment_home, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        tvWelcome = view.findViewById(R.id.tvWelcome)
//        rvHomeProducts = view.findViewById(R.id.rvHomeProducts)
//        progress = view.findViewById(R.id.progressHome)
//        tvError = view.findViewById(R.id.tvHomeError)
//
//        // Optional: keep your greeting logic if name is passed from Activity
//        // (Activity sends intent extra "name")
//        val name = requireActivity().intent.getStringExtra("name") ?: "User"
//        tvWelcome.text = "HI,\n$name!"
//
//        rvHomeProducts.layoutManager = GridLayoutManager(requireContext(), 2)
//
//        // Ensure Retrofit has token (if needed) before first call.
//        // If you already do this in Application or HomeActivity, you can remove this line.
//        RetrofitClient.init(requireContext().applicationContext)
//
//        fetchProductsForHome()
//    }
//
//    private fun fetchProductsForHome() {
//        progress.visibility = View.VISIBLE
//        tvError.visibility = View.GONE
//
//        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val products: List<Product> = RetrofitClient.api.getProducts()
//
//                withContext(Dispatchers.Main) {
//                    progress.visibility = View.GONE
//
//                    if (products.isEmpty()) {
//                        tvError.text = "No products available yet."
//                        tvError.visibility = View.VISIBLE
//                        return@withContext
//                    }
//
//                    adapter = ProductAdapter(products) { product ->
//                        val intent = Intent(requireContext(), UserProductDetailActivity::class.java)
//                        intent.putExtra("product", Gson().toJson(product))
//                        startActivity(intent)
//                    }
//                    rvHomeProducts.adapter = adapter
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    progress.visibility = View.GONE
//                    tvError.text = "Error loading products: ${e.message}"
//                    tvError.visibility = View.VISIBLE
//                }
//            }
//        }
//    }
//}
package com.example.thecomfycoapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.ProductAdapter
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.UserProductDetailActivity
import com.example.thecomfycoapp.UserProductListActivity
import com.example.thecomfycoapp.models.Product
import com.example.thecomfycoapp.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var tvWelcome: TextView
    private lateinit var rvHomeProducts: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnViewProduct: MaterialButton

    private var allProducts: List<Product> = emptyList()
    private var adapter: ProductAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWelcome = view.findViewById(R.id.tvWelcome)
        rvHomeProducts = view.findViewById(R.id.rvHomeProducts)
        progress = view.findViewById(R.id.progressHome)
        tvError = view.findViewById(R.id.tvHomeError)
        etSearch = view.findViewById(R.id.etSearch)
        btnViewProduct = view.findViewById(R.id.btnViewProduct)

        val name = requireActivity().intent.getStringExtra("name") ?: "User"
        tvWelcome.text = "HI,\n$name!"

        rvHomeProducts.layoutManager = GridLayoutManager(requireContext(), 2)

        RetrofitClient.init(requireContext().applicationContext)

        // 🔹 When "View Products" is clicked → open full product list
        btnViewProduct.setOnClickListener {
            val intent = Intent(requireContext(), UserProductListActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Search filter logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                val filtered = if (query.isEmpty()) allProducts else allProducts.filter {
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query)
                }
                updateAdapter(filtered)
            }
        })

        fetchProductsForHome()
    }

    private fun fetchProductsForHome() {
        progress.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val products = RetrofitClient.api.getProducts()
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                    allProducts = products
                    if (products.isEmpty()) {
                        tvError.text = "No products available yet."
                        tvError.visibility = View.VISIBLE
                    } else {
                        updateAdapter(products)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                    tvError.text = "Error loading products: ${e.message}"
                    tvError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateAdapter(list: List<Product>) {
        adapter = ProductAdapter(list) { product ->
            val intent = Intent(requireContext(), UserProductDetailActivity::class.java)
            intent.putExtra("product", Gson().toJson(product))
            startActivity(intent)
        }
        rvHomeProducts.adapter = adapter
    }
}
