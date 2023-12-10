package com.example.ygodb.model.popupsortmenu;

import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Map;

public class MenuState {

	private final Map<Integer, MenuItemBean> menuItemMap;
	private Integer currentMenuSelection;
	private boolean isCurrentlyAsc;

	private static final String ERROR_NULL = "current MenuState selection was null";

	public MenuState(Map<Integer, MenuItemBean> menuItemMap, Integer currentMenuSelection) {
		this.menuItemMap = menuItemMap;
		this.currentMenuSelection = currentMenuSelection;
		MenuItemBean current = menuItemMap.get(currentMenuSelection);

		if(current == null){
			YGOLogger.error(ERROR_NULL);
			isCurrentlyAsc = false;
		}
		else{
			isCurrentlyAsc = current.isDefaultSortAsc();
		}
	}

	public void clickOnMenuItem(Integer newMenuSelection){
		if(newMenuSelection.equals(this.currentMenuSelection)){
			isCurrentlyAsc = !isCurrentlyAsc;
		}
		else{
			this.currentMenuSelection = newMenuSelection;
			MenuItemBean current = menuItemMap.get(currentMenuSelection);

			if(current == null){
				YGOLogger.error(ERROR_NULL);
				isCurrentlyAsc = false;
			}
			else {
				isCurrentlyAsc = current.isDefaultSortAsc();
			}
		}
	}

	public void setSelection(Integer currentMenuSelection, boolean isCurrentlyAsc){
		this.currentMenuSelection = currentMenuSelection;
		this.isCurrentlyAsc = isCurrentlyAsc;
	}

	public String getCurrentSelectionText(){
		MenuItemBean current = menuItemMap.get(currentMenuSelection);

		if(current == null){
			YGOLogger.error(ERROR_NULL);
			return "NULL";
		}

		if(isCurrentlyAsc){
			return current.getMenuBaseText() + " (ASC)";
		}
		else {
			return current.getMenuBaseText() + " (DESC)";
		}
	}

	public Integer getCurrentSelectionID(){
		return currentMenuSelection;
	}

	public String getCurrentSelectionSql(){
		MenuItemBean current = menuItemMap.get(currentMenuSelection);

		if(current == null){
			YGOLogger.error(ERROR_NULL);
			return "NULL";
		}

		if(isCurrentlyAsc){
			return current.getMenuSortSqlAsc();
		}
		else{
			return current.getMenuSortSqlDesc();
		}
	}
}
