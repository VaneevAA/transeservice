package com.example.ekotransservice_routemanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

public class SettingFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings,rootKey);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();

        goThroughPreferences((PreferenceGroup) prefScreen,sharedPreferences);

    }

    private void goThroughPreferences(PreferenceGroup preferenceGroup, SharedPreferences sharedPreferences) {

        int count = preferenceGroup.getPreferenceCount();

        // Перебираем все элементы управления настройками и устанавливаем их описание текущими значениями
        for (int i = 0; i < count; i++) {
            Preference p = preferenceGroup.getPreference(i);
            if (p instanceof PreferenceGroup) {
                goThroughPreferences((PreferenceGroup) p, sharedPreferences);
            }else {
            // You don't need to set up preference summaries for checkbox preferences because
            // they are already set up in xml using summaryOff and summary On
                if (!(p instanceof CheckBoxPreference)) {
                    String value = sharedPreferences.getString(p.getKey(), "");
                    setPreferenceSummary(p, value);
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof EditTextPreference) {
            preference.setSummary(value);
        }
    }

}
