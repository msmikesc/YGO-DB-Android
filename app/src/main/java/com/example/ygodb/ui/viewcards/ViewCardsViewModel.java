package com.example.ygodb.ui.viewcards;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemBean;
import com.example.ygodb.abs.MenuState;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCardsViewModel extends ViewModel {

	private List<OwnedCard> cardsList;

	public static final int LOADING_LIMIT = 100;
	private String cardNameSearch = null;
	protected long currentSearchStartTime = 0;
	private final MenuState menuState;

	public ViewCardsViewModel() {
		cardsList = new ArrayList<>();
		menuState = new MenuState(createMenuMap(), 0);
	}

	private Map<Integer, MenuItemBean> createMenuMap(){

		Map<Integer, MenuItemBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemBean(
				0,
				"Date Bought",
				"dateBought desc, modificationDate desc",
				"dateBought asc, modificationDate asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc, dateBought desc",
				"cardName asc, dateBought desc",
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
				"priceBought desc, cardName asc",
				"priceBought asc, cardName asc",
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

	public List<OwnedCard> getCardsList() {
		return cardsList;
	}

	public List<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
		return AndroidUtil.getDBInstance().queryOwnedCards(orderBy, limit, offset, cardNameSearch);
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

	public void setCardsList(List<OwnedCard> cardsList) {
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