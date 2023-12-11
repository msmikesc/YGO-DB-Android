package com.example.ygodb.model.partialscroll;

import com.example.ygodb.model.ViewCardsBaseViewModel;
import com.example.ygodb.model.popupsortmenu.MenuItemBean;
import com.example.ygodb.model.popupsortmenu.MenuState;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.Rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ViewCardsLoadPartialScrollViewModel<T> extends ViewCardsBaseViewModel<T> {

	public static final int LOADING_LIMIT = 100;

	protected final MenuState menuState;

	protected ViewCardsLoadPartialScrollViewModel() {
		super();
		menuState = new MenuState(createMenuMap(), 0);
	}

	protected abstract Map<Integer, MenuItemBean> createMenuMap();

	public abstract List<T> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch, String rarityFilter);

	public void refreshViewDBUpdate() {
		cardsList.clear();
		cardsList.addAll(loadMoreData(getSortOrder(), LOADING_LIMIT, 0, cardNameSearch, getCurrentlySelectedRarityFilter()));

		this.dbRefreshIndicator.postValue(true);
	}

	public String getSortOrder() {
		return menuState.getCurrentSelectionSql();
	}

	public MenuState getMenuState() {
		return menuState;
	}

	public List<String> getRarityListFor(String cardNameSearch) {

		List<String> dbResults = AndroidUtil.getDBInstance().queryRaritiesFor(cardNameSearch);

		return Rarity.getSortedListFromCollection(dbResults);
	}

}
