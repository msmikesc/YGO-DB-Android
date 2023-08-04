package ygodb.commonlibrary.connection;

import java.sql.SQLException;

public interface SelectQueryResultMapper<T, R> {

	T mapRow(R resultSet) throws SQLException;

}
