package com.example.ygodb.ui.viewsetboxes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.utility.YGOLogger;

import java.sql.SQLException;
import java.util.List;

public class ViewBoxSetViewModel extends ViewModel {

	private List<SetBox> boxList;
	private String currentSearchText = "";

	public ViewBoxSetViewModel() {
		boxList = getInitialData();
	}

	private final MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<>(false);

	public MutableLiveData<Boolean> getDbRefreshIndicator() {
		return dbRefreshIndicator;
	}

	public void setDbRefreshIndicatorFalse() {
		this.dbRefreshIndicator.setValue(false);
	}

	public void refreshViewDBUpdate() {
		List<SetBox> results = getSearchData(currentSearchText);
		boxList.clear();
		boxList.addAll(results);

		this.dbRefreshIndicator.postValue(true);
	}

	public List<SetBox> getInitialData() {
		try {
			return AndroidUtil.getDBInstance().getAllSetBoxes();
		} catch (SQLException e) {
			YGOLogger.logException(e);
			throw new RuntimeException(e);
		}
	}

	public List<SetBox> getSearchData(String input) {

		if (input == null || input.isBlank()) {
			try {
				return AndroidUtil.getDBInstance().getAllSetBoxes();
			} catch (SQLException e) {
				YGOLogger.logException(e);
				throw new RuntimeException(e);
			}
		}

		try {
			return AndroidUtil.getDBInstance().getSetBoxesByNameOrCodeOrLabel(input);
		} catch (SQLException e) {
			YGOLogger.logException(e);
			throw new RuntimeException(e);
		}
	}

	public boolean attemptToAddNewFromSetPrefix(String setPrefix) {

		List<SetBox> newSetBoxDataForValidSetPrefix;
		try {
			newSetBoxDataForValidSetPrefix = AndroidUtil.getDBInstance().getNewSetBoxDataForValidSetPrefix(setPrefix);
			if (newSetBoxDataForValidSetPrefix.isEmpty()) {
				return false;
			}

			SetBox setBox = newSetBoxDataForValidSetPrefix.get(0);

			setBox.setSetBoxUUID(java.util.UUID.randomUUID().toString());

			AndroidUtil.getDBInstance().insertIntoSetBoxes(setBox);
			boxList.add(0, setBox);
			return true;
		} catch (SQLException e) {
			YGOLogger.logException(e);
			return false;
		}
	}

	public List<SetBox> getBoxList() {
		return boxList;
	}

	public void setBoxList(List<SetBox> boxList) {
		this.boxList = boxList;
	}

	public String getCurrentSearchText() {
		return currentSearchText;
	}

	public void setCurrentSearchText(String currentSearchText) {
		this.currentSearchText = currentSearchText;
	}

}