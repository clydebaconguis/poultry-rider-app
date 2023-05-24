package com.example.poultryorderingriderapp


data class OrdersModel(val id:Int, var transaction:String, var status:String,
                       val total: Double, val userId: Int, val userName: String,
                       val payOpt: String, val address: String, val phone: String,
                       val lat:String, val long:String, val dateToDeliver:String, val proofOfPayment:String, val dateDelivered:String) {
}

