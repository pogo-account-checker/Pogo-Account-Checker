package com.pogoaccountchecker.utils;

import java.io.IOException;
import java.io.OutputStream;

public class Shell {
    private Shell() {
    }

    public static void runSuCommand(String command) throws IOException, InterruptedException {
        if (!command.substring(command.length() - 2).equals("\n")) {
            command = command.concat("\n");
        }
        Process process = Runtime.getRuntime().exec("su");
        OutputStream stdin = process.getOutputStream();
        stdin.write(command.getBytes());
        stdin.write("exit\n".getBytes());
        stdin.flush();
        stdin.close();
        process.waitFor();
        process.destroy();
    }
}
