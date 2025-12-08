package com.example.firealarm.presentation.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferences {
    companion object{
        private const val TOKEN_PREF = "token_prefs"
        private const val USER_REF = "user_prefs"
        private const val DEVICE_REF = "device_refs"
        private lateinit var sharedPref: SharedPreferences
        fun init(context: Context){
            sharedPref = context.getSharedPreferences(TOKEN_PREF, Context.MODE_PRIVATE)
        }

        fun saveToken(token: String) {
            sharedPref.edit().putString(TOKEN_PREF, token).apply()
        }


        fun getToken(): String? {
            return sharedPref.getString(TOKEN_PREF, null)
        }

        fun saveUsername(username: String){
            sharedPref.edit().putString(USER_REF, username).apply()
        }

        fun getUsername(): String?{
            return sharedPref.getString(USER_REF, null)
        }

        fun savePhone(phone: String){
            sharedPref.edit().putString(USER_REF, phone).apply()
        }

        fun getPhone(): String?{
            return sharedPref.getString(USER_REF, null)
        }

        fun saveDeviceId(id: String){
            sharedPref.edit().putString(DEVICE_REF, id).apply()
        }

        fun getDeviceId(): String?{
            return sharedPref.getString(DEVICE_REF, null)
        }
    }

}