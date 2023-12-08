package com.example.ygodb.model;

import com.example.ygodb.abs.MenuItemBean;
import com.example.ygodb.abs.MenuState;

import java.util.List;
import java.util.Map;

public abstract class ViewCardsLoadPartialScrollViewModel<T> extends ViewCardsBaseViewModel<T> {

	public static final int LOADING_LIMIT = 100;

	protected final MenuState menuState;

	protected ViewCardsLoadPartialScrollViewModel() {
		super();
		menuState = new MenuState(createMenuMap(), 0);
	}

	protected abstract Map<Integer, MenuItemBean> createMenuMap();

	public abstract List<T> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch);

	public void refreshViewDBUpdate() {
		cardsList.clear();
		cardsList.addAll(loadMoreData(getSortOrder(), LOADING_LIMIT, 0, cardNameSearch));

		this.dbRefreshIndicator.postValue(true);
	}

	public String getSortOrder() {
		return menuState.getCurrentSelectionSql();
	}

	public MenuState getMenuState() {
		return menuState;
	}

}
