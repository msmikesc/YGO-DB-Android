package com.example.ygodb;

import android.app.Application;

import com.example.ygodb.backend.connection.SQLiteConnection;

import java.sql.SQLException;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            SQLiteConnection.initializeInstance(getApplicationContext());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
