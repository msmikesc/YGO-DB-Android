package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.List;

public interface DatabaseSelectQuery<T, R> {
	void prepareStatement(String query) throws SQLException;

	void bindString(int index, String value) throws SQLException;

	void bindInteger(int index, Integer value) throws SQLException;

	List<T> executeQuery(SelectQueryResultMapper<T, R> mapper) throws SQLException;
}
