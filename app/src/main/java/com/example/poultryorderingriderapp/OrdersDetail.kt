package com.example.poultryorderingriderapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.*
import java.lang.RuntimeException
import java.util.*
import kotlin.math.min

class OrdersDetail : AppCompatActivity() {
    private lateinit var orderId: TextView
    private lateinit var orderName: TextView
    private lateinit var orderAddress: TextView
    private lateinit var orderPhone: TextView
    private lateinit var orderStatus: TextView
    private lateinit var orderPaymentMethod: TextView
    private lateinit var orderTotal: TextView
    private lateinit var totalHeading: TextView
    private lateinit var call: Button
    private lateinit var locate: Button
    private lateinit var btnConfirm: Button
    private lateinit var btnFailed: Button
    private lateinit var btnHome: TextView
    private lateinit var btnTakePic: ImageView
    private lateinit var loading: ProgressBar
    private var rowId = 0
    private var phone = ""
    private var lat = ""
    private var long = ""
    private var paymentMethod = ""
    private var proofOfPayment = ""
    private var dateToDeliver = ""
    private var ordStat = ""
    private var total = 0.0
    private lateinit var spf : SharedPref
    private var imageData: ByteArray? = null
    private val loadImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        btnTakePic.setImageURI(it)
        if (it != null) {
            createImageData(it)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_detail)
        spf = SharedPref(this)
        loading = findViewById(R.id.progressBar)
        orderId = findViewById(R.id.item_code)
        orderName = findViewById(R.id.item_user_name)
        orderAddress = findViewById(R.id.item_address)
        orderPhone = findViewById(R.id.item_phone)
        orderStatus = findViewById(R.id.item_status)
        orderPaymentMethod = findViewById(R.id.item_payment_method)
        orderTotal = findViewById(R.id.item_total)
        totalHeading = findViewById(R.id.total_heading)
        call = findViewById(R.id.buttonCall)
        btnConfirm = findViewById(R.id.buttonConfirm)
        locate = findViewById(R.id.buttonLocate)
        btnTakePic = findViewById(R.id.take_picture)
        btnHome = findViewById(R.id.btnHome)
        btnFailed = findViewById(R.id.buttonFailed)
        btnFailed.setOnClickListener {
            if (ordStat != "failed"){
                failedDelivery()
            }
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnTakePic.setOnClickListener {
            loadImage.launch("image/*")
        }
        if(intent.hasExtra("order_id")){
            lat = intent.getStringExtra("order_lat").toString()
            long = intent.getStringExtra("order_long").toString()
            rowId = intent.getIntExtra("order_id", 0)
            orderId.text = "Order ID: " + intent.getStringExtra("order_code")
            orderName.text = "Name: " + intent.getStringExtra("order_name")
            orderAddress.text = "Address: " + intent.getStringExtra("order_address")
            phone = intent.getStringExtra("order_phone").toString()
            orderPhone.text = "Phone: $phone"
            total = intent.getDoubleExtra("order_total", 0.0)
            paymentMethod = intent.getStringExtra("order_payment_method").toString()
            if (paymentMethod == "GCASH"){
                orderTotal.text = "Total: Php 0.0"
                totalHeading.text = "Php 0.0"
            }else{
                orderTotal.text = "Total: Php$total"
                totalHeading.text = "Php $total"
            }
            proofOfPayment = intent.getStringExtra("proof_of_payment").toString()
            dateToDeliver = intent.getStringExtra("date_to_deliver").toString()
            orderPaymentMethod.text = "Payment Method: $paymentMethod \nRef: $proofOfPayment"
            ordStat = intent.getStringExtra("order_status").toString()
            orderStatus.text = "Status: $ordStat \nExpected date to deliver: $dateToDeliver"
            loading.isVisible = false
        }
        call.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL);
            intent.data = Uri.parse("tel: $phone")
            startActivity(intent)
        }
        locate.setOnClickListener {
            try {
                val uri: String = java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", lat.toDouble(), long.toDouble())
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            } catch (e: NumberFormatException) {
                Toast.makeText(this,"Invalid lat long!", Toast.LENGTH_SHORT).show()
            }
        }
        btnConfirm.setOnClickListener {
            uploadProduct()
        }
    }
//    fun isValidLatLang(latitude: Double?, longitude: Double?): Boolean {
//        return latitude?.toInt() in -90 until 90 && longitude?.toInt() in -180 until 180
//    }
    private fun failedDelivery(){
    loading.isVisible = true
    val url = "https://larapoultry.herokuapp.com/api/transactions/${rowId}"
    val request = object:StringRequest(
        Method.PUT,url,
        Response.Listener{
            loading.isVisible = false
            Toast.makeText(this,"Rescheduled Successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },
        Response.ErrorListener {
            loading.isVisible = false
            Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
        }){
        override fun getParams(): MutableMap<String, String> {
            val params= HashMap<String, String>()
            params["status"] = "failed"
            params["orderId"] = rowId.toString()
            return params
        }
    }
    val queue = Volley.newRequestQueue(this)
    queue.add(request)
}
    private fun createImageData(uri: Uri){
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use{
            imageData = it.readBytes()
        }
    }
    private fun uploadProduct(){
        imageData?: return
        loading.isVisible = true
        val url = "https://larapoultry.herokuapp.com/api/transactions"
        val request2 = object:VolleyFileUploadRequest(
            Method.POST,url,
            Response.Listener{
                loading.isVisible = false
                spf.storeCollection(spf.totalCollection + total)
                Toast.makeText(this,"Success!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            },
            Response.ErrorListener {
                loading.isVisible = false
                Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show()
            }){
            override fun getByteData(): MutableMap<String, FileDataPart> {
                val params= HashMap<String, FileDataPart>()
                params["image"] = FileDataPart("image", imageData!!, "jpeg")
                return params
            }

            override fun getParams(): MutableMap<String, String> {
                val params= HashMap<String, String>()
                params["status"] = "delivered"
                params["payment"] = paymentMethod
                params["id"] = rowId.toString()
                return params
            }
        }
        Volley.newRequestQueue(this).add(request2)
    }

}

open class VolleyFileUploadRequest(
    method:Int,
    url:String,
    listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener) : Request<NetworkResponse>(method, url, errorListener){
    private var responseListener: Response.Listener<NetworkResponse>? = null
    init {
        this.responseListener = listener
    }

    private var headers: Map<String, String>? = null
    private val divider: String = "--"
    private val ending = "\r\n"
    private val boundary = "imageRequest${System.currentTimeMillis()}"


    override fun getHeaders(): MutableMap<String, String> {
        return when (headers) {
            null -> super.getHeaders()
            else -> headers!!.toMutableMap()
        }
    }

    override fun getBodyContentType() = "multipart/form-data;boundary=$boundary"

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)
        try {
            if (params!=null && params!!.isNotEmpty()){
                processParams(dataOutputStream, params!!, paramsEncoding)
            }
            val data = getByteData() as Map<String, FileDataPart>?
            if(data!= null && data.isNotEmpty()){
                processData(dataOutputStream, data)
            }
            dataOutputStream.writeBytes(divider + boundary + divider + ending)
            return byteArrayOutputStream.toByteArray()

        }catch (e: IOException){
            e.printStackTrace()
        }
        return super.getBody()
    }

    @Throws(AuthFailureError::class)
    open fun getByteData(): Map<String, Any>?{
        return null
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        }catch (e: Exception){
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse?) {
        responseListener?.onResponse(response)
    }

    override fun deliverError(error: VolleyError?) {
        errorListener?.onErrorResponse(error)
    }

    @Throws(IOException::class)
    private fun processParams(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding:String){
        try {
            params.forEach {
                dataOutputStream.writeBytes(divider + boundary + ending)
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"$ending")
                dataOutputStream.writeBytes(ending)
                dataOutputStream.writeBytes(it.value + ending)
            }
        }catch (e: UnsupportedEncodingException){
            throw RuntimeException("Unsupported encoding not supported: $encoding with error: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    private fun processData(dataOutputStream: DataOutputStream, data: Map<String, FileDataPart>){
        data.forEach {
            val dataFile = it.value
            dataOutputStream.writeBytes("$divider$boundary$ending")
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"; filename=\"${dataFile.filename}\"$ending")
            if (dataFile.type.trim().isNotEmpty()){
                dataOutputStream.writeBytes("Content-Type: ${dataFile.type}$ending")
            }
            dataOutputStream.writeBytes(ending)
            val fileInputStream = ByteArrayInputStream(dataFile.data)
            var bytesAvailable = fileInputStream.available()
            val maxBufferSize = 1024 * 1024
            var bufferSize = min(bytesAvailable, maxBufferSize)
            val buffer = ByteArray(bufferSize)
            var bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            while (bytesRead > 0){
                dataOutputStream.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize =  min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            }
            dataOutputStream.writeBytes(ending)
        }
    }

}

class FileDataPart(var filename:String?, var data:ByteArray, var type:String)