package com.pogoaccountchecker.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import com.pogoaccountchecker.R;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        ListPreference delimiterPreference = findPreference(getString(R.string.delimiter_pref_key));
        delimiterPreference.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
            @Override
            public CharSequence provideSummary(ListPreference preference) {
                return preference.getValue();
            }
        });

        EditTextPreference webSocketPreference = findPreference(getString(R.string.webSocket_uri_pref_key));
        webSocketPreference.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
            @Override
            public CharSequence provideSummary(EditTextPreference preference) {
                String text = preference.getText();
                if (TextUtils.isEmpty(text)){
                    return "Not set";
                }
                return text;
            }
        });

        //Hide for now
        SwitchPreference withMadPreference = findPreference(getString(R.string.with_mad_pref_key));
        withMadPreference.setVisible(false);
        webSocketPreference.setVisible(false);
    }
}
