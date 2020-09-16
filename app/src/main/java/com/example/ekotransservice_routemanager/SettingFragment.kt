package com.example.electroniktrack_tko

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import com.example.ekotransservice_routemanager.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        setPreferencesFromResource(R.xml.settings ,rootKey)
        val sharedPreferences = preferenceScreen.sharedPreferences
        val prefScreen = preferenceScreen
        goThroughPreferences(prefScreen as PreferenceGroup, sharedPreferences)
    }

    private fun goThroughPreferences(
        preferenceGroup: PreferenceGroup,
        sharedPreferences: SharedPreferences
    ) {
        val count = preferenceGroup.preferenceCount

        // Перебираем все элементы управления настройками и устанавливаем их описание текущими значениями
        for (i in 0 until count) {
            val p = preferenceGroup.getPreference(i)
            if (p is PreferenceGroup) {
                goThroughPreferences(p, sharedPreferences)
            } else {
                // You don't need to set up preference summaries for checkbox preferences because
                // they are already set up in xml using summaryOff and summary On
                if (p !is CheckBoxPreference) {
                    val value = sharedPreferences.getString(p.key, "")
                    setPreferenceSummary(p, value)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun setPreferenceSummary(preference: Preference, value: String?) {
        (preference as? EditTextPreference)?.summary = value
    }
}