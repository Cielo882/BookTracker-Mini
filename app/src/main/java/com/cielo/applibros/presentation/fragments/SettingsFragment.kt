package com.cielo.applibros.presentation.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.cielo.applibros.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Language
import com.cielo.applibros.domain.model.ThemeMode
import com.cielo.applibros.presentation.viewmodel.SettingsViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.BuildConfig

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var rgLanguage: RadioGroup
    private lateinit var rbSpanish: RadioButton
    private lateinit var rbEnglish: RadioButton

    private lateinit var rgTheme: RadioGroup
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton
    private lateinit var rbSystem: RadioButton

    private lateinit var btnAccept: MaterialButton

    // Variables para guardar las selecciones temporales
    private var tempLanguage: Language? = null
    private var tempTheme: ThemeMode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).getSettingsViewModel()

        setupViews(view)
        observeSettings()
        setupListeners()
        setupFirebaseTestButtons(view)

    }

    private fun setupViews(view: View) {
        rgLanguage = view.findViewById(R.id.rgLanguage)
        rbSpanish = view.findViewById(R.id.rbSpanish)
        rbEnglish = view.findViewById(R.id.rbEnglish)

        rgTheme = view.findViewById(R.id.rgTheme)
        rbLight = view.findViewById(R.id.rbLight)
        rbDark = view.findViewById(R.id.rbDark)
        rbSystem = view.findViewById(R.id.rbSystem)

        btnAccept = view.findViewById(R.id.btnAccept)
    }

    private fun observeSettings() {
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            // Actualizar UI según configuración guardada
            when (settings.language) {
                Language.SPANISH -> rbSpanish.isChecked = true
                Language.ENGLISH -> rbEnglish.isChecked = true
            }

            when (settings.themeMode) {
                ThemeMode.LIGHT -> rbLight.isChecked = true
                ThemeMode.DARK -> rbDark.isChecked = true
                ThemeMode.SYSTEM -> rbSystem.isChecked = true
            }

            // Inicializar valores temporales con los valores actuales
            if (tempLanguage == null) {
                tempLanguage = settings.language
            }
            if (tempTheme == null) {
                tempTheme = settings.themeMode
            }

            updateAcceptButtonState()
        }
    }

    private fun setupListeners() {
        // Listener para idioma - solo guarda temporalmente
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            tempLanguage = when (checkedId) {
                R.id.rbSpanish -> Language.SPANISH
                R.id.rbEnglish -> Language.ENGLISH
                else -> tempLanguage
            }
            updateAcceptButtonState()
        }

        // Listener para tema - solo guarda temporalmente
        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            tempTheme = when (checkedId) {
                R.id.rbLight -> ThemeMode.LIGHT
                R.id.rbDark -> ThemeMode.DARK
                R.id.rbSystem -> ThemeMode.SYSTEM
                else -> tempTheme
            }
            updateAcceptButtonState()
        }


        // Botón Aceptar - aplica los cambios
        btnAccept.setOnClickListener {
            applyChanges()
        }
    }

    private fun updateAcceptButtonState() {
        // Habilitar el botón solo si hay cambios pendientes
        val currentSettings = viewModel.settings.value
        val hasChanges = currentSettings?.let {
            tempLanguage != it.language || tempTheme != it.themeMode
        } ?: false

        btnAccept.isEnabled = hasChanges

        // Cambiar el texto del botón si hay cambios
        if (hasChanges) {
            btnAccept.text = "✓ Guardar cambios"
            btnAccept.alpha = 1.0f
        } else {
            btnAccept.text = "✓ Sin cambios"
            btnAccept.alpha = 0.6f
        }
    }

    private fun applyChanges() {
        val currentSettings = viewModel.settings.value ?: return
        val languageChanged = tempLanguage != currentSettings.language
        val themeChanged = tempTheme != currentSettings.themeMode

        // --- CAMBIO SUGERIDO: Aplicar el tema PRIMERO si ha cambiado ---
        tempTheme?.let {
            if (themeChanged) {
                // 1. Guardar y aplicar el TEMA. Esto no reinicia la actividad.
                viewModel.updateTheme(it)
                applyTheme(it)
            }
        }
        // ----------------------------------------------------------------

        // --- Aplicar el idioma DESPUÉS si ha cambiado ---
        tempLanguage?.let {
            if (languageChanged) {
                // 2. Guardar el IDIOMA. Esto probablemente reinicie la actividad,
                // pero el tema ya está guardado y aplicado en la configuración.
                viewModel.updateLanguage(it)
            }
        }
        // ---------------------------------------------------


        // Mostrar mensaje (Asegúrate de que este diálogo se pueda mostrar
        // ANTES del reinicio, o considera mostrarlo en la nueva actividad)
        if (languageChanged || themeChanged) {
             //showChangesAppliedDialog(languageChanged, themeChanged)
        }
    }

    private fun applyTheme(theme: ThemeMode) {
        val mode = when (theme) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun showChangesAppliedDialog(languageChanged: Boolean, themeChanged: Boolean) {
        val message = buildString {
            append("✓ Cambios aplicados correctamente\n\n")

            if (languageChanged) {
                append("• Idioma actualizado: ")
                append(when (tempLanguage) {
                    Language.SPANISH -> "Español"
                    Language.ENGLISH -> "English"
                    else -> ""
                })
                append("\n")
            }

            if (themeChanged) {
                append("• Tema actualizado: ")
                append(when (tempTheme) {
                    ThemeMode.LIGHT -> "Modo claro"
                    ThemeMode.DARK -> "Modo oscuro"
                    ThemeMode.SYSTEM -> "Según el sistema"
                    else -> ""
                })
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Configuración actualizada")
            .setMessage(message)
            .setPositiveButton("Entendido") { _, _ ->
                // Opcional: volver a la pantalla anterior
                // activity?.onBackPressed()
            }
            .show()
    }

    private fun setupFirebaseTestButtons(view: View) {

        val mainActivity = activity as? MainActivity ?: return
        val analyticsHelper = mainActivity.getAnalyticsHelper()
        val crashlyticsHelper = mainActivity.getCrashlyticsHelper()

        // -----------------------------------------
        // BOTÓN 1: PROBAR ANALYTICS (NO CRASH)
        // -----------------------------------------
        view.findViewById<Button>(R.id.btnTestAnalytics)?.setOnClickListener {

            analyticsHelper.logFeatureUsed("firebase_test_analytics")
            analyticsHelper.logScreenView("TestScreen")

            crashlyticsHelper.logUserAction("Analytics Test Button Clicked")

            Toast.makeText(
                requireContext(),
                "✅ Evento de Analytics enviado (revisa Firebase en unos minutos)",
                Toast.LENGTH_LONG
            ).show()
        }

        // -----------------------------------------
        // BOTÓN 2: PROBAR ERROR NO FATAL
        // -----------------------------------------
        view.findViewById<Button>(R.id.btnTestCrashlytics)?.setOnClickListener {

            crashlyticsHelper.testNonFatalError()

            Toast.makeText(
                requireContext(),
                "⚠️ Error NO fatal enviado a Crashlytics",
                Toast.LENGTH_LONG
            ).show()
        }

        // -----------------------------------------
        // BOTÓN 3: FORZAR CRASH REAL (FATAL)
        // -----------------------------------------
        view.findViewById<Button>(R.id.btnForceCrash)?.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Advertencia")
                .setMessage(
                    "Esto forzará un crash REAL y cerrará la app.\n\n" +
                            "Úsalo solo para verificar Crashlytics.\n\n" +
                            "¿Continuar?"
                )
                .setPositiveButton("Sí, forzar crash") { _, _ ->

                    // 🔥 CRASH REAL — NO try/catch
                    crashlyticsHelper.forceCrashForTesting()

                    // Alternativa oficial (también válida):
                    // FirebaseCrashlytics.getInstance().crash()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }


}