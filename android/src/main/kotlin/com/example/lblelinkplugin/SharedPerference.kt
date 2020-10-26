package com.example.lblelinkplugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreference<T>(val name: String, val default: T) {

    val prefs: SharedPreferences by lazy { LblelinkpluginPlugin.ctx!!.getSharedPreferences(name, Context.MODE_PRIVATE) }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getSharedPreferences(name, default)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putSharedPreferences(name, value)
    }

    @SuppressLint("CommitPrefEdits")
    private fun putSharedPreferences(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("SharedPreferences can't be save this type")
        }.apply()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSharedPreferences(name: String, default: T): T = with(prefs) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)?:0L
            is String -> getString(name, default)?:""
            is Int -> getInt(name, default)?:0
            is Boolean -> getBoolean(name, default)?:false
            is Float -> getFloat(name, default)?:0f
            else -> throw IllegalArgumentException("SharedPreferences can't be get this type")
        }
        return res as T
    }
}