package com.cielo.applibros.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.cielo.applibros.R

class OnboardingNameFragment : Fragment() {

    private lateinit var etUserName: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUserName = view.findViewById(R.id.etUserName)

        // Guardar el nombre automáticamente al escribir
        etUserName.doAfterTextChanged { text ->
            (activity as? OnboardingActivity)?.setUserName(text?.toString()?.trim() ?: "")
        }
    }

    override fun onPause() {
        super.onPause()
        // Guardar el nombre al salir del fragment
        val name = etUserName.text.toString().trim()
        (activity as? OnboardingActivity)?.setUserName(name)
    }
}