package com.cielo.applibros

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cielo.applibros.data.local.preferences.SettingsPreferences
import com.cielo.applibros.domain.model.ThemeMode
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
class BookTrackerApplication : Application() {

    // Instancias globales de Firebase
    lateinit var analytics: FirebaseAnalytics
        private set

    lateinit var crashlytics: FirebaseCrashlytics
        private set

    override fun onCreate() {
        super.onCreate()

        // 1️⃣ Aplicar tema ANTES de cualquier UI
        applyTheme()

        // 2️⃣ Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // 3️⃣ Obtener instancias
        analytics = Firebase.analytics
        crashlytics = Firebase.crashlytics

        // 4️⃣ Configurar Crashlytics
        setupCrashlytics()

        // 5️⃣ Log de inicio
        logAppStart()
    }

    private fun applyTheme() {
        val prefs = SettingsPreferences(this)
        val theme = prefs.getSettings().themeMode

        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun setupCrashlytics() {
        // Habilitar recolección de crashlytics (incluye debug)
        crashlytics.setCrashlyticsCollectionEnabled(true)

        // Info personalizada
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)

        // Log inicial
        crashlytics.log("App initialized successfully")
    }

    private fun logAppStart() {
        // Evento de inicio en Analytics
        analytics.logEvent("app_start", null)
    }

    companion object {
        private const val TAG = "BookTrackerApp"
    }
}