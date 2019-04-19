package com.pogoaccountchecker.fragments;

import android.os.Bundle;

import com.pogoaccountchecker.R;

import androidx.preference.PreferenceFragmentCompat;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
    }
}
