package com.example.pulsefit_app

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

// Handles English, isiZulu and Sesotho
object LocaleHelper {

    val tags = listOf("en", "zu", "st")

    val names = listOf("English", "isiZulu", "Sesotho")

    fun currentTag(context: Context): String {
        val saved = AppPrefs.get(context).getString(AppPrefs.KEY_LANGUAGE, null)
        if (saved != null && saved in tags) return saved

        val phoneLanguage = Locale.getDefault().language
        return if (phoneLanguage in tags) phoneLanguage else "en"
    }

    fun apply(context: Context, tag: String) {
        if (tag !in tags) return

        AppPrefs.get(context).edit().putString(AppPrefs.KEY_LANGUAGE, tag).apply()
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(tag)
        )
    }

    fun wrap(context: Context): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(currentTag(context)))
        return context.createConfigurationContext(configuration)
    }
}