package com.cielo.applibros.presentation.onboarding

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.cielo.applibros.R
import com.cielo.applibros.presentation.views.DrawableAvatarView
class OnboardingAvatarFragment : Fragment() {

    private lateinit var avatarView: DrawableAvatarView
    private lateinit var btnClear: Button
    private lateinit var btnUseInitial: Button

    private val colors = listOf(
        Color.BLACK,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.parseColor("#FFC107"), // Amarillo
        Color.parseColor("#9C27B0")  // Morado
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_avatar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatarView = view.findViewById(R.id.avatarView)
        btnClear = view.findViewById(R.id.btnClear)
        btnUseInitial = view.findViewById(R.id.btnUseInitial)

        setupColorButtons(view)

        // 🔄 Restaurar dibujo si existe
        arguments?.getString(ARG_DRAWING)?.let { base64 ->
            if (base64.isNotEmpty()) {
                avatarView.setBitmapFromString(base64)
                Log.d("AvatarFragment", "Avatar restored, len=${base64.length}")
            }
        }

        // 🧹 Borrar dibujo
        btnClear.setOnClickListener {
            avatarView.clear()
        }

        // 🔤 Usar inicial
        btnUseInitial.setOnClickListener {
            val activity = activity as? OnboardingActivity ?: return@setOnClickListener
            activity.setUseInitial(true)
            activity.setAvatarDrawing("")
            activity.finishOnboarding()
        }
    }

    // 🔒 CLAVE: bloquear swipe del ViewPager al entrar
    override fun onResume() {
        super.onResume()
        (activity as? OnboardingActivity)
            ?.findViewById<ViewPager2>(R.id.viewPager)
            ?.isUserInputEnabled = false
    }

    // 🔓 Volver a habilitar swipe al salir
    override fun onPause() {
        super.onPause()

        val drawing = avatarView.getBitmapAsString()
        if (drawing.isNotEmpty()) {
            (activity as? OnboardingActivity)?.setAvatarDrawing(drawing)
        }

        (activity as? OnboardingActivity)
            ?.findViewById<ViewPager2>(R.id.viewPager)
            ?.isUserInputEnabled = true
    }

    private fun setupColorButtons(view: View) {
        val colorButtons = listOf(
            view.findViewById<ImageButton>(R.id.btnColor1),
            view.findViewById<ImageButton>(R.id.btnColor2),
            view.findViewById<ImageButton>(R.id.btnColor3),
            view.findViewById<ImageButton>(R.id.btnColor4),
            view.findViewById<ImageButton>(R.id.btnColor5),
            view.findViewById<ImageButton>(R.id.btnColor6)
        )

        colorButtons.forEachIndexed { index, button ->
            button.setBackgroundColor(colors[index])
            button.setOnClickListener {
                avatarView.currentColor = colors[index]
                highlightSelectedColor(colorButtons, index)
            }
        }

        highlightSelectedColor(colorButtons, 0)
    }

    private fun highlightSelectedColor(
        buttons: List<ImageButton>,
        selectedIndex: Int
    ) {
        buttons.forEachIndexed { index, button ->
            button.alpha = if (index == selectedIndex) 1f else 0.4f
            button.scaleX = if (index == selectedIndex) 1.1f else 1f
            button.scaleY = if (index == selectedIndex) 1.1f else 1f
        }
    }

    fun getAvatarDrawing(): String {
        return avatarView.getBitmapAsString()
    }

    companion object {
        private const val ARG_DRAWING = "arg_drawing"

        fun newInstance(drawing: String): OnboardingAvatarFragment {
            return OnboardingAvatarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DRAWING, drawing)
                }
            }
        }
    }
}
