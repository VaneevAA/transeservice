package com.example.ekotransservice_routemanager

import android.content.SharedPreferences
import android.icu.text.CaseMap
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.preference.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val portPreference: EditTextPreference? = findPreference("URL_PORT")

        portPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        val urlPreference: EditTextPreference? = findPreference("URL_NAME")

        urlPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }

        val passwordPreference: EditTextPreference? = findPreference("URL_AUTHPASS")

        passwordPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val datePreference: EditTextPreference? = findPreference("DATE")
        datePreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_DATETIME_VARIATION_DATE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //requireContext().getTheme().applyStyle(R.style.PreferenceScreen, true);
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.background = requireActivity().getDrawable(R.drawable.pictures_back)
        /*for( child in (view as ViewGroup).children){
            //child.background = requireActivity().getDrawable(R.drawable.point_back)
            if (child is ViewGroup){
                for (grandChild in (child as ViewGroup).children){
                    grandChild.background = requireActivity().getDrawable(R.drawable.point_back)
                }
            }
        }*/

        return view
    }

    
}