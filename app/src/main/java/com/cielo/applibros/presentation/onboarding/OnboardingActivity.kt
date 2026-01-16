package com.cielo.applibros.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cielo.applibros.R
import com.cielo.applibros.data.local.UserProfileHelper
import com.cielo.applibros.MainActivity
import com.cielo.applibros.presentation.views.DrawableAvatarView
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.content.edit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView

    private var userName = ""
    private var avatarDrawing = ""
    private var useInitial = false

    private var onlyAvatar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        onlyAvatar = intent.getBooleanExtra("only_avatar", false)

        setupViews()
        if (onlyAvatar) {
            viewPager.currentItem = 2
            btnSkip.visibility = View.GONE

            val profileHelper = UserProfileHelper(this)
            val profile = profileHelper.getProfile()

            userName = profile.name
            avatarDrawing = profile.avatarDrawing
            useInitial = profile.useInitial

            Log.d("Onboarding", "Loaded for edit:")
            Log.d("Onboarding", "useInitial=$useInitial len=${avatarDrawing.length}")
        }
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)

        val adapter = OnboardingAdapter(this, avatarDrawing)
        viewPager.adapter = adapter

        viewPager.isUserInputEnabled = true


        btnNext.setOnClickListener {
            //  AGREGAR: Guardar avatar ANTES de continuar
            if (viewPager.currentItem == 2) {
                saveCurrentAvatar()  // Guardar el dibujo
            }

            if (viewPager.currentItem < 2) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        btnNext.text = "Comenzar"
                        btnSkip.visibility = android.view.View.VISIBLE
                        viewPager.isUserInputEnabled = true  //  Permitir swipe

                    }
                    1 -> {
                        btnNext.text = "Siguiente"
                        btnSkip.visibility = android.view.View.VISIBLE
                        viewPager.isUserInputEnabled = true  //  Permitir swipe
                    }
                    2 -> {
                        btnNext.text = "¡Empezar a leer!"
                        btnSkip.visibility = android.view.View.GONE
                        viewPager.isUserInputEnabled = false  // ❌ Deshabilitar swipe
                    }
                }
            }
        })
    }

    fun setUserName(name: String) {
        userName = name
    }

    fun setAvatarDrawing(drawing: String) {
        Log.d("Onboarding", "setAvatarDrawing called, length: ${drawing.length}")
        avatarDrawing = drawing
        useInitial = false
    }

    fun setUseInitial(use: Boolean) {
        useInitial = use
        if (use) avatarDrawing = ""
    }


    fun finishOnboarding() {
        val profileHelper = UserProfileHelper(this)

        val finalName =     userName.ifEmpty { "Lector Apasionado" }

        //  DEBUG: Ver qué se guarda
        Log.d("Onboarding", "Saving profile:")
        Log.d("Onboarding", "  Name: $finalName")
        Log.d("Onboarding", "  Use Initial: $useInitial")
        Log.d("Onboarding", "  Avatar Drawing length: ${avatarDrawing.length}")

        profileHelper.saveProfile(
            name = finalName,
            avatarDrawing = if (!useInitial) avatarDrawing else "",
            useInitial = useInitial
        )

        //  DEBUG: Verificar que se guardó
        val saved = profileHelper.getProfile()
        Log.d("Onboarding", "Profile saved:")
        Log.d("Onboarding", "  Name: ${saved.name}")
        Log.d("Onboarding", "  Use Initial: ${saved.useInitial}")
        Log.d("Onboarding", "  Avatar Drawing length: ${saved.avatarDrawing.length}")

        //  Si solo era avatar, volver a MainActivity
        if (onlyAvatar) {
            finish()  // Solo cerrar y volver
            return
        }
        // Marcar onboarding como completado
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit {
                putBoolean("onboarding_completed", true)
            }

        // Ir a MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    fun saveCurrentAvatar() {
        // Obtener el fragment actual
        val currentFragment = supportFragmentManager.fragments.lastOrNull()

        if (currentFragment is OnboardingAvatarFragment) {
            val drawing = currentFragment.getAvatarDrawing()
            Log.d("Onboarding", "Saving avatar from button, length: ${drawing.length}")

            if (drawing.isNotEmpty()) {
                setAvatarDrawing(drawing)
                setUseInitial(false)
            }
        }
    }


    companion object {
        fun shouldShowOnboarding(context: Context): Boolean {
            return !context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getBoolean("onboarding_completed", false)
        }
    }
}