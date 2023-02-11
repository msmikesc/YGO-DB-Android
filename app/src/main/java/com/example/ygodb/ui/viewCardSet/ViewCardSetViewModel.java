package com.example.ygodb.ui.viewCardSet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.OwnedCardQuantityComparator;
import com.example.ygodb.backend.analyze.AnalyzeCardsInSet;
import com.example.ygodb.backend.bean.AnalyzeData;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.connection.SQLiteConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewCardSetViewModel extends ViewModel {

    private ArrayList<OwnedCard> cardsList;
    private ArrayList<OwnedCard> filteredCardsList;

    private static Comparator<OwnedCard> currentComparator = null;
    private String sortOption = null;
    private String cardNameSearch = null;
    private String setNameSearch = null;

    private String[] setNamesDropdownList = null;

    public ViewCardSetViewModel() {
        currentComparator = new OwnedCardQuantityComparator();
        sortOption = "Quantity";
        cardsList = new ArrayList<>();
        filteredCardsList = new ArrayList<>();

        ArrayList<String> setNamesArrayList = SQLiteConnection.getObj().getDistinctSetNames();

        setNamesDropdownList = new String[setNamesArrayList.size()];

        setNamesArrayList.toArray(setNamesDropdownList);
    }

    public void loadInitialData(String setName) {

        AnalyzeCardsInSet runner = new AnalyzeCardsInSet();

        ArrayList<AnalyzeData> results = null;
        ArrayList<OwnedCard> newList = new ArrayList<>();

        if(setName == null || setName.equals("")){
            cardsList.clear();
            filteredCardsList.clear();
            return;
        }

        try {
            results = runner.runFor(setName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(AnalyzeData current: results){
            OwnedCard currentCard = new OwnedCard();
            currentCard.cardName = current.cardName;
            currentCard.setRarity = current.getStringOfMainRarities();
            currentCard.id = current.id;
            currentCard.setName = current.getStringOfSetNames();
            currentCard.quantity = current.quantity;
            currentCard.setNumber = current.getStringOfMainSetNumbers();
            currentCard.priceBought = current.getAveragePrice();
            newList.add(currentCard);
        }

        sortData(newList, currentComparator);

        cardsList.clear();
        filteredCardsList.clear();

        cardsList.addAll(newList);

        for(OwnedCard current: cardsList){
            if(cardNameSearch == null ||cardNameSearch.equals("") || current.cardName.toUpperCase().contains(cardNameSearch.toUpperCase())){
                filteredCardsList.add(current);
            }
        }
    }

    public void sortData(ArrayList<OwnedCard> cardsList, Comparator<OwnedCard> currentComparator){
        Collections.sort(cardsList, currentComparator);
    }

    public ArrayList<OwnedCard> getCardsList() {
        return cardsList;
    }

    public ArrayList<OwnedCard> getFilteredCardsList() {
        return filteredCardsList;
    }

    public String getSortOption() {
        return sortOption;
    }

    public void setSortOption(String sortOption) {
        this.sortOption = sortOption;
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

    public String[] getSetNamesDropdownList() {
        return setNamesDropdownList;
    }

    public static Comparator<OwnedCard> getCurrentComparator() {
        return currentComparator;
    }

    public static void setCurrentComparator(Comparator<OwnedCard> currentComparator) {
        ViewCardSetViewModel.currentComparator = currentComparator;
    }
}