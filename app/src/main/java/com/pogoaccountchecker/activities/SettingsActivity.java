package com.pogoaccountchecker.activities;

import android.os.Bundle;

import com.pogoaccountchecker.R;
import com.pogoaccountchecker.fragments.SettingsFragment;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate the new Fragment
        final Fragment settingsFragment = new SettingsFragment();
        setContentView(R.layout.activity_settings);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, settingsFragment)
                .commit();

        Toolbar childToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(childToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }
}