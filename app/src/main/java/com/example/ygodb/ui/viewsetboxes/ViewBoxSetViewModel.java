package com.example.ygodb.ui.viewsetboxes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonlibrary.bean.SetBox;

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
		return AndroidUtil.getDBInstance().getAllSetBoxes();
	}

	public List<SetBox> getSearchData(String input) {

		if (input == null || input.isBlank() || input.trim().length() < 3) {
			return AndroidUtil.getDBInstance().getAllSetBoxes();
		}

		return AndroidUtil.getDBInstance().getSetBoxesByNameOrCode(input);
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