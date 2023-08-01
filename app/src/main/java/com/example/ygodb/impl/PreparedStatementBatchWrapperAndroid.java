package com.example.ygodb.impl;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.ArrayList;
import java.util.List;

public class PreparedStatementBatchWrapperAndroid implements PreparedStatementBatchWrapper {

	private final SQLiteStatement statement;
	private final SQLiteDatabase connection;
	private final int batchedLinesMax;
	private final BatchSetterAndroid batchSetterAndroid;
	private final List<List<Object>> inputParams;

	private int currentBatchedLinesCount = 0;
	private boolean isFinalized = false;

	public PreparedStatementBatchWrapperAndroid(SQLiteDatabase connection, String input, int maximum, BatchSetterAndroid setter) {
		this.connection = connection;
		statement = connection.compileStatement(input);
		batchedLinesMax = maximum;
		this.batchSetterAndroid = setter;
		inputParams = new ArrayList<>();
	}

	@Override
	public void addSingleValuesSet(List<Object> params) {

		if(isFinalized){
			return;
		}

		inputParams.add(params);
		currentBatchedLinesCount++;

		if(isAboveBatchMaximum()){
			executeBatch();
		}

	}

	@Override
	public void executeBatch() {

		if(isFinalized){
			return;
		}

		if(!connection.inTransaction()) {
			connection.beginTransaction();
		}

		for(List<Object> params : inputParams){
			try {
				batchSetterAndroid.setParams(statement, params);
				statement.execute();
			} catch (Exception e) {
				YGOLogger.error("Issue executing batch query with parameters" + params);
				YGOLogger.logException(e);
			}
		}

		if(connection.inTransaction()) {
			connection.setTransactionSuccessful();
			connection.endTransaction();
		}

		currentBatchedLinesCount = 0;
		inputParams.clear();
	}

	@Override
	public void finalizeBatches() {

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
