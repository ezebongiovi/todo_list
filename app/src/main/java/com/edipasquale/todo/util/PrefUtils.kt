package com.edipasquale.todo.util

import android.app.Activity
import android.content.Context

private const val CONST_PREFS = "EXTRA_PREFS"
private const val CONST_PREF_AUTH = "EXTRA_PREF_AUTH"

class PrefUtils {

    init {
        throw AssertionError("Utility classes should not be instantiated")
    }

    companion object {

        /**
         * Saves the access token into the shared preferences
         *
         * @param context the application's context
         * @param token the token being saved
         */
        fun saveAccessToken(context: Context, token: String) {
            context.getSharedPreferences(CONST_PREFS, Activity.MODE_PRIVATE)
                .edit()
                .putString(CONST_PREF_AUTH, token)
                .apply()
        }

        /**
         * Fetches access token from shared preferences
         *
         * @param context the application's context
         */
        fun getAccessToken(context: Context) : String? {
            return context.getSharedPreferences(CONST_PREFS, Activity.MODE_PRIVATE)
                .getString(CONST_PREF_AUTH, null)
        }
    }
}