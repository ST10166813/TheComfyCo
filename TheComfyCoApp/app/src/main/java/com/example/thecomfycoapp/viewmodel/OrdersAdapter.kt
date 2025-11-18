package com.example.thecomfycoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thecomfycoapp.models.OrderResponse

class OrdersAdapter :
    ListAdapter<OrderResponse, OrdersAdapter.OrderViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<OrderResponse>() {
        override fun areItemsTheSame(oldItem: OrderResponse, newItem: OrderResponse): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderResponse, newItem: OrderResponse): Boolean =
            oldItem == newItem
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvCustomer: TextView = itemView.findViewById(R.id.tvOrderCustomer)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)

        fun bind(order: OrderResponse) {
            tvOrderId.text = "Order #${order.id.takeLast(6)}"
            tvCustomer.text = order.customerName ?: "Guest"
            tvTotal.text = "R ${String.format("%.2f", order.grandTotal)}"
            tvStatus.text = order.status ?: "Pending"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
