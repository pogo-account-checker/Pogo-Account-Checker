package com.pogoaccountchecker.utils;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class Shell {
    private Shell() {
    }

    public static void runSuCommand(String command) {
        command = command.concat("\n");
        Process process;
        OutputStream stdin;
        try {
            process = Runtime.getRuntime().exec("su");

            stdin = process.getOutputStream();
            stdin.write(command.getBytes());
            stdin.write("exit\n".getBytes());
            stdin.flush();
            stdin.close();

            process.waitFor();
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e("Shell", "Exception while executing command: " + command);
        }
    }
}
