package com.example.ygodb.ui.addcards;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddCardsViewModel extends ViewModel {

    private final HashMap<String,Integer> keyToPosition;
    private final ArrayList<OwnedCard> cardsList;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public AddCardsViewModel() {
        cardsList = new ArrayList<>();
        keyToPosition = new HashMap<>();
    }


    public void saveToDB(){
        for(OwnedCard current: cardsList){
            if(current.getDropdownSelectedSetNumber() != null && !current.getDropdownSelectedSetNumber().equals("")){
                current.setSetNumber(current.getDropdownSelectedSetNumber());
            }

            if(current.getSetCode() == null || current.getSetCode().equals("")) {
                current.setSetCode(Util.getPrefixFromSetNumber(current.getSetNumber()));
            }

            if(current.getDropdownSelectedRarity() != null && !current.getDropdownSelectedRarity().equals("")){
                current.setSetRarity(current.getDropdownSelectedRarity());
            }

            if(current.getDropdownSelectedSetName() != null && !current.getDropdownSelectedSetName().equals("")){
                current.setSetName(current.getDropdownSelectedSetName());
            }
            else if(current.getSetNamesOptions() != null && current.getSetNamesOptions().size() == 1){
                current.setSetName(current.getSetNamesOptions().get(0));
            }

            if(current.getPriceBought() == null){
                current.setPriceBought(Const.ZERO_PRICE_STRING);
            }

            OwnedCard existingRecord = AndroidUtil.getDBInstance().getExistingOwnedCardByObject(current);

            if(existingRecord != null){
                existingRecord.setQuantity(existingRecord.getQuantity() + current.getQuantity());
                current = existingRecord;
            }

            AndroidUtil.getDBInstance().upsertOwnedCardBatch(current);

        }
        keyToPosition.clear();
        cardsList.clear();
    }

    public void invertAllEditions(){
        for(OwnedCard o : cardsList){
            if(o.getEditionPrinting().equals(Const.CARD_PRINTING_FIRST_EDITION)){
                o.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
            }
            else{
                o.setEditionPrinting(Const.CARD_PRINTING_FIRST_EDITION);
            }
        }
    }

    public List<OwnedCard> getCardsList(){
        return cardsList;
    }

    public void addNewFromOwnedCard(OwnedCard current){

        if(current.getSetNumber() == null || current.getSetRarity() == null ||
                current.getSetName() == null || current.getCardName() == null){
            return;
        }

        String key = current.getSetNumber() + current.getSetRarity();

        if(current.getSetNamesOptions() != null){
            key = current.getSetNumber() + current.getSetRarity() + current.getSetNamesOptions().toString();
        }

        Integer position = keyToPosition.get(key);

        OwnedCard newCard = null;

        if(position != null){
            newCard = cardsList.get(position);
        }
        if(newCard != null){
            newCard.setQuantity(newCard.getQuantity() + 1);
        }
        else{
            newCard = new OwnedCard();
            keyToPosition.put(key, cardsList.size());
            cardsList.add(newCard);
            newCard.setCardName(current.getCardName());
            newCard.setDateBought(sdf.format(new Date()));
            newCard.setGamePlayCardUUID(current.getGamePlayCardUUID());
            newCard.setSetRarity(current.getSetRarity());
            newCard.setSetName(current.getSetName());
            newCard.setQuantity(1);
            newCard.setRarityUnsure(0);
            newCard.setSetCode(current.getSetCode());
            newCard.setFolderName(Const.FOLDER_UNSYNCED);
            newCard.setSetNumber(current.getSetNumber());
            newCard.setColorVariant("-1");
            newCard.setAnalyzeResultsCardSets(current.getAnalyzeResultsCardSets());
            newCard.setPasscode(current.getPasscode());

            newCard.setSetNamesOptions(current.getSetNamesOptions());

            newCard.setPriceBought(getAPIPriceFromRarity(current.getSetRarity(),
                    current.getAnalyzeResultsCardSets(), current.getSetName(),
                    current.getGamePlayCardUUID(), current.getSetNumber()));

            if(current.getCondition() == null || current.getCondition().equals("")){
                newCard.setCondition("NearMint");
            }
            else{
                newCard.setCondition(current.getCondition());
            }

            if(current.getEditionPrinting() == null || current.getEditionPrinting().equals("")){
                //assume ots unlimited, everything else 1st
                if(newCard.getSetName().contains("OTS")){
                    newCard.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
                }
                else{
                    newCard.setEditionPrinting(Const.CARD_PRINTING_FIRST_EDITION);
                }

            }
            else{
                newCard.setEditionPrinting(current.getEditionPrinting());
            }

        }

    }

    public void setAllPricesEstimate(){
        for(OwnedCard current: cardsList){

            String rarity = (current.getDropdownSelectedRarity() == null) ? current.getSetRarity() : current.getDropdownSelectedRarity();

            current.setPriceBought(getEstimatePriceFromRarity(rarity));
        }
    }

    public void setAllPricesAPI(){
        for(OwnedCard current: cardsList){

            String rarity = (current.getDropdownSelectedRarity() == null) ? current.getSetRarity() : current.getDropdownSelectedRarity();

            String setNumber = (current.getDropdownSelectedSetNumber() == null) ? current.getSetNumber() : current.getDropdownSelectedSetNumber();

            String setName = (current.getDropdownSelectedSetName() == null) ? current.getSetName() : current.getDropdownSelectedSetName();

            current.setPriceBought(getAPIPriceFromRarity(rarity, current.getAnalyzeResultsCardSets(),
                    setName, current.getGamePlayCardUUID(), setNumber));
        }
    }

    public void setAllPricesZero(){
        for(OwnedCard current: cardsList){
            current.setPriceBought(Const.ZERO_PRICE_STRING);
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

    public String getAPIPriceFromRarity(String rarity, List<CardSet> analyzeResultsCardSets,
                                        String setName, String gamePlayCardUUID, String setNumber){

        if(analyzeResultsCardSets == null){
            analyzeResultsCardSets = AndroidUtil.getDBInstance().
                    getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName);
        }

        if(analyzeResultsCardSets.size() == 1){
            return analyzeResultsCardSets.get(0).getSetPrice();
        }

        String[] rarities = rarity.split(", ");
        String assumedRarity = rarity.trim();
        if(rarities.length > 1){
            assumedRarity = rarities[0].trim();
        }

        String[] setNumbers = setNumber.split(", ");
        String assumedNumber = setNumber.trim();
        if(setNumbers.length > 1){
            assumedNumber = setNumbers[0].trim();
        }

        String[] setNames = setName.split(", ");
        String assumedSet = setName.trim();
        if(setNames.length > 1){
            assumedSet = setNames[0].trim();
        }

        for (CardSet cardSet : analyzeResultsCardSets) {
            if (cardSet.getSetRarity().equalsIgnoreCase(assumedRarity) &&
                    cardSet.getSetNumber().equalsIgnoreCase(assumedNumber) &&
                    cardSet.getSetName().equalsIgnoreCase(assumedSet)) {
                return cardSet.getSetPrice();
            }
        }

        return getEstimatePriceFromRarity(assumedRarity);
    }

    public void removeNewFromOwnedCard(OwnedCard current){

        String key = current.getSetNumber() + current.getSetRarity();

        if(current.getSetNamesOptions() != null){
            key = current.getSetNumber() + current.getSetRarity() + current.getSetNamesOptions().toString();
        }

        Integer position = keyToPosition.get(key);
        OwnedCard newCard = null;

        if(position != null){
            newCard = cardsList.get(position);
        }
        if(newCard == null){
           return;
        }

        for(Map.Entry<String, Integer> testEntry: keyToPosition.entrySet()){

            String testKey = testEntry.getKey();

            Integer testPos = testEntry.getValue();

            if(testPos > position){
                testPos--;
                keyToPosition.put(testKey, testPos);
            }
        }

        keyToPosition.remove(key);
        cardsList.remove(position.intValue());

    }

}