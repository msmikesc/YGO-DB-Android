package com.example.ygodb.model.popupsortmenu;

public class MenuItemBean {

    private Integer positionIndex;
	private String menuBaseText;
	private String menuSortSqlDesc;
	private String menuSortSqlAsc;
	private boolean isDefaultSortAsc;

	public MenuItemBean(Integer positionIndex, String menuBaseText, String menuSortSqlDesc, String menuSortSqlAsc, boolean isDefaultSortAsc) {
		this.positionIndex = positionIndex;
		this.menuBaseText = menuBaseText;
		this.menuSortSqlDesc = menuSortSqlDesc;
		this.menuSortSqlAsc = menuSortSqlAsc;
		this.isDefaultSortAsc = isDefaultSortAsc;
	}

	public String getMenuBaseText() {
		return menuBaseText;
	}

	public void setMenuBaseText(String menuBaseText) {
		this.menuBaseText = menuBaseText;
	}

	public String getMenuSortSqlDesc() {
		return menuSortSqlDesc;
	}

	public void setMenuSortSqlDesc(String menuSortSqlDesc) {
		this.menuSortSqlDesc = menuSortSqlDesc;
	}

	public String getMenuSortSqlAsc() {
		return menuSortSqlAsc;
	}

	public void setMenuSortSqlAsc(String menuSortSqlAsc) {
		this.menuSortSqlAsc = menuSortSqlAsc;
	}

	public boolean isDefaultSortAsc() {
		return isDefaultSortAsc;
	}

	public void setDefaultSortAsc(boolean defaultSortAsc) {
		isDefaultSortAsc = defaultSortAsc;
	}

	public Integer getPositionIndex() {
		return positionIndex;
	}

	public void setPositionIndex(Integer positionIndex) {
		this.positionIndex = positionIndex;
	}
}
