package com.example.ygodb.ui.sellCards;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.Rarity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class SellCardsViewModel extends ViewModel {

    private HashMap<String,Integer> keyToPosition;
    private ArrayList<OwnedCard> cardsList;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public SellCardsViewModel() {
        cardsList = new ArrayList<>();
        keyToPosition = new HashMap<>();
    }


    public void saveToDB(){
        for(OwnedCard current: cardsList){

            AndroidUtil.getDBInstance().sellCards(current, current.sellQuantity, current.priceSold);

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

        String key = current.UUID;

        Integer position = keyToPosition.get(key);

        OwnedCard sellingCard = null;

        if(position != null){
            sellingCard = cardsList.get(position);
        }
        if(sellingCard != null){

            if(sellingCard.quantity > sellingCard.sellQuantity){
                sellingCard.sellQuantity++;
            }
        }
        else{
            sellingCard = new OwnedCard();
            keyToPosition.put(key, cardsList.size());
            cardsList.add(sellingCard);
            sellingCard.cardName = current.cardName;
            sellingCard.dateBought = current.dateBought;
            sellingCard.dateSold = sdf.format(new Date());
            sellingCard.gamePlayCardUUID = current.gamePlayCardUUID;
            sellingCard.setRarity = current.setRarity;
            sellingCard.setName = current.setName;
            sellingCard.quantity = current.quantity;
            sellingCard.sellQuantity = 1;
            sellingCard.rarityUnsure= 0;
            sellingCard.setCode = current.setCode;
            sellingCard.setNumber = current.setNumber;
            sellingCard.colorVariant = current.colorVariant;
            sellingCard.UUID = current.UUID;
            sellingCard.passcode = current.passcode;

            sellingCard.priceBought = current.priceBought;
            sellingCard.priceSold = getAPIPriceFromRarity(current.setRarity,
                    current.mainSetCardSets, current.cardName, current.setName,
                    current.gamePlayCardUUID, current.setNumber);

            sellingCard.creationDate = current.creationDate;

            sellingCard.mainSetCardSets = current.mainSetCardSets;

            if(current.condition == null || current.condition.equals("")){
                sellingCard.condition = "NearMint";
            }
            else{
                sellingCard.condition = current.condition;
            }

            if(current.editionPrinting == null || current.editionPrinting.equals("")){
                //assume unlimited
                sellingCard.editionPrinting = "Unlimited";
            }
            else{
                sellingCard.editionPrinting = current.editionPrinting;
            }

        }

    }

    public void setAllPricesEstimate(){
        for(OwnedCard current: cardsList){

            String rarity = (current.dropdownSelectedRarity == null) ? current.setRarity: current.dropdownSelectedRarity;

            current.priceSold = getEstimatePriceFromRarity(rarity);
        }
    }

    public void setAllPricesAPI(){
        for(OwnedCard current: cardsList){

            String rarity = (current.dropdownSelectedRarity == null) ? current.setRarity: current.dropdownSelectedRarity;

            String setNumber = (current.dropdownSelectedSetNumber == null) ? current.setNumber: current.dropdownSelectedSetNumber;

            current.priceSold = getAPIPriceFromRarity(rarity, current.mainSetCardSets,
                    current.cardName, current.setName, current.gamePlayCardUUID, setNumber);
        }
    }

    public void setAllPricesZero(){
        for(OwnedCard current: cardsList){
            current.priceSold = "0.00";
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

        return "1.00";

    }

    public String getAPIPriceFromRarity(String rarity, ArrayList<CardSet> mainSetCardSets,
                                        String cardName, String setName, String gamePlayCardUUID, String setNumber){

        if(mainSetCardSets == null){
            mainSetCardSets = AndroidUtil.getDBInstance().
                    getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName);
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

        String key = current.UUID;

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