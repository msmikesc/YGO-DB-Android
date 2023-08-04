package ygodb.windows.connection;

import ygodb.commonlibrary.connection.DatabaseSelectQuery;
import ygodb.commonlibrary.connection.SelectQueryResultMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSelectQueryWindows<T> implements DatabaseSelectQuery<T, ResultSet> {

	private static final String ERROR_MESSAGE = "Statement null or closed";
	private static final String ERROR_MESSAGE_INIT = "Statement already exists";

	private final Connection connection;
	private PreparedStatement statement = null;

	public DatabaseSelectQueryWindows(Connection connection) {
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
	public List<T> executeQuery(SelectQueryResultMapper<T, ResultSet> mapper) throws SQLException {
		if (statement == null || statement.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}

		List<T> resultList = new ArrayList<>();

		try (ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				T mappedResult = mapper.mapRow((resultSet));
				resultList.add(mappedResult);
			}
		} finally {
			statement.close();
		}

		return resultList;
	}
}
