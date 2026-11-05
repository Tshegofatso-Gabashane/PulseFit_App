package com.example.pulsefit_app

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

// Handles the in-app language (English, Afrikaans, isiZulu, Setswana)
object LocaleHelper {

    // Language codes, in the same order as the Settings spinner
    val tags = listOf("en", "af", "zu", "tn")

    // Names are shown in their own language so people can always find theirs
    val names = listOf("English", "Afrikaans", "isiZulu", "Setswana")

    // The language the app is using: the saved choice, else the phone's language, else English
    fun currentTag(context: Context): String {
        val saved = AppPrefs.get(context).getString(AppPrefs.KEY_LANGUAGE, null)
        if (saved != null && saved in tags) return saved

        val phoneLanguage = Locale.getDefault().language
        return if (phoneLanguage in tags) phoneLanguage else "en"
    }

    // Saves the choice and switches the language. Open screens restart by themselves.
    fun apply(context: Context, tag: String) {
        AppPrefs.get(context).edit().putString(AppPrefs.KEY_LANGUAGE, tag).apply()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }

    // For code that runs without a screen (like push notifications) so text uses the chosen language
    fun wrap(context: Context): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(currentTag(context)))
        return context.createConfigurationContext(configuration)
    }
}