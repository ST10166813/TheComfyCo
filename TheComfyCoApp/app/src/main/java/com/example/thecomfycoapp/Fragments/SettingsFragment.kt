package com.example.thecomfycoapp.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.thecomfycoapp.R
import com.example.thecomfycoapp.utils.LanguageManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // Views
        val toolbar     = view.findViewById<MaterialToolbar>(R.id.settingsToolbar)
        val rgMode      = view.findViewById<RadioGroup>(R.id.rgMode)
        val rbLight     = view.findViewById<RadioButton>(R.id.rbLight)
        val rbDark      = view.findViewById<RadioButton>(R.id.rbDark)
        val actLanguage = view.findViewById<MaterialAutoCompleteTextView>(R.id.actLanguage)
        val actTextSize = view.findViewById<MaterialAutoCompleteTextView>(R.id.actTextSize)

        // Back behaviour
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // -------- THEME MODE --------
        when (prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)) {
            AppCompatDelegate.MODE_NIGHT_YES -> rbDark.isChecked = true
            else -> rbLight.isChecked = true
        }

        rgMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.rbDark)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            prefs.edit().putInt("theme_mode", mode).apply()
            AppCompatDelegate.setDefaultNightMode(mode)
            requireActivity().recreate()
        }

        // -------- LANGUAGE DROPDOWN (English / isiZulu / Afrikaans) --------
        val languageItems = listOf(
            getString(R.string.lang_english),    // 0 -> "en"
            getString(R.string.lang_isizulu),    // 1 -> "zu"
            getString(R.string.lang_afrikaans)   // 2 -> "af"
        )

        actLanguage.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languageItems)
        )

        // Read saved language ("en", "zu", "af") and pick correct index
        val savedLangTag = LanguageManager.getSavedLanguageTag(requireContext())
        val langIndex = when {
            savedLangTag.startsWith("zu") -> 1
            savedLangTag.startsWith("af") -> 2
            else -> 0  // default: English
        }
        actLanguage.setText(languageItems[langIndex], false)

        actLanguage.setOnItemClickListener { _, _, position, _ ->
            val langTag = when (position) {
                1 -> "zu"
                2 -> "af"
                else -> "en"
            }

            // Save + apply new locale
            LanguageManager.changeLanguage(requireContext(), langTag)

            // Recreate activity so all text reloads in new language
            requireActivity().recreate()
        }

        // -------- TEXT SIZE DROPDOWN --------
        val sizeItems = listOf(
            getString(R.string.text_size_small),
            getString(R.string.text_size_medium),
            getString(R.string.text_size_large)
        )

        actTextSize.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, sizeItems)
        )

        val savedSizeIndex = prefs.getInt("text_size_index", 0)
        actTextSize.setText(sizeItems[savedSizeIndex], false)

        actTextSize.setOnItemClickListener { _, _, position, _ ->
            prefs.edit().putInt("text_size_index", position).apply()
        }
    }
}
