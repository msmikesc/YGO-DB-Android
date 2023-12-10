package com.example.ygodb.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public abstract class ViewCardsBaseViewModel<T> extends ViewModel {

	protected List<T> cardsList;
	protected String cardNameSearch = null;
	protected long currentSearchStartTime = 0;
	protected final MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<>(false);
	protected String currentlySelectedRarityFilter = "";
	protected List<String> rarityFiltersList;

	protected ViewCardsBaseViewModel() {
		cardsList = new ArrayList<>();
		rarityFiltersList = new ArrayList<>();
	}

	public abstract void refreshViewDBUpdate();

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
}
