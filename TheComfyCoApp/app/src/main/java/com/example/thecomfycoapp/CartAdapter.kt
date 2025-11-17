package com.example.thecomfycoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.thecomfycoapp.models.CartItemModel
import com.example.thecomfycoapp.network.RetrofitClient

class CartAdapter(
    private val items: MutableList<CartItemModel>,
    private val onQtyChanged: (position: Int, newQty: Int) -> Unit,
    private val onItemDeleted: (position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgCartProduct)
        val tvName: TextView = view.findViewById(R.id.tvCartProductName)
        val tvDetails: TextView = view.findViewById(R.id.tvCartProductDetails)
        val tvQty: TextView = view.findViewById(R.id.tvQty)
        val btnMinus: TextView = view.findViewById(R.id.btnQtyMinus)
        val btnPlus: TextView = view.findViewById(R.id.btnQtyPlus)
        val tvLineTotal: TextView = view.findViewById(R.id.tvCartItemTotal)
        val imgDelete: ImageView = view.findViewById(R.id.imgDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val product = item.product
        val unitPrice = product.price ?: 0.0
        val lineTotal = unitPrice * item.qty

        holder.tvName.text = product.name
        holder.tvDetails.text = buildString {
            append("R ${String.format("%.2f", unitPrice)}")
            if (!item.size.isNullOrBlank()) {
                append("\nSize: ${item.size}")
            }
        }
        holder.tvQty.text = item.qty.toString()
        holder.tvLineTotal.text = "R ${String.format("%.2f", lineTotal)}"

        val raw = product.images?.firstOrNull()
        val imageUrl = if (!raw.isNullOrBlank()) {
            if (raw.startsWith("http", ignoreCase = true)) raw
            else "${RetrofitClient.BASE_URL.trimEnd('/')}/${raw.trimStart('/')}"
        } else null

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .error(R.drawable.logo)
            .into(holder.imgProduct)

        holder.btnPlus.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val newQty = items[pos].qty + 1
                onQtyChanged(pos, newQty)
            }
        }

        holder.btnMinus.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                val current = items[pos].qty
                val newQty = current - 1
                onQtyChanged(pos, newQty)
            }
        }

        holder.imgDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemDeleted(pos)
            }
        }
    }
}
