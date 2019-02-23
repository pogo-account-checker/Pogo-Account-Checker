package com.pogoaccountchecker.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.pogoaccountchecker.services.AccountCheckingService;
import com.pogoaccountchecker.R;
import com.stericson.RootShell.RootShell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ArrayList<String> mAccounts;
    private Spinner delimiterSpinner;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        delimiterSpinner = findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delimiters, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        delimiterSpinner.setAdapter(adapter);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int selectedItem = sharedPref.getInt(getString(R.string.selected_delimiter_pref_key), 0);
        Log.d("debug", selectedItem + "");
        delimiterSpinner.setSelection(selectedItem, false);
        delimiterSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Called when the start button is pressed.
     *
     * @param view the View that was clicked.
     */
    public void start(View view) {
        if (RootShell.isAccessGiven()) {
            if (mAccounts == null) {
                Toast.makeText(this, "Select TXT file with accounts first!", Toast.LENGTH_SHORT).show();
            } else {
                if (accountsHaveDelimiter()) {
                    // Start service
                    Intent intent = new Intent(this, AccountCheckingService.class);
                    intent.putStringArrayListExtra("accounts", mAccounts);
                    char delimiter = ((String) delimiterSpinner.getSelectedItem()).charAt(0);
                    intent.putExtra("delimiter", delimiter);
                    intent.putExtra("separator", (String) delimiterSpinner.getSelectedItem());
                    startService(intent);
                } else {
                    Toast.makeText(this, "There is something wrong with your accounts file!", Toast.LENGTH_LONG).show();
                }

            }
        } else {
            Toast.makeText(this, "No root access!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the close button is pressed.
     *
     * @param view the View that was clicked.
     */
    public void stop(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Do you really want to stop?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent intent = new Intent(MainActivity.this, AccountCheckingService.class);
                        stopService(intent);
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // Don't do anything.
                    }
                })
                .show();
    }

    /**
     * Called when the set accounts button is pressed.
     *
     * @param view the View that was clicked.
     */
    public void setAccounts(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, perform file search.
            performFileSearch();
        } else {
            // Permission is not granted, request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, perform file search.
                    performFileSearch();
                }
                break;
            }
        }
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened".
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();

                // Do all this stuff to get a file name...
                Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                assert returnCursor != null;
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String fileName = returnCursor.getString(nameIndex);
                returnCursor.close();
                Button accountsButton = findViewById(R.id.accounts);
                accountsButton.setText(fileName);

                try {
                    readAccountsFromFile(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readAccountsFromFile(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        mAccounts = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            mAccounts.add(line.replace("\n", ""));
        }
        inputStream.close();
        reader.close();
    }

    private boolean accountsHaveDelimiter() {
        String delimiter = (String) delimiterSpinner.getSelectedItem();
        for (String account : mAccounts) {
            if (!account.contains(delimiter)) return false;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.selected_delimiter_pref_key), position);
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
