package com.example.poultryorderingriderapp

import android.content.Context
import android.content.SharedPreferences

class SharedPref(val context: Context) {
    private val mCol = "collected"
    private val mID = "uID"
    private val mName = "uName"
    private val mAddress = "uAddress"
    private val mEmail = "uEmail"
    private val mPhone = "uPhone"
    private val mToken = "uToken"
    private val PREF_NAME = "poultryapp"
    private var preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    val totalCollection : Int
        get() {
            return preferences.getInt(mCol,0)
        }
    val userID : Int
        get() {
            return preferences.getInt(mID,0)
        }
    val name : String?
        get() {
            return preferences.getString(mName, "")
        }
    val phone : String?
        get() {
            return preferences.getString(mPhone, "")
        }
    val email : String?
        get() {
            return preferences.getString(mEmail, "")
        }
    val userAddress : String?
        get() {
            return preferences.getString(mAddress, "")
        }
    fun hasToken(): Boolean {
        return preferences.getString(mToken,"") != ""
    }
    fun store(uID:Int, uName:String, uPhone:String, uAddress:String, uEmail:String, uToken:String){
       editor.putInt(mID, uID).putString(mName, uName).putString(mAddress, uAddress).putString(mPhone,uPhone)
            .putString(mEmail, uEmail).putString(mToken, uToken).apply()
    }
    fun storeCollection(num: Double){
        editor.putInt(mCol, num.toInt()).apply()
    }
    fun signOut(){
        editor.clear().apply()
    }
}