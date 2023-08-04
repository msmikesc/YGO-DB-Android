package com.example.ygodb.impl;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import ygodb.commonlibrary.connection.DatabaseUpdateQuery;

import java.sql.SQLException;

public class DatabaseUpdateQueryAndroid implements DatabaseUpdateQuery {

	private static final String ERROR_MESSAGE = "Statement null or closed";
	private static final String ERROR_MESSAGE_INIT = "Statement already exists";

	private final SQLiteDatabase connection;
	private SQLiteStatement statement = null;
	private boolean isClosed = false;

	public DatabaseUpdateQueryAndroid(SQLiteDatabase connection) {
		this.connection = connection;
	}

	@Override
	public void prepareStatement(String query) throws SQLException {
		if (statement != null) {
			throw new SQLException(ERROR_MESSAGE_INIT);
		}
		statement = connection.compileStatement(query);
	}

	@Override
	public void bindString(int index, String value) throws SQLException {
		if (statement == null || isClosed) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			statement.bindNull(index);
		} else {
			statement.bindString(index, value);
		}
	}

	@Override
	public void bindInteger(int index, Integer value) throws SQLException {
		if (statement == null || isClosed) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			statement.bindNull(index);
		} else {
			statement.bindLong(index, value);
		}
	}

	@Override
	public int executeUpdate() throws SQLException {
		if (statement == null || isClosed) {
			throw new SQLException(ERROR_MESSAGE);
		}
		int count = statement.executeUpdateDelete();
		statement.close();
		isClosed = true;

		return count;
	}
}
