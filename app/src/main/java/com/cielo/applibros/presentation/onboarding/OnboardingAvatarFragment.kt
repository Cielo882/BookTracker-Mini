package com.cielo.applibros.presentation.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
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

        btnClear.setOnClickListener {
            avatarView.clear()
        }

        btnUseInitial.setOnClickListener {
            (activity as? OnboardingActivity)?.setUseInitial(true)
        }
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

        // Seleccionar negro por defecto
        highlightSelectedColor(colorButtons, 0)
    }

    private fun highlightSelectedColor(buttons: List<ImageButton>, selectedIndex: Int) {
        buttons.forEachIndexed { index, button ->
            button.alpha = if (index == selectedIndex) 1f else 0.4f
            button.scaleX = if (index == selectedIndex) 1.1f else 1f
            button.scaleY = if (index == selectedIndex) 1.1f else 1f
        }
    }


    override fun onPause() {
        super.onPause()
        // Guardar el dibujo
        val drawing = avatarView.getBitmapAsString()
        (activity as? OnboardingActivity)?.setAvatarDrawing(drawing)
    }
}