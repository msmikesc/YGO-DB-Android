package ygodb.windows.connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@FunctionalInterface
public
interface BatchSetterWindows {
	void setParams(PreparedStatement statement, List<Object> params) throws SQLException;
}