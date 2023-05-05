/**
 * Auteur: David Pigeon
 * Date: 5 mai 2023
 * Version: 1.0
 * Description: Cette classe représente l'application elle-même et fournit un point d'entrée pour toutes les activités.
 *              On l'utilise pour changer la langue de l'application selon la langue choisie dans les préférences.
 */
package com.example.myapplication

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import java.util.Locale

class MyApp : Application() {
    // Est appelé lorsque l'application est créée
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
        updateViewsLanguage(base)
    }

    // Permet de changer la langue de l'application selon la langue choisie dans les préférences
    private fun updateBaseContextLocale(context: Context): Context {
        val language = getLanguageFromPreferences(context)
        val locale = Locale(language)
        Locale.setDefault(locale)

        return updateResourcesLocale(context, locale)
    }

    // Met à jour les configuration locale de l'application
    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    // Retourne la langue choisie dans les préférences
    private fun getLanguageFromPreferences(context: Context): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString("language", "en") ?: "en"
    }

    // Met à jour les vues de l'application selon la langue choisie dans les préférences
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
