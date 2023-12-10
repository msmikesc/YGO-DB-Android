package com.example.ygodb.model.popupfiltermenu;

public class FilterItemBean {

    private Integer positionIndex;
	private String menuBaseText;
	private String menuFilterString;

	public FilterItemBean(Integer positionIndex, String menuBaseText, String menuFilterString) {
		this.positionIndex = positionIndex;
		this.menuBaseText = menuBaseText;
		this.menuFilterString = menuFilterString;
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

	public String getMenuFilterString() {
		return menuFilterString;
	}

	public void setMenuFilterString(String menuFilterString) {
		this.menuFilterString = menuFilterString;
	}
}
