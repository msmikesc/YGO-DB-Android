package com.example.ygodb.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ygodb.commonlibrary.connection.DatabaseSelectQuery;
import ygodb.commonlibrary.connection.SelectQueryResultMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSelectQueryAndroid<T> implements DatabaseSelectQuery<T, Cursor> {

	private static final String ERROR_MESSAGE = "Cursor null or closed";
	private static final String ERROR_MESSAGE_INIT = "Cursor already exists";

	private final SQLiteDatabase connection;
	private Cursor cursor = null;
	private String query;
	private final Map<Integer, String> bindArgs = new HashMap<>();

	public DatabaseSelectQueryAndroid(SQLiteDatabase connection) {
		this.connection = connection;
	}

	@Override
	public void prepareStatement(String query) throws SQLException {
		if (cursor != null) {
			throw new SQLException(ERROR_MESSAGE_INIT);
		}
		this.query = query;
	}

	@Override
	public void bindString(int index, String value) throws SQLException {
		if (cursor == null || cursor.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			throw new IllegalArgumentException("Cannot bind null value to a string parameter");
		} else {
			bindArgs.put(index, value);
		}
	}

	@Override
	public void bindInteger(int index, Integer value) throws SQLException {
		if (cursor == null || cursor.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			throw new IllegalArgumentException("Cannot bind null value to an integer parameter");
		} else {
			bindArgs.put(index, String.valueOf(value));
		}
	}

	@Override
	public List<T> executeQuery(SelectQueryResultMapper<T, Cursor> mapper) throws SQLException {
		if (cursor == null || cursor.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}

		String[] bindArgsArray = new String[bindArgs.size()];
		for (Map.Entry<Integer, String> entry : bindArgs.entrySet()) {
			int index = entry.getKey();
			String value = entry.getValue();
			bindArgsArray[index - 1] = value;
		}

		cursor = connection.rawQuery(query, bindArgsArray);
		bindArgs.clear(); // Clear the bound arguments after execution

		List<T> resultList = new ArrayList<>();

		try {
			while (cursor.moveToNext()) {
				T mappedResult = mapper.mapRow(cursor);
				resultList.add(mappedResult);
			}
		} finally {
			cursor.close();
		}

		return resultList;
	}
}


