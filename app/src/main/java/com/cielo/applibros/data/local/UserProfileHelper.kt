package com.cielo.applibros.data.local

import android.content.Context
import android.content.SharedPreferences

data class UserProfile(
    val name: String = "Lector Apasionado",
    val avatarDrawing: String = "",  // Base64 del dibujo
    val useInitial: Boolean = true,  // Si usa inicial o dibujo
    val memberSince: Long = System.currentTimeMillis()
)

class UserProfileHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    fun saveProfile(
        name: String? = null,
        avatarDrawing: String? = null,
        useInitial: Boolean? = null
    ) {
        val current = getProfile()

        prefs.edit().apply {
            putString("name", name ?: current.name)
            putString("avatar_drawing", avatarDrawing ?: current.avatarDrawing)
            putBoolean("use_initial", useInitial ?: current.useInitial)

            if (!prefs.contains("member_since")) {
                putLong("member_since", System.currentTimeMillis())
            }
            apply()
        }
    }
    fun getProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("name", "Lector Apasionado") ?: "Lector Apasionado",
            avatarDrawing = prefs.getString("avatar_drawing", "") ?: "",
            useInitial = prefs.getBoolean("use_initial", true),
            memberSince = prefs.getLong("member_since", System.currentTimeMillis())
        )
    }

    fun getReaderTitle(booksThisYear: Int): String {
        return when {
            booksThisYear == 0 -> "Nuevo lector 📚"
            booksThisYear < 5 -> "Lector casual 📖"
            booksThisYear < 12 -> "Lector apasionado 🔥"
            booksThisYear < 24 -> "Lector voraz 🚀"
            booksThisYear < 50 -> "Lector extremo ⚡"
            else -> "Leyenda literaria 👑"
        }
    }

    fun getInitialFromName(name: String): String {
        return name.firstOrNull()?.uppercase() ?: "L"
    }
}