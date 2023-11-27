package com.example.ygodb.ui.viewSoldCards;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemBean;
import com.example.ygodb.abs.MenuState;
import ygodb.commonlibrary.bean.SoldCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewSoldCardsViewModel extends ViewModel {

	private List<SoldCard> cardsList;

	public static final int LOADING_LIMIT = 100;

	private String cardNameSearch = null;
	protected long currentSearchStartTime = 0;

	private final MenuState menuState;

	public ViewSoldCardsViewModel() {
		cardsList = new ArrayList<>();
		menuState = new MenuState(createMenuMap(), 0);
	}

	private Map<Integer, MenuItemBean> createMenuMap(){

		Map<Integer, MenuItemBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemBean(
				0,
				"Date Sold",
				"dateSold desc, modificationDate desc",
				"dateSold asc, modificationDate asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc, dateSold desc",
				"cardName asc, dateSold desc",
				true));
		menuItemMap.put(2, new MenuItemBean(
				2,
				"Set Number",
				"setName desc, setNumber desc",
				"setName asc, setNumber asc",
				true));
		menuItemMap.put(3, new MenuItemBean(
				3,
				"Price",
				"priceSold desc, cardName asc",
				"priceSold asc, cardName asc",
				false));

		return menuItemMap;
	}

	private final MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<>(false);

	public MutableLiveData<Boolean> getDbRefreshIndicator() {
		return dbRefreshIndicator;
	}

	public void setDbRefreshIndicatorFalse() {
		this.dbRefreshIndicator.setValue(false);
	}

	public void refreshViewDBUpdate() {
		cardsList.clear();
		cardsList.addAll(loadMoreData(getSortOrder(), LOADING_LIMIT, 0, cardNameSearch));

		this.dbRefreshIndicator.postValue(true);
	}

	public List<SoldCard> getCardsList() {
		return cardsList;
	}

	public List<SoldCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
		return AndroidUtil.getDBInstance().querySoldCards(orderBy, limit, offset, cardNameSearch);
	}

	public String getSortOrder() {
		return menuState.getCurrentSelectionSql();
	}

	public String getCardNameSearch() {

		if (cardNameSearch == null) {
			return "";
		}

		return cardNameSearch;
	}

	public void setCardNameSearch(String cardNameSearch) {
		this.cardNameSearch = cardNameSearch;
	}

	public void setCardsList(List<SoldCard> cardsList) {
		this.cardsList = cardsList;
	}

	public long getCurrentSearchStartTime() {
		return currentSearchStartTime;
	}

	public void setCurrentSearchStartTime(long currentSearchStartTime) {
		this.currentSearchStartTime = currentSearchStartTime;
	}

	public MenuState getMenuState() {
		return menuState;
	}
}