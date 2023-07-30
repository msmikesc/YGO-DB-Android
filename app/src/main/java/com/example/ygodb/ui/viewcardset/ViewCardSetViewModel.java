package com.example.ygodb.ui.viewcardset;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.OwnedCardSetNumberComparator;
import ygodb.commonlibrary.bean.OwnedCard;

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
        currentComparator = new OwnedCardSetNumberComparator();
        sortOption = "Set Number";
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

        if(cardName == null || cardName.isBlank() || cardName.trim().length() < 3){
            return new ArrayList<>();
        }

        List<OwnedCard> results = AndroidUtil.getDBInstance().getAllPossibleCardsByNameSearch(cardName,
                null);

        if(!results.isEmpty()){
            isCardNameMode = true;
        }

        return results;
    }

    public List<OwnedCard> getInitialData(String setName) {

        if(setName == null || setName.equals("") || setName.trim().length() < 3) {
            isCardNameMode = true;
            return getInitialCardNameData(cardNameSearch);
        }

        setName = setName.trim();

        List<OwnedCard> newList = AndroidUtil.getDBInstance().getAllPossibleCardsBySetName(setName,
                null);

        if(newList.isEmpty()){
            newList = AndroidUtil.getDBInstance().getAllPossibleCardsByArchetype(setName,
                    null);
        }

        if(!newList.isEmpty()){
            isCardNameMode = false;
        }

        return newList;
    }
    
    public List<OwnedCard> getFilteredList(List<OwnedCard> inputList, String filter){

        ArrayList<OwnedCard> newList = new ArrayList<>();
        
        for(OwnedCard current: inputList){
            if(filter == null ||filter.equals("") || current.getCardName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))){
                newList.add(current);
            }
        }
        
        return newList;
    }

    public void sortData(List<OwnedCard> cardsList, Comparator<OwnedCard> cardComparator){
        cardsList.sort(cardComparator);
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