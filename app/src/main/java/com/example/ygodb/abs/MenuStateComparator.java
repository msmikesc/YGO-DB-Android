package com.example.ygodb.abs;

import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.Comparator;
import java.util.Map;

public class MenuStateComparator {

	private final Map<Integer, MenuItemComparatorBean> menuItemMap;
	private Integer currentMenuSelection;
	private boolean isCurrentlyAsc;

	private static final String ERROR_NULL = "current MenuState selection was null";

	public MenuStateComparator(Map<Integer, MenuItemComparatorBean> menuItemMap, Integer currentMenuSelection) {
		this.menuItemMap = menuItemMap;
		this.currentMenuSelection = currentMenuSelection;
		MenuItemComparatorBean current = menuItemMap.get(currentMenuSelection);

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
			MenuItemComparatorBean current = menuItemMap.get(currentMenuSelection);

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
		MenuItemComparatorBean current = menuItemMap.get(currentMenuSelection);

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

	public Comparator<OwnedCard> getCurrentComparator(){
		MenuItemComparatorBean current = menuItemMap.get(currentMenuSelection);

		if(current == null){
			YGOLogger.error(ERROR_NULL);
			return null;
		}

		if(isCurrentlyAsc){
			return current.getComparatorAsc();
		}
		else{
			return current.getComparatorDesc();
		}
	}
}
