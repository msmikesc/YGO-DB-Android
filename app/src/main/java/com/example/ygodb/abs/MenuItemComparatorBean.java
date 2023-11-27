package com.example.ygodb.abs;

import ygodb.commonlibrary.bean.OwnedCard;

import java.util.Comparator;

public class MenuItemComparatorBean {

    private Integer positionIndex;
	private String menuBaseText;
	private Comparator<OwnedCard> comparatorAsc;
	private Comparator<OwnedCard> comparatorDesc;
	private boolean isDefaultSortAsc;

	public MenuItemComparatorBean(Integer positionIndex, String menuBaseText, Comparator<OwnedCard> comparatorAsc, Comparator<OwnedCard> comparatorDesc, boolean isDefaultSortAsc) {
		this.positionIndex = positionIndex;
		this.menuBaseText = menuBaseText;
		this.comparatorAsc = comparatorAsc;
		this.comparatorDesc = comparatorDesc;
		this.isDefaultSortAsc = isDefaultSortAsc;
	}

	public String getMenuBaseText() {
		return menuBaseText;
	}

	public void setMenuBaseText(String menuBaseText) {
		this.menuBaseText = menuBaseText;
	}

	public Integer getPositionIndex() {
		return positionIndex;
	}

	public void setPositionIndex(Integer positionIndex) {
		this.positionIndex = positionIndex;
	}

	public Comparator<OwnedCard> getComparatorAsc() {
		return comparatorAsc;
	}

	public void setComparatorAsc(Comparator<OwnedCard> comparatorAsc) {
		this.comparatorAsc = comparatorAsc;
	}

	public Comparator<OwnedCard> getComparatorDesc() {
		return comparatorDesc;
	}

	public void setComparatorDesc(Comparator<OwnedCard> comparatorDesc) {
		this.comparatorDesc = comparatorDesc;
	}

	public boolean isDefaultSortAsc() {
		return isDefaultSortAsc;
	}

	public void setDefaultSortAsc(boolean defaultSortAsc) {
		isDefaultSortAsc = defaultSortAsc;
	}
}
