package com.example.ygodb.ui.viewcardset;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemComparatorBean;
import com.example.ygodb.abs.MenuStateComparator;
import com.example.ygodb.abs.OwnedCardNameComparatorAsc;
import com.example.ygodb.abs.OwnedCardNameComparatorDesc;
import com.example.ygodb.abs.OwnedCardPriceComparatorAsc;
import com.example.ygodb.abs.OwnedCardPriceComparatorDesc;
import com.example.ygodb.abs.OwnedCardQuantityComparatorAsc;
import com.example.ygodb.abs.OwnedCardQuantityComparatorDesc;
import com.example.ygodb.abs.OwnedCardSetNumberComparatorAsc;
import com.example.ygodb.abs.OwnedCardSetNumberComparatorDesc;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewCardSetViewModel extends ViewModel {

	protected List<OwnedCard> cardsList;
	protected List<OwnedCard> filteredCardsList;
	protected String cardNameSearch = null;
	protected String setNameSearch = null;
	protected boolean isCardNameMode = true;
	protected long currentSearchStartTime = 0;
	protected final List<String> setNamesDropdownList = new ArrayList<>();
	protected MenuStateComparator menuState;

	public ViewCardSetViewModel() {
		cardsList = new ArrayList<>();
		filteredCardsList = new ArrayList<>();
		isCardNameMode = true;
		menuState = new MenuStateComparator(createMenuMap(), 0);
	}

	protected Map<Integer, MenuItemComparatorBean> createMenuMap(){

		Map<Integer, MenuItemComparatorBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemComparatorBean(
				0,
				"Quantity",
				new OwnedCardQuantityComparatorAsc(),
				new OwnedCardQuantityComparatorDesc(),
				true
		));
		menuItemMap.put(1, new MenuItemComparatorBean(
				1,
				"Card Name",
				new OwnedCardNameComparatorAsc(),
				new OwnedCardNameComparatorDesc(),
				true
		));
		menuItemMap.put(2, new MenuItemComparatorBean(
				2,
				"Set Number",
				new OwnedCardSetNumberComparatorAsc(),
				new OwnedCardSetNumberComparatorDesc(),
				true
		));
		menuItemMap.put(3, new MenuItemComparatorBean(
				3,
				"Price",
				new OwnedCardPriceComparatorAsc(),
				new OwnedCardPriceComparatorDesc(),
				false
		));

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
		if (!isCardNameMode) {
			List<OwnedCard> results = getInitialData(setNameSearch);
			cardsList.clear();
			cardsList.addAll(results);

			List<OwnedCard> filteredResults = getFilteredList(results, cardNameSearch);
			filteredCardsList.clear();
			filteredCardsList.addAll(filteredResults);
			sortData(filteredCardsList, getSortOption());
		} else {
			List<OwnedCard> results = getInitialCardNameData(cardNameSearch);
			filteredCardsList.clear();
			filteredCardsList.addAll(results);
			sortData(filteredCardsList, getSortOption());
		}

		this.dbRefreshIndicator.postValue(true);
	}

	public List<OwnedCard> getInitialCardNameData(String cardName) {

		if (cardName == null || cardName.isBlank() || cardName.trim().length() < 3) {
			return new ArrayList<>();
		}

		List<OwnedCard> results = AndroidUtil.getDBInstance().getAllPossibleCardsByNameSearch(cardName, null);

		if (!results.isEmpty()) {
			isCardNameMode = true;
		}

		return results;
	}

	public List<OwnedCard> getInitialData(String setName) {

		if (setName == null || setName.isBlank() || setName.trim().length() < 3) {
			isCardNameMode = true;
			return getInitialCardNameData(cardNameSearch);
		}

		setName = setName.trim();

		List<OwnedCard> newList = AndroidUtil.getDBInstance().getAllPossibleCardsBySetName(setName, null);

		if (newList.isEmpty()) {
			newList = AndroidUtil.getDBInstance().getAllPossibleCardsByArchetype(setName, null);
		}

		if (!newList.isEmpty()) {
			isCardNameMode = false;
		}

		return newList;
	}

	public List<OwnedCard> getFilteredList(List<OwnedCard> inputList, String filter) {

		ArrayList<OwnedCard> newList = new ArrayList<>();

		for (OwnedCard current : inputList) {
			if (filter == null || filter.equals("") ||
					current.getCardName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) {
				newList.add(current);
			}
		}

		return newList;
	}

	public void sortData(List<OwnedCard> cardsList, Comparator<OwnedCard> cardComparator) {
		cardsList.sort(cardComparator);
	}

	public List<OwnedCard> getCardsList() {
		return cardsList;
	}

	public List<OwnedCard> getFilteredCardsList() {
		return filteredCardsList;
	}

	public Comparator<OwnedCard> getSortOption() {
		return menuState.getCurrentComparator();
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

	public String getSetNameSearch() {

		if (setNameSearch == null) {
			return "";
		}

		return setNameSearch;
	}

	public void setSetNameSearch(String setNameSearch) {
		this.setNameSearch = setNameSearch;
	}

	public List<String> getSetNamesDropdownList() {
		return setNamesDropdownList;
	}

	public void updateSetNamesDropdownList(List<String> input) {
		setNamesDropdownList.clear();
		setNamesDropdownList.addAll(input);
	}

	public boolean isCardNameMode() {
		return isCardNameMode;
	}

	public void setCardsList(List<OwnedCard> cardsList) {
		this.cardsList = cardsList;
	}

	public void setFilteredCardsList(List<OwnedCard> filteredCardsList) {
		this.filteredCardsList = filteredCardsList;
	}

	public long getCurrentSearchStartTime() {
		return currentSearchStartTime;
	}

	public void setCurrentSearchStartTime(long currentSearchStartTime) {
		this.currentSearchStartTime = currentSearchStartTime;
	}

	public MenuStateComparator getMenuState() {
		return menuState;
	}
}