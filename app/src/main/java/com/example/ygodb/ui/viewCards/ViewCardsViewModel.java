package com.example.ygodb.ui.viewCards;

import static java.lang.Thread.sleep;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.SQLiteConnection;

import java.util.ArrayList;

public class ViewCardsViewModel extends ViewModel {

    private ArrayList<OwnedCard> cardsList;

    public static final int LOADING_LIMIT = 100;

    private String sortOrder = null;
    private String sortOption = null;
    private String cardNameSearch = null;

    public ViewCardsViewModel() {
        sortOrder = "dateBought desc, cardName asc";
        sortOption = "Date Bought";
        cardsList = new ArrayList<>();
    }

    public ArrayList<OwnedCard> getCardsList(){
        return cardsList;
    }

    public ArrayList<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
        ArrayList<OwnedCard> newList = SQLiteConnection.getObj().queryOwnedCards(orderBy,
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

}