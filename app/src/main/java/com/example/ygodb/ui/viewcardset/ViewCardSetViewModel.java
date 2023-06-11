package com.example.ygodb.ui.viewcardset;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.OwnedCardQuantityComparator;

import ygodb.commonlibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.bean.OwnedCard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ViewCardSetViewModel extends ViewModel {

    private List<OwnedCard> cardsList;
    private List<OwnedCard> filteredCardsList;

    private Comparator<OwnedCard> currentComparator = null;
    private String sortOption = null;
    private String cardNameSearch = null;
    private String setNameSearch = null;

    private boolean isCardNameMode = true;

    private final List<String> setNamesDropdownList = new ArrayList<>();

    public ViewCardSetViewModel() {
        currentComparator = new OwnedCardQuantityComparator();
        sortOption = "Quantity";
        cardsList = new ArrayList<>();
        filteredCardsList = new ArrayList<>();
        isCardNameMode = true;
    }

    private final MutableLiveData<Boolean> dbRefreshIndicator = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> getDbRefreshIndicator() {
        return dbRefreshIndicator;
    }

    public void setDbRefreshIndicatorFalse() {
        this.dbRefreshIndicator.setValue(false);
    }

    public void refreshViewDBUpdate() {
        if(!isCardNameMode) {
            List<OwnedCard> results = getInitialData(setNameSearch);
            cardsList.clear();
            cardsList.addAll(results);

            List<OwnedCard> filteredResults = getFilteredList(results, cardNameSearch);
            filteredCardsList.clear();
            filteredCardsList.addAll(filteredResults);
        }
        else{
            List<OwnedCard> results = getInitialCardNameData(cardNameSearch);
            filteredCardsList.clear();
            filteredCardsList.addAll(results);
        }

        this.dbRefreshIndicator.postValue(true);
    }

    public List<OwnedCard> getInitialCardNameData(String cardName) {

        ArrayList<OwnedCard> results = null;

        if(cardName == null || cardName.trim().equals("") || cardName.trim().length() < 3){
            return new ArrayList<>();
        }

        results = AndroidUtil.getDBInstance().getAllPossibleCardsByNameSearch(cardName,
                "a.cardName asc, a.setNumber asc, a.setRarity asc");

        if(!results.isEmpty()){
            isCardNameMode = true;
        }
        sortOption = "Default";

        return results;
    }

    public List<OwnedCard> getInitialData(String setName) {

        AnalyzeCardsInSet runner = new AnalyzeCardsInSet();

        List<AnalyzeData> results = null;
        ArrayList<OwnedCard> newList = new ArrayList<>();

        if(setName == null || setName.equals("") || setName.trim().length() < 4){
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
            currentCard.gamePlayCardUUID = current.gamePlayCardUUID;
            currentCard.setName = current.mainSetName;
            currentCard.multiListSetNames = current.getStringOfSetNames();
            currentCard.quantity = current.quantity;
            currentCard.setNumber = current.getStringOfMainSetNumbers();
            currentCard.priceBought = current.getAveragePrice();
            currentCard.setCode = current.mainSetCode;
            currentCard.mainSetCardSets = current.mainSetCardSets;
            currentCard.passcode = current.passcode;
            newList.add(currentCard);
        }

        sortData(newList, currentComparator);

        if(!newList.isEmpty()){
            isCardNameMode = false;
        }

        return newList;
    }
    
    public List<OwnedCard> getFilteredList(List<OwnedCard> inputList, String filter){

        ArrayList<OwnedCard> newList = new ArrayList<>();
        
        for(OwnedCard current: inputList){
            if(filter == null ||filter.equals("") || current.cardName.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))){
                newList.add(current);
            }
        }
        
        return newList;
    }

    public void sortData(List<OwnedCard> cardsList, Comparator<OwnedCard> currentComparator){
        cardsList.sort(currentComparator);
    }

    public List<OwnedCard> getCardsList() {
        return cardsList;
    }

    public List<OwnedCard> getFilteredCardsList() {
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

    public List<String> getSetNamesDropdownList() {
        return setNamesDropdownList;
    }

    public void updateSetNamesDropdownList(List<String> input) {
        setNamesDropdownList.clear();
        setNamesDropdownList.addAll(input);
    }

    public Comparator<OwnedCard> getCurrentComparator() {
        return currentComparator;
    }

    public void setCurrentComparator(Comparator<OwnedCard> currentComparator) {
        this.currentComparator = currentComparator;
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
}