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
        int notExistCount = getIntent().getIntExtra("notExistCount", 0);
        int errorCount = getIntent().getIntExtra("errorCount", 0);
        boolean interrupted = getIntent().getBooleanExtra("interrupted", true);

        TextView resultTitleView = findViewById(R.id.resultTitle);
        if (interrupted) {
            resultTitleView.setText("Account checking has stopped");
        } else {
            resultTitleView.setText("Account checking has finished");
        }

        int numAccountsChecked = notBannedCount + bannedCount + notExistCount + errorCount;
        TextView resultView = findViewById(R.id.result);
        resultView.setText(numAccountsChecked + "/" +numAccounts + " have been checked.\n"
                + notBannedCount + " are not banned.\n"
                + bannedCount + " are banned.\n"
                + notExistCount + " don't exist.\n"
                + errorCount + " couldn't be checked.");
    }
}
