package com.cielo.applibros.presentation.onboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView

    private var userName = ""
    private var avatarDrawing = ""
    private var useInitial = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        setupViews()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)

        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        btnNext.setOnClickListener {
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
                    }
                    1 -> {
                        btnNext.text = "Siguiente"
                        btnSkip.visibility = android.view.View.VISIBLE
                    }
                    2 -> {
                        btnNext.text = "¡Empezar a leer!"
                        btnSkip.visibility = android.view.View.GONE
                    }
                }
            }
        })
    }

    fun setUserName(name: String) {
        userName = name
    }

    fun setAvatarDrawing(drawing: String) {
        avatarDrawing = drawing
        useInitial = false
    }

    fun setUseInitial(use: Boolean) {
        useInitial = use
    }

    private fun finishOnboarding() {
        val profileHelper = UserProfileHelper(this)

        val finalName = userName.ifEmpty { "Lector Apasionado" }

        profileHelper.saveProfile(
            name = finalName,
            avatarDrawing = if (!useInitial) avatarDrawing else "",
            useInitial = useInitial
        )

        // Marcar onboarding como completado
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply()

        // Ir a MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        fun shouldShowOnboarding(context: Context): Boolean {
            return !context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getBoolean("onboarding_completed", false)
        }
    }
}