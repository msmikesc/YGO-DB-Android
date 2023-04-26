package com.example.ygodb;

import android.app.Application;
import android.os.Environment;

import com.example.ygodb.backend.connection.SQLiteConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        File logFile = null;

        if (isExternalStorageWritable()) {
            logFile = new File(getExternalFilesDir(null), "ygodblog.txt");
            try {
                if(!logFile.exists()) {
                    logFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File finalLogFile = logFile;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                //Catch your exception
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(finalLogFile, true);
                    PrintStream ps = new PrintStream(fos);
                    paramThrowable.printStackTrace(ps);
                    ps.close();
                } catch (FileNotFoundException e) {

                }

                // Without System.exit() this will not work.
                System.exit(2);
            }
        });

        try {
            SQLiteConnection.initializeInstance(getApplicationContext());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
