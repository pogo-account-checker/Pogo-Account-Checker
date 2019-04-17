package com.pogoaccountchecker.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
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
    private Spinner mDelimiterSpinner;
    private AccountCheckingService mService;
    private boolean mBound;
    private boolean mWithMad;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mDelimiterSpinner = findViewById(R.id.delimiterSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.delimiters, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDelimiterSpinner.setAdapter(adapter);
        int selectedItem = sharedPref.getInt(getString(R.string.selected_delimiter_pref_key), 0);
        mDelimiterSpinner.setSelection(selectedItem, false);
        mDelimiterSpinner.setOnItemSelectedListener(this);

        mWithMad = sharedPref.getBoolean(getString(R.string.with_mad_pref_key), false);

        final Button accountsButton = findViewById(R.id.accountsButton);
        if (mWithMad) {
            accountsButton.setVisibility(View.GONE);
        }

        final EditText webSocketEditText = findViewById(R.id.webSocketEditText);
        if (!mWithMad) {
            webSocketEditText.setVisibility(View.GONE);
        }
        String webSocketUri = sharedPref.getString(getString(R.string.webSocket_uri_pref_key), null);
        if (webSocketUri != null) {
            webSocketEditText.setText(webSocketUri);
        }
        webSocketEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.webSocket_uri_pref_key), s.toString());
                editor.apply();
            }
        });

        Switch madSwitch = findViewById(R.id.madSwitch);
        madSwitch.setChecked(mWithMad);
        madSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWithMad = isChecked;
                if (mWithMad) {
                    webSocketEditText.setVisibility(View.VISIBLE);
                    accountsButton.setVisibility(View.GONE);
                } else {
                    webSocketEditText.setVisibility(View.GONE);
                    accountsButton.setVisibility(View.VISIBLE);
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.with_mad_pref_key), mWithMad);
                editor.apply();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AccountCheckingService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        mBound = false;
    }

    /**
     * Called when the start/pause/continue button is pressed.
     *
     * @param view the View that was clicked.
     */
    public void startPauseContinue(View view) {
        if (RootShell.isAccessGiven()) {
            if (mBound) {
                char delimiter = ((String) mDelimiterSpinner.getSelectedItem()).charAt(0);
                Button startPauseContinueButton = findViewById(R.id.startPauseContinueButton);

                if (!mService.isChecking()) {
                    if (mWithMad) {
                        EditText webSocketEditText = findViewById(R.id.webSocketEditText);
                        String webSocketUri = webSocketEditText.getText().toString();
                        if (!webSocketUri.isEmpty()) {
                            mService.checkAccountsWithMAD(webSocketUri, delimiter);
                        } else {
                            Toast.makeText(this, "Enter a WebSocket URI first!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (mAccounts != null) {
                            if (accountsHaveDelimiter()) {
                                mService.checkAccounts(mAccounts, delimiter);
                                startPauseContinueButton.setText("Pause");
                                Button stopButton = findViewById(R.id.stopButton);
                                stopButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(this, "There is something wrong with your accounts file!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Select TXT file with accounts first!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (mService.isPaused()) {
                        mService.resume();
                        startPauseContinueButton.setText("Pause");
                    } else {
                        mService.pause();
                        startPauseContinueButton.setText("Continue");
                    }
                }
            }
        } else {
            Toast.makeText(this, "No root access!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the stop button is pressed.
     *
     * @param view the View that was clicked.
     */
    public void stop(View view) {
        if (mBound) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Do you really want to stop?")
                    .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            mService.stop();
                            Button startPauseContinueButton = findViewById(R.id.startPauseContinueButton);
                            startPauseContinueButton.setText("Start");
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
        super.onActivityResult(requestCode, resultCode, resultData);

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
                Button accountsButton = findViewById(R.id.accountsButton);
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
            // Remove white space characters from account and add account to account list.
            mAccounts.add(line.replaceAll("\\s", ""));
        }
        inputStream.close();
        reader.close();
    }

    private boolean accountsHaveDelimiter() {
        String delimiter = (String) mDelimiterSpinner.getSelectedItem();
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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AccountCheckingService.AccountCheckingServiceBinder binder = (AccountCheckingService.AccountCheckingServiceBinder) service;
            mService = binder.getService();
            mBound = true;

            Button startPauseContinueButton = findViewById(R.id.startPauseContinueButton);
            if (mService.isChecking()) {
                if (mService.isPaused()) {
                    startPauseContinueButton.setText("Continue");
                } else {
                    startPauseContinueButton.setText("Pause");
                }
            } else {
                startPauseContinueButton.setText("Start");
                Button stopButton = findViewById(R.id.stopButton);
                stopButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
