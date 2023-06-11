package com.example.ygodb.ui.viewcards;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.ArrayList;
import java.util.List;

public class ViewCardsViewModel extends ViewModel {

    private List<OwnedCard> cardsList;

    public static final int LOADING_LIMIT = 100;

    private String sortOrder = null;
    private String sortOption = null;
    private String cardNameSearch = null;

    public ViewCardsViewModel() {
        sortOrder = "dateBought desc, modificationDate desc";
        sortOption = "Date Bought";
        cardsList = new ArrayList<>();
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
        cardsList.addAll(loadMoreData(sortOrder, LOADING_LIMIT, 0, cardNameSearch));

        this.dbRefreshIndicator.postValue(true);
    }

    public List<OwnedCard> getCardsList(){
        return cardsList;
    }

    public List<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
        return AndroidUtil.getDBInstance().queryOwnedCards(orderBy,
                limit, offset, cardNameSearch);
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

    public void setCardsList(List<OwnedCard> cardsList) {
        this.cardsList = cardsList;
    }

}