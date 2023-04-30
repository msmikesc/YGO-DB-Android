package com.example.ygodb.ui.viewCardSet;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.OwnedCardQuantityComparator;

import analyze.AnalyzeCardsInSet;
import bean.AnalyzeData;
import bean.OwnedCard;

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

    private boolean isCardNameMode = true;

    public String[] setNamesDropdownList = null;

    public ViewCardSetViewModel() {
        currentComparator = new OwnedCardQuantityComparator();
        sortOption = "Quantity";
        cardsList = new ArrayList<>();
        filteredCardsList = new ArrayList<>();
        isCardNameMode = true;
    }

    private MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<Boolean>(false);

    public MutableLiveData<Boolean> getDbRefreshIndicator() {
        return dbRefreshIndicator;
    }

    public void setDbRefreshIndicatorFalse() {
        this.dbRefreshIndicator.setValue(false);
    }

    public void refreshViewDBUpdate() {
        if(!isCardNameMode) {
            ArrayList<OwnedCard> results = getInitialData(setNameSearch);
            cardsList.clear();
            cardsList.addAll(results);

            ArrayList<OwnedCard> filteredResults = getFilteredList(results, cardNameSearch);
            filteredCardsList.clear();
            filteredCardsList.addAll(filteredResults);
        }
        else{
            ArrayList<OwnedCard> results = getInitialCardNameData(cardNameSearch);
            filteredCardsList.clear();
            filteredCardsList.addAll(results);
        }

        this.dbRefreshIndicator.postValue(true);
    }

    public ArrayList<OwnedCard> getInitialCardNameData(String cardName) {

        ArrayList<OwnedCard> results = null;

        if(cardName == null || cardName.trim().equals("") || cardName.trim().length() < 3){
            return new ArrayList<>();
        }

        results = AndroidUtil.getDBInstance().getAllPossibleCardsByNameSearch(cardName,
                "a.cardName asc, a.setNumber asc, a.setRarity asc");

        if(results.size() > 0){
            isCardNameMode = true;
        }
        sortOption = "Default";

        return results;
    }

    public ArrayList<OwnedCard> getInitialData(String setName) {

        AnalyzeCardsInSet runner = new AnalyzeCardsInSet();

        ArrayList<AnalyzeData> results = null;
        ArrayList<OwnedCard> newList = new ArrayList<>();

        if(setName == null || setName.equals("") || setName.trim().length() < 3){
            cardsList.clear();
            filteredCardsList.clear();
            isCardNameMode = true;
            if(cardNameSearch != null && cardNameSearch.length() > 0){
                return getInitialCardNameData(cardNameSearch);
            }
            return newList;
        }

        try {
            results = runner.runFor(setName, AndroidUtil.getDBInstance());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for(AnalyzeData current: results){
            OwnedCard currentCard = new OwnedCard();
            currentCard.cardName = current.cardName;
            currentCard.setRarity = current.getStringOfMainRarities();
            currentCard.id = current.id;
            currentCard.setName = current.mainSetName;
            currentCard.multiListSetNames = current.getStringOfSetNames();
            currentCard.quantity = current.quantity;
            currentCard.setNumber = current.getStringOfMainSetNumbers();
            currentCard.priceBought = current.getAveragePrice();
            currentCard.setCode = current.mainSetCode;
            currentCard.mainSetCardSets = current.mainSetCardSets;
            newList.add(currentCard);
        }

        sortData(newList, currentComparator);

        if(newList.size() > 0){
            isCardNameMode = false;
        }

        return newList;
    }
    
    public ArrayList<OwnedCard> getFilteredList(ArrayList<OwnedCard> inputList, String filter){

        ArrayList<OwnedCard> newList = new ArrayList<>();
        
        for(OwnedCard current: inputList){
            if(filter == null ||filter.equals("") || current.cardName.toUpperCase().contains(filter.toUpperCase())){
                newList.add(current);
            }
        }
        
        return newList;
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

    public boolean isCardNameMode() {
        return isCardNameMode;
    }

    public void setCardsList(ArrayList<OwnedCard> cardsList) {
        this.cardsList = cardsList;
    }

    public void setFilteredCardsList(ArrayList<OwnedCard> filteredCardsList) {
        this.filteredCardsList = filteredCardsList;
    }
}