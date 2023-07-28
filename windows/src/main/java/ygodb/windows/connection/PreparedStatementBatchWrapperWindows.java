package ygodb.windows.connection;

import ygodb.commonlibrary.connection.BatchSetter;
import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PreparedStatementBatchWrapperWindows implements PreparedStatementBatchWrapper {

	private final PreparedStatement statement;
	private final Connection connection;
	private final int batchedLinesMax;
	private int currentBatchedLinesCount = 0;
	private final BatchSetter batchSetter;

	public PreparedStatementBatchWrapperWindows(Connection connection, String input, int maximum, BatchSetter setter) throws SQLException {
		this.connection = connection;
		connection.setAutoCommit(false);
		statement = connection.prepareStatement(input);
		batchedLinesMax = maximum;
		this.batchSetter = setter;
	}

	@Override
	public void addSingleValuesSet(List<Object> params) throws SQLException {
		batchSetter.setParams(statement, params.toArray());
		currentBatchedLinesCount++;
		statement.addBatch();

		if(isAboveBatchMaximum()){
			executeBatch();
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		statement.executeBatch();
		connection.commit();
		currentBatchedLinesCount = 0;
	}

	@Override
	public void finalizeBatches() throws SQLException {
		executeBatch();
		statement.close();
	}

	@Override
	public boolean isAboveBatchMaximum(){
		return currentBatchedLinesCount > batchedLinesMax;
	}

}
