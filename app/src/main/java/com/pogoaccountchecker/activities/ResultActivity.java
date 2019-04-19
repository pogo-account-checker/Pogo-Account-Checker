package com.pogoaccountchecker.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

import com.pogoaccountchecker.R;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar childToolbar = findViewById(R.id.result_toolbar);
        setSupportActionBar(childToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        int accountCount = getIntent().getIntExtra("accountCount", 0);
        int notBannedCount = getIntent().getIntExtra("notBannedCount", 0);
        int bannedCount = getIntent().getIntExtra("bannedCount", 0);
        int newCount = getIntent().getIntExtra("newCount", 0);
        int wrongCredentialsCount = getIntent().getIntExtra("wrongCredentialsCount", 0);
        int notActivatedCount = getIntent().getIntExtra("notActivatedCount", 0);
        int lockedCount = getIntent().getIntExtra("lockedCount", 0);
        int errorCount = getIntent().getIntExtra("errorCount", 0);
        boolean stopped = getIntent().getBooleanExtra("stopped", true);

        if (stopped) {
            ab.setTitle("Account checking stopped");
        } else {
            ab.setTitle("Account checking finished");
        }

        int numAccountsChecked = notBannedCount + bannedCount + newCount + wrongCredentialsCount + notActivatedCount + lockedCount + errorCount;
        TextView resultView = findViewById(R.id.result);
        resultView.setText(numAccountsChecked + "/" +accountCount + " accounts have been checked.\n\n"
                + "Not banned: " + notBannedCount + "\n"
                + "Banned: " + bannedCount + "\n"
                + "New: " + newCount + "\n"
                + "Not activated: " + notActivatedCount + "\n"
                + "Locked: " + lockedCount + "\n"
                + "Wrong username/password: " + wrongCredentialsCount + "\n"
                + "Couldn't be checked: " + errorCount + "\n");
    }
}
