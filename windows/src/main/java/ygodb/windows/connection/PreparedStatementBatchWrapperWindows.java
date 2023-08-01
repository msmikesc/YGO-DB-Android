package ygodb.windows.connection;

import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PreparedStatementBatchWrapperWindows implements PreparedStatementBatchWrapper {

	private final PreparedStatement statement;
	private final Connection connection;
	private final int batchedLinesMax;
	private final BatchSetterWindows batchSetterWindows;

	private int currentBatchedLinesCount = 0;
	private boolean isFinalized = false;

	public PreparedStatementBatchWrapperWindows(Connection connection, String input, int maximum, BatchSetterWindows setter) throws SQLException {
		this.connection = connection;
		connection.setAutoCommit(false);
		statement = connection.prepareStatement(input);
		batchedLinesMax = maximum;
		this.batchSetterWindows = setter;
	}

	@Override
	public void addSingleValuesSet(List<Object> params) throws SQLException {

		if(isFinalized){
			return;
		}

		batchSetterWindows.setParams(statement, params);
		currentBatchedLinesCount++;
		statement.addBatch();

		if(isAboveBatchMaximum()){
			executeBatch();
		}
	}

	@Override
	public void executeBatch() throws SQLException {

		if(isFinalized){
			return;
		}

		statement.executeBatch();
		connection.commit();
		currentBatchedLinesCount = 0;
	}

	@Override
	public void finalizeBatches() throws SQLException {

		if(isFinalized){
			return;
		}

		executeBatch();
		statement.close();
		isFinalized = true;
	}

	@Override
	public boolean isFinalized(){
		return isFinalized;
	}

	@Override
	public boolean isAboveBatchMaximum(){
		return currentBatchedLinesCount >= batchedLinesMax;
	}

}
