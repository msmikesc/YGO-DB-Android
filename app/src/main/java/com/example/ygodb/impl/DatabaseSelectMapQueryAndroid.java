package com.example.ygodb.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ygodb.commonlibrary.connection.DatabaseSelectMapQuery;
import ygodb.commonlibrary.connection.SelectQueryMapMapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSelectMapQueryAndroid<T> implements DatabaseSelectMapQuery<T, Cursor> {
	private final SQLiteDatabase connection;
	private String query;
	private final Map<Integer, String> bindArgs = new HashMap<>();

	public DatabaseSelectMapQueryAndroid(SQLiteDatabase connection) {
		this.connection = connection;
	}

	@Override
	public void prepareStatement(String query) {
		this.query = query;
	}

	@Override
	public void bindString(int index, String value) {
		bindArgs.put(index, value);
	}

	@Override
	public void bindInteger(int index, Integer value) {
		bindArgs.put(index, String.valueOf(value));
	}

	@Override
	public Map<String,List<T>> executeQuery(SelectQueryMapMapper<T, Cursor> mapper) throws SQLException {

		String[] bindArgsArray = new String[bindArgs.size()];
		for (Map.Entry<Integer, String> entry : bindArgs.entrySet()) {
			int index = entry.getKey();
			String value = entry.getValue();
			bindArgsArray[index - 1] = value;
		}

		Cursor cursor = connection.rawQuery(query, bindArgsArray);
		bindArgs.clear(); // Clear the bound arguments after execution

		Map<String, List<T>> resultMap = new HashMap<>();

		try {
			while (cursor.moveToNext()) {
				T mappedResult = mapper.mapRow(cursor);
				List<String> keys = mapper.getKeys(mappedResult);

				for(String key : keys){
					List<T> currentList = resultMap.computeIfAbsent(key, k -> new ArrayList<>());
					currentList.add(mappedResult);
				}
			}
		} finally {
			cursor.close();
		}

		return resultMap;
	}
}


