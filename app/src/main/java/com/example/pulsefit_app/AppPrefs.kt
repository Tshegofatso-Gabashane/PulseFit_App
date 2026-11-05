package com.example.pulsefit_app

import android.content.Context
import android.content.SharedPreferences

// One place for the SharedPreferences name and keys used by Settings, sync and push.
object AppPrefs {

    private const val NAME = "PulseFitSettings"

    const val KEY_AUTO_SYNC = "sync_enabled"
    const val KEY_PUSH = "push_enabled"
    const val KEY_LAST_SYNC = "last_sync_time"
    const val KEY_LANGUAGE = "app_language"
    const val KEY_UNITS = "units_position"

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}