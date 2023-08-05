package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseSelectMapQuery<T, R> {
	void prepareStatement(String query) throws SQLException;

	void bindString(int index, String value) throws SQLException;

	void bindInteger(int index, Integer value) throws SQLException;

	Map<String, List<T>> executeQuery(SelectQueryMapMapper<T, R> mapper) throws SQLException;
}
