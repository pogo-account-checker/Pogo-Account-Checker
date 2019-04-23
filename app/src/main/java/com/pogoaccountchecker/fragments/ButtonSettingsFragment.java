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

        EditTextPreference yearSelectorXPreference = findPreference(getString(R.string.year_selector_x_pref_key));
        yearSelectorXPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yearSelectorYPreference = findPreference(getString(R.string.year_selector_y_pref_key));
        yearSelectorYPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yearSelectorWidthPreference = findPreference(getString(R.string.year_selector_width_pref_key));
        yearSelectorWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yearSelectorHeightPreference = findPreference(getString(R.string.year_selector_height_pref_key));
        yearSelectorHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        yearSelectorHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yob2010XPreference = findPreference(getString(R.string.yob_2010_x_pref_key));
        yob2010XPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010XPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yob2010YPreference = findPreference(getString(R.string.yob_2010_y_pref_key));
        yob2010YPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010YPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yob2010WidthPreference = findPreference(getString(R.string.yob_2010_width_pref_key));
        yob2010WidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010WidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference yob2010HeightPreference = findPreference(getString(R.string.yob_2010_height_pref_key));
        yob2010HeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        yob2010HeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference submitDobButtonXPreference = findPreference(getString(R.string.submit_dob_button_x_pref_key));
        submitDobButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference submitDobButtonYPreference = findPreference(getString(R.string.submit_dob_button_y_pref_key));
        submitDobButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference submitDobButtonWidthPreference = findPreference(getString(R.string.submit_dob_button_width_pref_key));
        submitDobButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference submitDobButtonHeightPreference = findPreference(getString(R.string.submit_dob_button_height_pref_key));
        submitDobButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        submitDobButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference returningPlayerButtonXPreference = findPreference(getString(R.string.returning_player_button_y_pref_key));
        returningPlayerButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference returningPlayerButtonYPreference = findPreference(getString(R.string.returning_player_button_y_pref_key));
        returningPlayerButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference returningPlayerButtonWidthPreference = findPreference(getString(R.string.returning_player_button_width_pref_key));
        returningPlayerButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference returningPlayerButtonHeightPreference = findPreference(getString(R.string.returning_player_button_height_pref_key));
        returningPlayerButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        returningPlayerButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference ptcButtonXPreference = findPreference(getString(R.string.ptc_button_x_pref_key));
        ptcButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference ptcButtonYPreference = findPreference(getString(R.string.ptc_button_y_pref_key));
        ptcButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference ptcButtonWidthPreference = findPreference(getString(R.string.ptc_button_width_pref_key));
        ptcButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference ptcButtonHeightPreference = findPreference(getString(R.string.ptc_button_height_pref_key));
        ptcButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        ptcButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference usernameTextFieldXPreference = findPreference(getString(R.string.username_text_field_x_pref_key));
        usernameTextFieldXPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference usernameTextFieldYPreference = findPreference(getString(R.string.username_text_field_y_pref_key));
        usernameTextFieldYPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference usernameTextFieldWidthPreference = findPreference(getString(R.string.username_text_field_width_pref_key));
        usernameTextFieldWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference usernameTextFieldHeightPreference = findPreference(getString(R.string.username_text_field_height_pref_key));
        usernameTextFieldHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        usernameTextFieldHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference passwordTextFieldXPreference = findPreference(getString(R.string.password_text_field_x_pref_key));
        passwordTextFieldXPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference passwordTextFieldYPreference = findPreference(getString(R.string.password_text_field_y_pref_key));
        passwordTextFieldYPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference passwordTextFieldWidthPreference = findPreference(getString(R.string.password_text_field_width_pref_key));
        passwordTextFieldWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference passwordTextFieldHeightPreference = findPreference(getString(R.string.password_text_field_height_pref_key));
        passwordTextFieldHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        passwordTextFieldHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference signInButtonXPreference = findPreference(getString(R.string.sign_in_button_x_pref_key));
        signInButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference signInButtonYPreference = findPreference(getString(R.string.sign_in_button_y_pref_key));
        signInButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference signInButtonWidthPreference = findPreference(getString(R.string.sign_in_button_width_pref_key));
        signInButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference signInButtonHeightPreference = findPreference(getString(R.string.sign_in_button_height_pref_key));
        signInButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        signInButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowLocationButtonXPreference = findPreference(getString(R.string.allow_location_button_x_pref_key));
        allowLocationButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowLocationButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowLocationButtonYPreference = findPreference(getString(R.string.allow_location_button_y_pref_key));
        allowLocationButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowLocationButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowLocationButtonWidthPreference = findPreference(getString(R.string.allow_location_button_width_pref_key));
        allowLocationButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowLocationButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowLocationButtonHeightPreference = findPreference(getString(R.string.allow_location_button_height_pref_key));
        allowLocationButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowLocationButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowCameraButtonXPreference = findPreference(getString(R.string.allow_camera_button_x_pref_key));
        allowCameraButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowCameraButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowCameraButtonYPreference = findPreference(getString(R.string.allow_camera_button_y_pref_key));
        allowCameraButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowCameraButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowCameraButtonWidthPreference = findPreference(getString(R.string.allow_camera_button_width_pref_key));
        allowCameraButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowCameraButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference allowCameraButtonHeightPreference = findPreference(getString(R.string.allow_camera_button_height_pref_key));
        allowCameraButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        allowCameraButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSafetyWarningButtonXPreference = findPreference(getString(R.string.close_safety_warning_button_x_pref_key));
        closeSafetyWarningButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSafetyWarningButtonYPreference = findPreference(getString(R.string.close_safety_warning_button_y_pref_key));
        closeSafetyWarningButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSafetyWarningButtonWidthPreference = findPreference(getString(R.string.close_safety_warning_button_width_pref_key));
        closeSafetyWarningButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSafetyWarningButtonHeightPreference = findPreference(getString(R.string.close_safety_warning_button_height_pref_key));
        closeSafetyWarningButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSafetyWarningButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning1ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_1_button_x_pref_key));
        closeCheatingWarning1ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning1ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_1_button_y_pref_key));
        closeCheatingWarning1ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning1ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_1_button_width_pref_key));
        closeCheatingWarning1ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning1ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_1_button_height_pref_key));
        closeCheatingWarning1ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning1ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning2ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_2_button_x_pref_key));
        closeCheatingWarning2ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning2ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_2_button_y_pref_key));
        closeCheatingWarning2ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning2ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_2_button_width_pref_key));
        closeCheatingWarning2ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning2ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_2_button_height_pref_key));
        closeCheatingWarning2ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning2ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning3ButtonXPreference = findPreference(getString(R.string.close_cheating_warning_3_button_x_pref_key));
        closeCheatingWarning3ButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning3ButtonYPreference = findPreference(getString(R.string.close_cheating_warning_3_button_y_pref_key));
        closeCheatingWarning3ButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning3ButtonWidthPreference = findPreference(getString(R.string.close_cheating_warning_3_button_width_pref_key));
        closeCheatingWarning3ButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeCheatingWarning3ButtonHeightPreference = findPreference(getString(R.string.close_cheating_warning_3_button_height_pref_key));
        closeCheatingWarning3ButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeCheatingWarning3ButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSuspensionWarningButtonXPreference = findPreference(getString(R.string.close_suspension_warning_button_x_pref_key));
        closeSuspensionWarningButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSuspensionWarningButtonYPreference = findPreference(getString(R.string.close_suspension_warning_button_y_pref_key));
        closeSuspensionWarningButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSuspensionWarningButtonWidthPreference = findPreference(getString(R.string.close_suspension_warning_button_width_pref_key));
        closeSuspensionWarningButtonWidthPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonWidthPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference closeSuspensionWarningButtonHeightPreference = findPreference(getString(R.string.close_suspension_warning_button_height_pref_key));
        closeSuspensionWarningButtonHeightPreference.setOnBindEditTextListener(onBindEditTextListener);
        closeSuspensionWarningButtonHeightPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference playerProfileButtonXPreference = findPreference(getString(R.string.player_profile_button_x_pref_key));
        playerProfileButtonXPreference.setOnBindEditTextListener(onBindEditTextListener);
        playerProfileButtonXPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        EditTextPreference playerProfileButtonYPreference = findPreference(getString(R.string.player_profile_button_y_pref_key));
        playerProfileButtonYPreference.setOnBindEditTextListener(onBindEditTextListener);
        playerProfileButtonYPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }
}
