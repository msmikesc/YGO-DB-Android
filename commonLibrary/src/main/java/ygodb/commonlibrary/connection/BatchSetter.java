package ygodb.commonlibrary.connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public
interface BatchSetter {
	void setParams(PreparedStatement statement, Object... params) throws SQLException;
}