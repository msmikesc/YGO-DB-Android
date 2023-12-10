package com.example.ygodb.model.popupfiltermenu;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Map;

public class FilterState {

	private final Map<Integer, FilterItemBean> filterItemMap;
	private Integer currentMenuSelection;

	private static final String ERROR_NULL = "current MenuState selection was null";

	public FilterState(Map<Integer, FilterItemBean> filterItemMap, Integer currentMenuSelection) {
		this.filterItemMap = filterItemMap;
		this.currentMenuSelection = currentMenuSelection;
	}

	public void clickOnMenuItem(Integer newMenuSelection){

		if(newMenuSelection.equals(this.currentMenuSelection)){
			currentMenuSelection = null;
		}
		else{
			this.currentMenuSelection = newMenuSelection;
		}
	}

	public void setSelection(Integer currentMenuSelection){
		this.currentMenuSelection = currentMenuSelection;
	}

	public String getCurrentSelectionText(){
		FilterItemBean current = filterItemMap.get(currentMenuSelection);

		if(current == null){
			YGOLogger.error(ERROR_NULL);
			return "NULL";
		}

		return current.getMenuBaseText() + " ✅";
	}

	public Integer getCurrentSelectionID(){
		return currentMenuSelection;
	}

	public String getCurrentSelectionFilterString(){
		FilterItemBean current = filterItemMap.get(currentMenuSelection);

		if(current == null){
			return null;
		}

		return current.getMenuFilterString();
	}
}
