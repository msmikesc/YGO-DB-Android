package ygodb.windows.connection;

import ygodb.commonlibrary.connection.DatabaseSelectMapQuery;
import ygodb.commonlibrary.connection.SelectQueryMapMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSelectMapQueryWindows<T> implements DatabaseSelectMapQuery<T, ResultSet> {

	private static final String ERROR_MESSAGE = "Statement null or closed";
	private static final String ERROR_MESSAGE_INIT = "Statement already exists";

	private final Connection connection;
	private PreparedStatement statement = null;

	public DatabaseSelectMapQueryWindows(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void prepareStatement(String query) throws SQLException {
		if (statement != null) {
			throw new SQLException(ERROR_MESSAGE_INIT);
		}
		statement = connection.prepareStatement(query);
	}

	@Override
	public void bindString(int index, String value) throws SQLException {
		if (statement == null || statement.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			statement.setNull(index, Types.VARCHAR);
		} else {
			statement.setString(index, value);
		}
	}

	@Override
	public void bindInteger(int index, Integer value) throws SQLException {
		if (statement == null || statement.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}
		if (value == null) {
			statement.setNull(index, Types.INTEGER);
		} else {
			statement.setInt(index, value);
		}
	}

	@Override
	public Map<String,List<T>> executeQuery(SelectQueryMapMapper<T, ResultSet> mapper) throws SQLException {
		if (statement == null || statement.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}

		Map<String, List<T>> resultMap = new HashMap<>();

		try (ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				T mappedResult = mapper.mapRow((resultSet));
				List<String> keys = mapper.getKeys(mappedResult);

				for(String key : keys){
					List<T> currentList = resultMap.computeIfAbsent(key, k -> new ArrayList<>());
					currentList.add(mappedResult);
				}
			}
		} finally {
			statement.close();
		}

		return resultMap;
	}
}
