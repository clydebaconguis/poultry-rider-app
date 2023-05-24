package com.example.poultryorderingriderapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var ordersList: RecyclerView
    private lateinit var grandTotal: TextView
    private lateinit var title: TextView
    private lateinit var count: TextView
    private lateinit var history: ImageButton
    private lateinit var totalCollected: TextView
    private var list = ArrayList<OrdersModel>()
    private val list2 = ArrayList<OrdersModel>()
    private lateinit var spf : SharedPref
    private var enabledHis : Boolean = false

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spf = SharedPref(this)
        count = findViewById(R.id.count)
        title = findViewById(R.id.title)
        totalCollected = findViewById(R.id.totalCollected)
        grandTotal = findViewById(R.id.grandTotal)
        ordersList = findViewById(R.id.orders_list)
        ordersList.layoutManager = LinearLayoutManager(this)
        ordersList.setHasFixedSize(true)
        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        history = findViewById(R.id.btnHistory)
        history.setOnClickListener {
            enabledHis = !enabledHis
            if (enabledHis){
                title.text = "Delivered"
                totalCollected.text = "Php 0.0"
            }else{
                title.text = "Delivery"
                totalCollected.text ="Total Collected Php ${spf.totalCollection}"
            }
            Toast.makeText(this, "Switching history", Toast.LENGTH_SHORT).show()
            fetchData()
        }
        swipeRefreshLayout.setOnRefreshListener {
            list.clear()
            fetchData()
            swipeRefreshLayout.isRefreshing = false
        }
        totalCollected.text ="Total Collected Php ${spf.totalCollection}"
        fetchData()
    }

    private fun fetchData() {
        list.clear()
        list2.clear()
        val url = "https://larapoultry.herokuapp.com/api/transactions"
        val stringRequest= object : StringRequest(
            Method.GET,url,
            Response.Listener{
                parseJson(it)
            },
            Response.ErrorListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }){}

        val queue = Volley.newRequestQueue(this)
        queue.add(stringRequest)
    }

    private fun parseJson(jsonResponse: String){
        try {
            var countItem = 0
            val ja = JSONArray(jsonResponse)
            var index = 0
            var grandT = 0.0
            while (index < ja.length() ){
                val jo = ja.getJSONObject(index)
                val id = jo.getInt("id")
                val code = jo.getString("trans_code")
                val status = jo.getString("status")
                val total = jo.getDouble("total_payment")
                val name = jo.getString("name")
                val userId = jo.getInt("user_id")
                val address = jo.getString("user_add")
                val payOpt = jo.getString("payment_opt")
                val phone = jo.getString("phone")
                val lat = jo.getString("lat")
                val long = jo.getString("long")
                var proofOfPayment = ""
                if (jo.has("proof_of_payment")){
                    proofOfPayment = jo.getString("proof_of_payment")
                }
                val dateToDeliver = jo.getString("date_to_deliver")
                val dateDelivered = jo.getString("date_delivered")
                if (status == "delivery" || status == "failed" && !enabledHis){
                    list.add(OrdersModel(id, code, status,total,userId,name,payOpt,address,phone,lat,long,dateToDeliver,proofOfPayment,dateDelivered))
                    grandT+=total
                    countItem++
                }
                if (status == "delivered" && enabledHis ){
                    grandT = 0.0
                    countItem = 0
                    list2.add(OrdersModel(id, code, status,total,userId,name,payOpt,address,phone,lat,long,dateToDeliver,proofOfPayment,dateDelivered))
                }
                index++
            }
            grandTotal.text = "Php ${grandT}"
            if (enabledHis){
                ordersList.adapter = OrdersAdapter(this,list2)
            }else{
                ordersList.adapter = OrdersAdapter(this,list)
            }
            count.text = "$countItem"
            if (list.isEmpty()){
                Toast.makeText(this,"No deliveries yet!",Toast.LENGTH_SHORT).show()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}