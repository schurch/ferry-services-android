package com.stefanchurch.ferryservices

import android.content.Context

interface Preferences {
    fun lookupBool(key: Int) : Boolean
    fun lookupString(key: Int) : String?
}

class SharedPreferences(private val context: Context): Preferences {
    private val prefs = context.getSharedPreferences(context.getString(R.string.preferences_key), Context.MODE_PRIVATE)

    override fun lookupBool(key: Int): Boolean {
        val key = context.resources.getString(key)
        return prefs.getBoolean(key, false)
    }

    override fun lookupString(key: Int): String? {
        val key = context.resources.getString(key)
        return prefs.getString(key, null)
    }
}