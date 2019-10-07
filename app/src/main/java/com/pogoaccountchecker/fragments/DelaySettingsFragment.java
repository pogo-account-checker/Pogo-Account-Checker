package com.pogoaccountchecker.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.pogoaccountchecker.R;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class DelaySettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.delay_preferences, rootKey);

        EditTextPreference.OnBindEditTextListener onBindEditTextListener = new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setSelection(editText.getText().length());
            }
        };

        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().isEmpty()) {
                    ((EditTextPreference) preference).setText("0");
                    return false;
                }
                return true;
            }
        };

        Preference.SummaryProvider summaryProvider = new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {
                String text = ((EditTextPreference) preference).getText();
                return text + " ms";
            }
        };

        EditTextPreference yearSelectorDelayPreference = findPreference(getString(R.string.year_selector_delay_pref_key));
        yearSelectorDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yearSelectorDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yob2010DelayPreference = findPreference(getString(R.string.yob_2010_delay_pref_key));
        yob2010DelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010DelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yob2010DelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference submitDobDelayPreference = findPreference(getString(R.string.submit_dob_delay_pref_key));
        submitDobDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        submitDobDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference returningPlayerDelayPreference = findPreference(getString(R.string.returning_player_delay_pref_key));
        returningPlayerDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        returningPlayerDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference ptcDelayPreference = findPreference(getString(R.string.ptc_delay_pref_key));
        ptcDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        ptcDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference safetyWarningDelayPreference = findPreference(getString(R.string.safety_warning_delay_pref_key));
        safetyWarningDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        safetyWarningDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        safetyWarningDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference notificationPopupDelayPreference = findPreference(getString(R.string.notification_popup_delay_pref_key));
        notificationPopupDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        notificationPopupDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        notificationPopupDelayPreference.setSummaryProvider(summaryProvider);

        EditTextPreference playerProfileDelayPreference = findPreference(getString(R.string.player_profile_delay_pref_key));
        playerProfileDelayPreference.setOnBindEditTextListener(onBindEditTextListener);
        playerProfileDelayPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        playerProfileDelayPreference.setSummaryProvider(summaryProvider);
    }
}
