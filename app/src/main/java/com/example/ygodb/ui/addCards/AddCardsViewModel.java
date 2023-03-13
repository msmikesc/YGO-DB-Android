package com.example.ygodb.ui.addCards;

import android.view.View;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.bean.Rarity;
import com.example.ygodb.backend.connection.SQLiteConnection;
import com.example.ygodb.backend.connection.Util;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

public class AddCardsViewModel extends ViewModel {

    private HashMap<String,Integer> keyToPosition;
    private ArrayList<OwnedCard> cardsList;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public AddCardsViewModel() {
        cardsList = new ArrayList<>();
        keyToPosition = new HashMap<>();
    }


    public void saveToDB(){
        for(OwnedCard current: cardsList){
            if(current.dropdownSelectedSetNumber != null && !current.dropdownSelectedSetNumber.equals("")){
                current.setNumber = current.dropdownSelectedSetNumber;
            }
            if(current.dropdownSelectedRarity != null && !current.dropdownSelectedRarity.equals("")){
                current.setRarity = current.dropdownSelectedRarity;
            }

            OwnedCard existingRecord = SQLiteConnection.getObj().getExistingOwnedCardByObject(current);

            if(existingRecord != null){
                existingRecord.quantity += current.quantity;
                current = existingRecord;
            }

            SQLiteConnection.getObj().upsertOwnedCardBatch(current);

        }
        keyToPosition.clear();
        cardsList.clear();
    }


    public ArrayList<OwnedCard> getCardsList(){
        return cardsList;
    }

    public void addNewFromOwnedCard(OwnedCard current){

        if(current.setNumber == null || current.setRarity == null ||
                current.setName == null || current.cardName == null){
            return;
        }

        String key = current.setNumber + current.setRarity;

        Integer position = keyToPosition.get(key);

        OwnedCard newCard = null;

        if(position != null){
            newCard = cardsList.get(position);
        }
        if(newCard != null){
            newCard.quantity++;
        }
        else{
            newCard = new OwnedCard();
            keyToPosition.put(key, cardsList.size());
            cardsList.add(newCard);
            newCard.cardName = current.cardName;
            newCard.dateBought = sdf.format(new Date());
            newCard.id = current.id;
            newCard.setRarity = current.setRarity;
            newCard.setName = current.setName;
            newCard.quantity = 1;
            newCard.rarityUnsure= 0;
            newCard.setCode = current.setCode;
            newCard.folderName = "UnSynced Folder";
            newCard.setNumber = current.setNumber;
            newCard.colorVariant = "-1";
            newCard.mainSetCardSets = current.mainSetCardSets;

            newCard.priceBought = getEstimatePriceFromRarity(current.setRarity);

            if(current.condition == null || current.condition.equals("")){
                newCard.condition = "NearMint";
            }
            else{
                newCard.condition = current.condition;
            }

            if(current.editionPrinting == null || current.editionPrinting.equals("")){
                //assume ots unlimited, everything else 1st
                if(newCard.setName.contains("OTS")){
                    newCard.editionPrinting = "Unlimited";
                }
                else{
                    newCard.editionPrinting = "1st Edition";
                }

            }
            else{
                newCard.editionPrinting = current.editionPrinting;
            }

        }

    }

    public void setAllPricesEstimate(){
        for(OwnedCard current: cardsList){

            String rarity = (current.dropdownSelectedRarity == null) ? current.setRarity: current.dropdownSelectedRarity;

            current.priceBought = getEstimatePriceFromRarity(rarity);
        }
    }

    public void setAllPricesAPI(){
        for(OwnedCard current: cardsList){

            String rarity = (current.dropdownSelectedRarity == null) ? current.setRarity: current.dropdownSelectedRarity;

            String setNumber = (current.dropdownSelectedSetNumber == null) ? current.setNumber: current.dropdownSelectedSetNumber;

            current.priceBought = getAPIPriceFromRarity(rarity, current.mainSetCardSets,
                    current.cardName, current.setName, current.id, setNumber);
        }
    }

    public void setAllPricesZero(){
        for(OwnedCard current: cardsList){
            current.priceBought = "0.00";
        }
    }

    public String getEstimatePriceFromRarity(String rarity){
        String[] rarities = rarity.split(", ");

        String assumedRarity = rarity.trim();

        if(rarities.length > 1){
            assumedRarity = rarities[0].trim();
        }

        if(assumedRarity.equalsIgnoreCase(Rarity.Common.toString())){
            return "0.15";
        }

        if(assumedRarity.equalsIgnoreCase(Rarity.Rare.toString())){
            return "0.15";
        }

        if(assumedRarity.equalsIgnoreCase(Rarity.SuperRare.toString())){
            return "0.25";
        }

        return "0.00";

    }

    public String getAPIPriceFromRarity(String rarity, ArrayList<CardSet> mainSetCardSets,
                                        String cardName, String setName, int id, String setNumber){

        if(mainSetCardSets == null){
            mainSetCardSets = SQLiteConnection.getObj().
                    getRaritiesOfCardInSetByIDAndName(id, setName, cardName);
        }

        if(mainSetCardSets.size() == 1){
            return mainSetCardSets.get(0).setPrice;
        }

        String[] rarities = rarity.split(", ");

        String assumedRarity = rarity.trim();

        if(rarities.length == 1){
            assumedRarity = rarities[0].trim();
        }

        for(int i = 0; i < mainSetCardSets.size(); i++){
            if(mainSetCardSets.get(i).setRarity.equalsIgnoreCase(assumedRarity) &&
                    mainSetCardSets.get(i).setNumber.equalsIgnoreCase(setNumber)){
                return mainSetCardSets.get(i).setPrice;
            }
        }

        return getEstimatePriceFromRarity(assumedRarity);

    }

    public void removeNewFromOwnedCard(OwnedCard current){

        String key = current.setNumber + current.setRarity;

        Integer position = keyToPosition.get(key);
        OwnedCard newCard = null;

        if(position != null){
            newCard = cardsList.get(position);
        }
        if(newCard == null){
           return;
        }

        for(String testKey: keyToPosition.keySet()){

            Integer testPos = keyToPosition.get(testKey);

            if(testPos > position){
                testPos--;
                keyToPosition.put(testKey, testPos);
            }
        }

        keyToPosition.remove(key);
        cardsList.remove(position.intValue());

    }

}