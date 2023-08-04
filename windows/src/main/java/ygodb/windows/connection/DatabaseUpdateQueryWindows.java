package ygodb.windows.connection;

import ygodb.commonlibrary.connection.DatabaseUpdateQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DatabaseUpdateQueryWindows implements DatabaseUpdateQuery {

	private static final String ERROR_MESSAGE = "Statement null or closed";
	private static final String ERROR_MESSAGE_INIT = "Statement already exists";

	private final Connection connection;
	private PreparedStatement statement = null;

	public DatabaseUpdateQueryWindows(Connection connection) {
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
	public int executeUpdate() throws SQLException {
		if (statement == null || statement.isClosed()) {
			throw new SQLException(ERROR_MESSAGE);
		}
		int count = statement.executeUpdate();
		statement.close();

		return count;
	}
}
