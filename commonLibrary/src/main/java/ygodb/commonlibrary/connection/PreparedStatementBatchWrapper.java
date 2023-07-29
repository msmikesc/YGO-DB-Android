package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.List;

public interface PreparedStatementBatchWrapper {
	void addSingleValuesSet(List<Object> params) throws SQLException;

	void executeBatch() throws SQLException;

	void finalizeBatches() throws SQLException;

	boolean isFinalized();

	boolean isAboveBatchMaximum();
}
