package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.List;

public interface SelectQueryMapMapper<T, R> {

	List<String> getKeys(T entity);

	T mapRow(R resultSet) throws SQLException;

}
