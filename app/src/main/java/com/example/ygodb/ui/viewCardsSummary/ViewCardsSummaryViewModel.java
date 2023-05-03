package com.example.ygodb.ui.viewCardsSummary;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonLibrary.bean.OwnedCard;

import java.util.ArrayList;

public class ViewCardsSummaryViewModel extends ViewModel {

    private ArrayList<OwnedCard> cardsList;
    public static final int LOADING_LIMIT = 100;
    private String sortOrder = null;
    private String sortOption = null;
    private String cardNameSearch = null;

    public ViewCardsSummaryViewModel() {
        sortOrder = "maxDate desc, cardName asc";
        sortOption = "Date Bought";
        cardsList = new ArrayList<>();
    }

    private MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<Boolean>(false);

    public MutableLiveData<Boolean> getDbRefreshIndicator() {
        return dbRefreshIndicator;
    }

    public void setDbRefreshIndicatorFalse() {
        this.dbRefreshIndicator.setValue(false);
    }

    public void refreshViewDBUpdate() {
        cardsList.clear();
        cardsList.addAll(loadMoreData(sortOrder, LOADING_LIMIT, 0, cardNameSearch));

        this.dbRefreshIndicator.postValue(true);
    }

    public ArrayList<OwnedCard> getCardsList(){
        return cardsList;
    }

    public ArrayList<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
        ArrayList<OwnedCard> newList = AndroidUtil.getDBInstance().queryOwnedCardsGrouped(orderBy,
                limit, offset, cardNameSearch);
        return newList;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortOption() {
        return sortOption;
    }

    public void setSortOption(String sortOption) {
        this.sortOption = sortOption;
    }

    public String getCardNameSearch() {

        if(cardNameSearch == null){
            return "";
        }

        return cardNameSearch;
    }

    public void setCardNameSearch(String cardNameSearch) {
        this.cardNameSearch = cardNameSearch;
    }

    public void setCardsList(ArrayList<OwnedCard> cardsList) {
        this.cardsList = cardsList;
    }
}