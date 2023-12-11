package com.example.ygodb.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.model.popupfiltermenu.FilterItemBean;
import com.example.ygodb.model.popupfiltermenu.FilterState;
import ygodb.commonlibrary.bean.Rarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ViewCardsBaseViewModel<T> extends ViewModel {

	protected List<T> cardsList;
	protected String cardNameSearch = null;
	protected long currentSearchStartTime = 0;
	protected final MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<>(false);
	protected String currentlySelectedRarityFilter = "";
	protected List<String> rarityFiltersList;

	protected FilterState filterState;

	protected ViewCardsBaseViewModel() {
		cardsList = new ArrayList<>();
		rarityFiltersList = new ArrayList<>();
	}

	public abstract void refreshViewDBUpdate();

	public Map<Integer, FilterItemBean> getFiltersMap(List<String> filterList){

		Map<Integer, FilterItemBean> results = new HashMap<>();

		for(int i =0 ; i< filterList.size(); i++){
			results.put(i,new FilterItemBean(i,filterList.get(i), filterList.get(i)));
		}

		return results;
	}

	public void updateFilterStateFromRarityCollection(Collection<String> raritySet) {

		List<String> sortedList = Rarity.getSortedListFromCollection(raritySet);

		rarityFiltersList.clear();
		rarityFiltersList.addAll(sortedList);

		Map<Integer, FilterItemBean> filterMap = getFiltersMap(getRarityFiltersList());

		String currentFilter = filterState.getCurrentSelectionFilterString();

		Integer currentItemPosition = null;

		boolean addFinalMissingCurrentlySelectedRarity = true;

		for(FilterItemBean currentMenuItem: filterMap.values()){
			if(currentMenuItem.getMenuFilterString().equals(currentFilter)){
				currentItemPosition = currentMenuItem.getPositionIndex();
				addFinalMissingCurrentlySelectedRarity = false;
				break;
			}
		}

		if(addFinalMissingCurrentlySelectedRarity && currentFilter != null){
			rarityFiltersList.add(currentFilter);
			currentItemPosition = filterMap.size();
			filterMap.put(filterMap.size(),
						  new FilterItemBean(filterMap.size(),currentFilter, currentFilter));
		}

		filterState = new FilterState(filterMap, currentItemPosition);
	}

	public MutableLiveData<Boolean> getDbRefreshIndicator() {
		return dbRefreshIndicator;
	}

	public void setDbRefreshIndicatorFalse() {
		this.dbRefreshIndicator.setValue(false);
	}

	public List<T> getCardsList() {
		return cardsList;
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

	public void setCardsList(List<T> cardsList) {
		this.cardsList = cardsList;
	}

	public long getCurrentSearchStartTime() {
		return currentSearchStartTime;
	}

	public void setCurrentSearchStartTime(long currentSearchStartTime) {
		this.currentSearchStartTime = currentSearchStartTime;
	}

	public String getCurrentlySelectedRarityFilter() {
		return currentlySelectedRarityFilter;
	}

	public void setCurrentlySelectedRarityFilter(String currentlySelectedRarityFilter) {
		this.currentlySelectedRarityFilter = currentlySelectedRarityFilter;
	}

	public List<String> getRarityFiltersList() {
		return rarityFiltersList;
	}

	public void setRarityFiltersList(List<String> rarityFiltersList) {
		this.rarityFiltersList = rarityFiltersList;
	}

	public FilterState getFilterState() {
		return filterState;
	}
}
