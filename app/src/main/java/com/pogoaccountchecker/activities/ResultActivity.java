package com.pogoaccountchecker.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

import com.pogoaccountchecker.R;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int numAccounts = getIntent().getIntExtra("numAccounts", 0);
        int notBannedCount = getIntent().getIntExtra("notBannedCount", 0);
        int bannedCount = getIntent().getIntExtra("bannedCount", 0);
        int wrongCredentialsCount = getIntent().getIntExtra("wrongCredentialsCount", 0);
        int notActivatedCount = getIntent().getIntExtra("notActivatedCount", 0);
        int lockedCount = getIntent().getIntExtra("lockedCount", 0);
        int errorCount = getIntent().getIntExtra("errorCount", 0);
        boolean interrupted = getIntent().getBooleanExtra("interrupted", true);

        TextView resultTitleView = findViewById(R.id.resultTitle);
        if (interrupted) {
            resultTitleView.setText("Account checking stopped");
        } else {
            resultTitleView.setText("Account checking finished");
        }

        int numAccountsChecked = notBannedCount + bannedCount + wrongCredentialsCount + notActivatedCount + lockedCount + errorCount;
        TextView resultView = findViewById(R.id.result);
        resultView.setText(numAccountsChecked + "/" +numAccounts + " accounts have been checked.\n"
                + "Not banned: " + notBannedCount + "\n"
                + "Banned: " + bannedCount + "\n"
                + "Wrong username/password: " + wrongCredentialsCount + "\n"
                + "Not activated: " + notActivatedCount + "\n"
                + "Locked: " + lockedCount + "\n"
                + "Couldn't be checked: " + errorCount + "\n");
    }
}
