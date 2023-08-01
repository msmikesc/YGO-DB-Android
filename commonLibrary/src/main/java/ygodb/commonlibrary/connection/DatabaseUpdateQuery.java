package ygodb.commonlibrary.connection;

import java.sql.SQLException;

public interface DatabaseUpdateQuery {
	void prepareStatement(String query) throws SQLException;
	void bindString(int index, String value) throws SQLException;
	void bindInteger(int index, Integer value) throws SQLException;
	int executeUpdate() throws SQLException;
}



