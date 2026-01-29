package com.cielo.applibros.data.local

import android.content.Context
import android.content.SharedPreferences
import com.cielo.applibros.R

data class UserProfile(
    val name: String,
    val avatarDrawing: String = "",
    val useInitial: Boolean = true,
    val memberSince: Long = System.currentTimeMillis()
)

class UserProfileHelper(private val context: Context) {

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
        // ✅ Usar string resource para el nombre por defecto
        val defaultName = context.getString(R.string.default_reader_name)

        return UserProfile(
            name = prefs.getString("name", defaultName) ?: defaultName,
            avatarDrawing = prefs.getString("avatar_drawing", "") ?: "",
            useInitial = prefs.getBoolean("use_initial", true),
            memberSince = prefs.getLong("member_since", System.currentTimeMillis())
        )
    }

    // ✅ MÉTODO CORREGIDO con string resources
    fun getReaderTitle(booksThisYear: Int): String {
        return when {
            booksThisYear == 0 -> context.getString(R.string.reader_title_new)
            booksThisYear < 5 -> context.getString(R.string.reader_title_casual)
            booksThisYear < 12 -> context.getString(R.string.reader_title_passionate)
            booksThisYear < 24 -> context.getString(R.string.reader_title_voracious)
            booksThisYear < 50 -> context.getString(R.string.reader_title_extreme)
            else -> context.getString(R.string.reader_title_legend)
        }
    }

    // ✅ MÉTODO CORREGIDO con string resource
    fun getInitialFromName(name: String): String {
        val defaultInitial = context.getString(R.string.default_initial)
        return name.firstOrNull()?.uppercase() ?: defaultInitial
    }
}