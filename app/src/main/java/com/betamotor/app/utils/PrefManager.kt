package com.betamotor.app.utils

import android.content.Context
import com.google.gson.Gson
import com.betamotor.app.data.api.auth.User

class PrefManager(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveData(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getData(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun setToken(token: String) {
        saveData("access_token", token)
    }

    fun getToken(): String {
        return getData("access_token", "")
    }

    fun setMotorcycleTypeId(id: Int) {
        saveData("motorcycle_code", id.toString())
    }

    fun getMotorcycleTypeId(): Int? {
        return getData("motorcycle_code", "").toIntOrNull()
    }

    fun setSelectedMotorcycleId(id: String) {
        saveData("selected_motorcycle_id", id.toString())
    }

    fun getSelectedMotorcycleId(): String {
        return getData("selected_motorcycle_id", "")
    }

    fun clearSelectedMotorcycleId() {
        val editor = sharedPreferences.edit()
        editor.remove("selected_motorcycle_id")
        editor.apply()
    }

    // for now, use mac address for vin.
    fun setMotorcycleVIN(vin: String) {
        saveData("motorcycle_vin", vin)
    }

    fun getMotorcycleVIN(): String {
        return getData("motorcycle_vin", "")
    }

    fun setCurrentLanguage(language: String) {
        val editor = sharedPreferences.edit()
        editor.putString("language", language)
        editor.apply()
    }

    fun getUser(): User? {
        val userJSON = sharedPreferences.getString("user", null)
        return if (userJSON != null) {
            Gson().fromJson(userJSON, User::class.java)
        } else {
            null
        }
    }

    fun setUser(user: User?) {
        val editor = sharedPreferences.edit()
        val userJson = Gson().toJson(user)
        editor.putString("user", userJson)
        editor.apply()
    }

    fun getCurrentLanguage(): String {
        return sharedPreferences.getString("language", "it")!!
    }

    fun setRememberMe(flag: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("remember_me", flag)
        editor.apply()
    }

    fun getRememberMe(): Boolean {
        return sharedPreferences.getBoolean("remember_me", false)
    }

    fun setMacAddress(macAddress: String) {
        saveData("mac_address", macAddress)
    }

    fun getMacAddress(): String {
        return getData("mac_address", "null")
    }

    fun clearMacAddress() {
        val editor = sharedPreferences.edit()
        editor.remove("mac_address")
        editor.apply()
    }
}