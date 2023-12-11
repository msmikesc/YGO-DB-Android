package com.example.ygodb.model.completedata;

import com.example.ygodb.model.ViewCardsBaseViewModel;
import com.example.ygodb.model.popupfiltermenu.FilterState;
import com.example.ygodb.model.popupsortmenu.MenuItemComparatorBean;
import com.example.ygodb.model.popupsortmenu.MenuStateComparator;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
		filterState = new FilterState(getFiltersMap(getRarityFiltersList()), null);
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

		Set<String> raritySet = new HashSet<>();

		for (OwnedCard current : results) {
			raritySet.add(current.getSetRarity());
		}

		updateFilterStateFromRarityCollection(raritySet);

		return results;
	}

	public List<OwnedCard> getFilteredList(List<OwnedCard> inputList, String filter) {

		ArrayList<OwnedCard> newList = new ArrayList<>();
		String rarityFilter = getCurrentlySelectedRarityFilter();

		Set<String> raritySet = new HashSet<>();

		for (OwnedCard current : inputList) {
			raritySet.add(current.getSetRarity());
			if ((filter == null || filter.isBlank() ||
					current.getCardName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))
			&& (rarityFilter == null || rarityFilter.isBlank() ||
					rarityFilter.equals(current.getSetRarity()))) {
				newList.add(current);
			}
		}

		updateFilterStateFromRarityCollection(raritySet);

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
