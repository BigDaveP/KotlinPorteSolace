package com.example.myapplication

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import java.util.Locale

class MyApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
        updateViewsLanguage(base)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val language = getLanguageFromPreferences(context)
        val locale = Locale(language)
        Locale.setDefault(locale)

        return updateResourcesLocale(context, locale)
    }

    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun getLanguageFromPreferences(context: Context): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString("language", "en") ?: "en"
    }

    private fun updateViewsLanguage(context: Context) {
        val resources = context.resources
        val configuration = resources.configuration
        val locale = getLanguageFromPreferences(context)
        Log.d("locale", locale)
        if (locale != null && !configuration.locale.equals(locale)) {
            configuration.setLocale(Locale(locale))
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

    }
}
