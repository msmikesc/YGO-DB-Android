package com.example.ygodb.db;

import android.database.sqlite.SQLiteStatement;

import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public interface BatchSetterAndroid {
	void setParams(SQLiteStatement statement, List<Object> params) throws SQLException;
}