package com.pogoaccountchecker.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import com.pogoaccountchecker.R;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class ButtonSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.button_preferences, rootKey);

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
                return text + " px";
            }
        };

        EditTextPreference yearSelectorXPreference = findPreference(getString(R.string.year_selector_x_pref_key));
        yearSelectorXPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yearSelectorXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yearSelectorYPreference = findPreference(getString(R.string.year_selector_y_pref_key));
        yearSelectorYPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yearSelectorYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yearSelectorWidthPreference = findPreference(getString(R.string.year_selector_width_pref_key));
        yearSelectorWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yearSelectorWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yearSelectorHeightPreference = findPreference(getString(R.string.year_selector_height_pref_key));
        yearSelectorHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yearSelectorHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yob2010XPreference = findPreference(getString(R.string.yob_2010_x_pref_key));
        yob2010XPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010XPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yob2010XPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yob2010YPreference = findPreference(getString(R.string.yob_2010_y_pref_key));
        yob2010YPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010YPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yob2010YPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yob2010WidthPreference = findPreference(getString(R.string.yob_2010_width_pref_key));
        yob2010WidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010WidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yob2010WidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference yob2010HeightPreference = findPreference(getString(R.string.yob_2010_height_pref_key));
        yob2010HeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010HeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        yob2010HeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference submitDobButtonXPreference = findPreference(getString(R.string.submit_dob_button_x_pref_key));
        submitDobButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        submitDobButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference submitDobButtonYPreference = findPreference(getString(R.string.submit_dob_button_y_pref_key));
        submitDobButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        submitDobButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference submitDobButtonWidthPreference = findPreference(getString(R.string.submit_dob_button_width_pref_key));
        submitDobButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        submitDobButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference submitDobButtonHeightPreference = findPreference(getString(R.string.submit_dob_button_height_pref_key));
        submitDobButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        submitDobButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference returningPlayerButtonXPreference = findPreference(getString(R.string.returning_player_button_x_pref_key));
        returningPlayerButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        returningPlayerButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference returningPlayerButtonYPreference = findPreference(getString(R.string.returning_player_button_y_pref_key));
        returningPlayerButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        returningPlayerButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference returningPlayerButtonWidthPreference = findPreference(getString(R.string.returning_player_button_width_pref_key));
        returningPlayerButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        returningPlayerButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference returningPlayerButtonHeightPreference = findPreference(getString(R.string.returning_player_button_height_pref_key));
        returningPlayerButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        returningPlayerButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference ptcButtonXPreference = findPreference(getString(R.string.ptc_button_x_pref_key));
        ptcButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        ptcButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference ptcButtonYPreference = findPreference(getString(R.string.ptc_button_y_pref_key));
        ptcButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        ptcButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference ptcButtonWidthPreference = findPreference(getString(R.string.ptc_button_width_pref_key));
        ptcButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        ptcButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference ptcButtonHeightPreference = findPreference(getString(R.string.ptc_button_height_pref_key));
        ptcButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        ptcButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference usernameTextFieldXPreference = findPreference(getString(R.string.username_text_field_x_pref_key));
        usernameTextFieldXPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        usernameTextFieldXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference usernameTextFieldYPreference = findPreference(getString(R.string.username_text_field_y_pref_key));
        usernameTextFieldYPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        usernameTextFieldYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference usernameTextFieldWidthPreference = findPreference(getString(R.string.username_text_field_width_pref_key));
        usernameTextFieldWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        usernameTextFieldWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference usernameTextFieldHeightPreference = findPreference(getString(R.string.username_text_field_height_pref_key));
        usernameTextFieldHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        usernameTextFieldHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference passwordTextFieldXPreference = findPreference(getString(R.string.password_text_field_x_pref_key));
        passwordTextFieldXPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        passwordTextFieldXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference passwordTextFieldYPreference = findPreference(getString(R.string.password_text_field_y_pref_key));
        passwordTextFieldYPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        passwordTextFieldYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference passwordTextFieldWidthPreference = findPreference(getString(R.string.password_text_field_width_pref_key));
        passwordTextFieldWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        passwordTextFieldWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference passwordTextFieldHeightPreference = findPreference(getString(R.string.password_text_field_height_pref_key));
        passwordTextFieldHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        passwordTextFieldHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference signInButtonXPreference = findPreference(getString(R.string.sign_in_button_x_pref_key));
        signInButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        signInButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference signInButtonYPreference = findPreference(getString(R.string.sign_in_button_y_pref_key));
        signInButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        signInButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference signInButtonWidthPreference = findPreference(getString(R.string.sign_in_button_width_pref_key));
        signInButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        signInButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference signInButtonHeightPreference = findPreference(getString(R.string.sign_in_button_height_pref_key));
        signInButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        signInButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptNotificationsButtonXPreference = findPreference(getString(R.string.accept_notifications_button_x_pref_key));
        acceptNotificationsButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptNotificationsButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptNotificationsButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptNotificationsButtonYPreference = findPreference(getString(R.string.accept_notifications_button_y_pref_key));
        acceptNotificationsButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptNotificationsButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptNotificationsButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptNotificationsButtonWidthPreference = findPreference(getString(R.string.accept_notifications_button_width_pref_key));
        acceptNotificationsButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptNotificationsButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptNotificationsButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptNotificationsButtonHeightPreference = findPreference(getString(R.string.accept_notifications_button_height_pref_key));
        acceptNotificationsButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptNotificationsButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptNotificationsButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptTosButtonXPreference = findPreference(getString(R.string.accept_tos_button_x_pref_key));
        acceptTosButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptTosButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptTosButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptTosButtonYPreference = findPreference(getString(R.string.accept_tos_button_y_pref_key));
        acceptTosButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptTosButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptTosButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptTosButtonWidthPreference = findPreference(getString(R.string.accept_tos_button_width_pref_key));
        acceptTosButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptTosButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptTosButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference acceptTosButtonHeightPreference = findPreference(getString(R.string.accept_tos_button_height_pref_key));
        acceptTosButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        acceptTosButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        acceptTosButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closePrivacyPolicyButtonXPreference = findPreference(getString(R.string.close_privacy_policy_button_x_pref_key));
        closePrivacyPolicyButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closePrivacyPolicyButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closePrivacyPolicyButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closePrivacyPolicyButtonYPreference = findPreference(getString(R.string.close_privacy_policy_button_y_pref_key));
        closePrivacyPolicyButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closePrivacyPolicyButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closePrivacyPolicyButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closePrivacyPolicyButtonWidthPreference = findPreference(getString(R.string.close_privacy_policy_button_width_pref_key));
        closePrivacyPolicyButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closePrivacyPolicyButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closePrivacyPolicyButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closePrivacyPolicyButtonHeightPreference = findPreference(getString(R.string.close_privacy_policy_button_height_pref_key));
        closePrivacyPolicyButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closePrivacyPolicyButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closePrivacyPolicyButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningSmallButtonXPreference = findPreference(getString(R.string.close_safety_warning_small_button_x_pref_key));
        closeSafetyWarningSmallButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningSmallButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningSmallButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningSmallButtonYPreference = findPreference(getString(R.string.close_safety_warning_small_button_y_pref_key));
        closeSafetyWarningSmallButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningSmallButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningSmallButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningSmallButtonWidthPreference = findPreference(getString(R.string.close_safety_warning_small_button_width_pref_key));
        closeSafetyWarningSmallButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningSmallButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningSmallButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningSmallButtonHeightPreference = findPreference(getString(R.string.close_safety_warning_small_button_height_pref_key));
        closeSafetyWarningSmallButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningSmallButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningSmallButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningLongButtonXPreference = findPreference(getString(R.string.close_safety_warning_long_button_x_pref_key));
        closeSafetyWarningLongButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningLongButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningLongButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningLongButtonYPreference = findPreference(getString(R.string.close_safety_warning_long_button_y_pref_key));
        closeSafetyWarningLongButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningLongButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningLongButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningLongButtonWidthPreference = findPreference(getString(R.string.close_safety_warning_long_button_width_pref_key));
        closeSafetyWarningLongButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningLongButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningLongButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSafetyWarningLongButtonHeightPreference = findPreference(getString(R.string.close_safety_warning_long_button_height_pref_key));
        closeSafetyWarningLongButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningLongButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSafetyWarningLongButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning1ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_1_button_x_pref_key));
        closeCheatingWarning1ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning1ButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning1ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_1_button_y_pref_key));
        closeCheatingWarning1ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning1ButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning1ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_1_button_width_pref_key));
        closeCheatingWarning1ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning1ButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning1ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_1_button_height_pref_key));
        closeCheatingWarning1ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning1ButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning2ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_2_button_x_pref_key));
        closeCheatingWarning2ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning2ButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning2ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_2_button_y_pref_key));
        closeCheatingWarning2ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning2ButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning2ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_2_button_width_pref_key));
        closeCheatingWarning2ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning2ButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning2ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_2_button_height_pref_key));
        closeCheatingWarning2ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning2ButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning3ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_3_button_x_pref_key));
        closeCheatingWarning3ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning3ButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning3ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_3_button_y_pref_key));
        closeCheatingWarning3ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning3ButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning3ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_3_button_width_pref_key));
        closeCheatingWarning3ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning3ButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeCheatingWarning3ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_3_button_height_pref_key));
        closeCheatingWarning3ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeCheatingWarning3ButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSuspensionWarningButtonXPreference = findPreference(getString(R.string.close_suspension_warning_button_x_pref_key));
        closeSuspensionWarningButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSuspensionWarningButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSuspensionWarningButtonYPreference = findPreference(getString(R.string.close_suspension_warning_button_y_pref_key));
        closeSuspensionWarningButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSuspensionWarningButtonYPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSuspensionWarningButtonWidthPreference = findPreference(getString(R.string.close_suspension_warning_button_width_pref_key));
        closeSuspensionWarningButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSuspensionWarningButtonWidthPreference.setSummaryProvider(summaryProvider);

        EditTextPreference closeSuspensionWarningButtonHeightPreference = findPreference(getString(R.string.close_suspension_warning_button_height_pref_key));
        closeSuspensionWarningButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        closeSuspensionWarningButtonHeightPreference.setSummaryProvider(summaryProvider);

        EditTextPreference playerProfileButtonXPreference = findPreference(getString(R.string.player_profile_button_x_pref_key));
        playerProfileButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        playerProfileButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        playerProfileButtonXPreference.setSummaryProvider(summaryProvider);

        EditTextPreference playerProfileButtonYPreference = findPreference(getString(R.string.player_profile_button_y_pref_key));
        playerProfileButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        playerProfileButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        playerProfileButtonYPreference.setSummaryProvider(summaryProvider);
    }
}
