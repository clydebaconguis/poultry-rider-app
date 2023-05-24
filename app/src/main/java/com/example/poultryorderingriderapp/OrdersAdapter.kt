package com.example.poultryorderingriderapp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class OrdersAdapter(private val context: Context, private val itemList:ArrayList<OrdersModel>): RecyclerView.Adapter<OrdersAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.customer_name)
        val date: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.order_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.name.text = currentItem.userName.replaceFirstChar { it.uppercase() }
        if (currentItem.status == "delivered") {
            holder.date.text = currentItem.dateDelivered
            holder.itemView.isEnabled = false
            holder.name.setTextColor(Color.GRAY)
            holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.baseline_directions_car_24_light,
                0,
                R.drawable.baseline_check_24,
                0
            )
        }else{
            holder.date.text = currentItem.dateToDeliver
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrdersDetail::class.java)
            intent.putExtra("order_id", currentItem.id)
            intent.putExtra("order_code", currentItem.transaction)
            intent.putExtra("order_name", currentItem.userName)
            intent.putExtra("order_status", currentItem.status)
            intent.putExtra("order_address", currentItem.address)
            intent.putExtra("order_phone", currentItem.phone)
            intent.putExtra("order_payment_method", currentItem.payOpt)
            intent.putExtra("order_total", currentItem.total)
            intent.putExtra("order_lat", currentItem.lat)
            intent.putExtra("order_long", currentItem.long)
            intent.putExtra("proof_of_payment", currentItem.proofOfPayment)
            intent.putExtra("date_to_deliver", currentItem.dateToDeliver)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}

data class OrderDetails(val id: Int, val image: String, val title: String,
                        val qty: Int, val size: String)