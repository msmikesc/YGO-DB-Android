package com.example.ygodb;

import android.app.Application;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;

import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Locale;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidUtil.setAppContext(getApplicationContext());

        File logFile = null;

        if (isExternalStorageWritable()) {
            logFile = new File(getExternalFilesDir(null), "ygodblog.txt");
            try {
                if(!logFile.exists() && !logFile.createNewFile()) {
                    YGOLogger.error("Unable to create log file");
                }
            } catch (IOException e) {
                YGOLogger.logException(e);
            }
        }

        File finalLogFile = logFile;
        Thread.setDefaultUncaughtExceptionHandler((paramThread, paramThrowable) -> {
            //Catch your exception
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(finalLogFile, true);
                PrintStream ps = new PrintStream(fos);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateAndTime = sdf.format(new Date());

                ps.println("--------------------------");
                ps.println(currentDateAndTime);

                paramThrowable.printStackTrace(ps);
                ps.close();
            } catch (FileNotFoundException ignored) {

            }

            // Without System.exit() this will not work.
            System.exit(2);
        });

        AndroidUtil.getDBInstance();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
