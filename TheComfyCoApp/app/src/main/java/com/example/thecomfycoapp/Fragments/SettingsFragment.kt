package com.example.thecomfycoapp.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.thecomfycoapp.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        val rgMode  = view.findViewById<RadioGroup>(R.id.rgMode)
        val rbLight = view.findViewById<RadioButton>(R.id.rbLight)
        val rbDark  = view.findViewById<RadioButton>(R.id.rbDark)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        // Reflect saved mode
        when (prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.MODE_NIGHT_YES -> rbDark.isChecked = true
            else -> rbLight.isChecked = true
        }

        // Change + persist theme
        rgMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.rbDark)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            prefs.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
        }

//        // Back button -> always return to Home
//        btnBack.setOnClickListener {
//            val homeId = R.id.id_home_fragment // <-- replace if your home id differs
//            val nav = findNavController()
//            val popped = nav.popBackStack(homeId, false)
//            if (!popped) {
//                nav.navigate(homeId)
//            }
//        }
    }
}
