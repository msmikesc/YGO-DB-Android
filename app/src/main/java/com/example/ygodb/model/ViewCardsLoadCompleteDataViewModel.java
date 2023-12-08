package com.example.ygodb.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemComparatorBean;
import com.example.ygodb.abs.MenuStateComparator;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class ViewCardsLoadCompleteDataViewModel extends ViewCardsBaseViewModel<OwnedCard> {
	protected List<OwnedCard> filteredCardsList;
	protected String setNameSearch = null;
	protected boolean isCardNameMode = true;
	protected final List<String> setNamesDropdownList = new ArrayList<>();
	protected MenuStateComparator menuState;

	protected ViewCardsLoadCompleteDataViewModel() {
		super();
		filteredCardsList = new ArrayList<>();
		isCardNameMode = true;
		menuState = new MenuStateComparator(createMenuMap(), 0);
	}

	protected abstract Map<Integer, MenuItemComparatorBean> createMenuMap();

	public abstract List<OwnedCard> getInitialData(String setName);

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

	public List<OwnedCard> getFilteredCardsList() {
		return filteredCardsList;
	}

	public Comparator<OwnedCard> getSortOption() {
		return menuState.getCurrentComparator();
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

	public void setFilteredCardsList(List<OwnedCard> filteredCardsList) {
		this.filteredCardsList = filteredCardsList;
	}

	public MenuStateComparator getMenuState() {
		return menuState;
	}

}
