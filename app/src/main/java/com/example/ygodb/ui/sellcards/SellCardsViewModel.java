package com.example.ygodb.ui.sellcards;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.abs.AndroidUtil;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.Rarity;
import ygodb.commonlibrary.constant.Const;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SellCardsViewModel extends ViewModel {

    private final HashMap<String,Integer> keyToPosition;
    private final ArrayList<OwnedCard> cardsList;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public SellCardsViewModel() {
        cardsList = new ArrayList<>();
        keyToPosition = new HashMap<>();
    }


    public void saveToDB(){
        for(OwnedCard current: cardsList){

            AndroidUtil.getDBInstance().sellCards(current, current.getSellQuantity(), current.getPriceSold());

        }
        keyToPosition.clear();
        cardsList.clear();
    }


    public List<OwnedCard> getCardsList(){
        return cardsList;
    }

    public void addNewFromOwnedCard(OwnedCard current){

        //TODO move around multiple buttons to add different amounts
        //TODO simplify addcards set names models

        if(current.getSetNumber() == null || current.getSetRarity() == null ||
                current.getSetName() == null || current.getCardName() == null){
            return;
        }

        String key = current.getUuid();

        Integer position = keyToPosition.get(key);

        OwnedCard sellingCard = null;

        if(position != null){
            sellingCard = cardsList.get(position);
        }
        if(sellingCard != null){

            if(sellingCard.getQuantity() > sellingCard.getSellQuantity()){
                sellingCard.setSellQuantity(sellingCard.getSellQuantity() + 1);
            }
        }
        else{
            sellingCard = new OwnedCard();
            keyToPosition.put(key, cardsList.size());
            cardsList.add(sellingCard);
            sellingCard.setCardName(current.getCardName());
            sellingCard.setDateBought(current.getDateBought());
            sellingCard.setDateSold(sdf.format(new Date()));
            sellingCard.setGamePlayCardUUID(current.getGamePlayCardUUID());
            sellingCard.setSetRarity(current.getSetRarity());
            sellingCard.setSetName(current.getSetName());
            sellingCard.setQuantity(current.getQuantity());
            sellingCard.setSellQuantity(1);
            sellingCard.setRarityUnsure(0);
            sellingCard.setSetCode(current.getSetCode());
            sellingCard.setSetNumber(current.getSetNumber());
            sellingCard.setColorVariant(current.getColorVariant());
            sellingCard.setUuid(current.getUuid());
            sellingCard.setPasscode(current.getPasscode());

            sellingCard.setPriceBought(current.getPriceBought());

            if(current.getEditionPrinting() == null || current.getEditionPrinting().equals("")){
                //assume unlimited
                sellingCard.setEditionPrinting(Const.CARD_PRINTING_UNLIMITED);
            }
            else{
                sellingCard.setEditionPrinting(current.getEditionPrinting());
            }

            boolean isFirstEdition = sellingCard.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);

            sellingCard.setAnalyzeResultsCardSets(current.getAnalyzeResultsCardSets());

            sellingCard.setPriceSold(getAPIPriceFromRarity(sellingCard.getSetRarity(),
                    sellingCard.getAnalyzeResultsCardSets(), sellingCard.getSetName(),
                    sellingCard.getGamePlayCardUUID(), sellingCard.getSetNumber(), isFirstEdition));

            sellingCard.setCreationDate(current.getCreationDate());

            if(current.getCondition() == null || current.getCondition().equals("")){
                sellingCard.setCondition("NearMint");
            }
            else{
                sellingCard.setCondition(current.getCondition());
            }
        }
    }

    public void setAllPricesEstimate(){
        for(OwnedCard current: cardsList){

            String rarity = (current.getDropdownSelectedRarity() == null) ? current.getSetRarity() : current.getDropdownSelectedRarity();

            current.setPriceSold(getEstimatePriceFromRarity(rarity));
        }
    }

    public void setAllPricesAPI(){
        for(OwnedCard current: cardsList){

            String rarity = (current.getDropdownSelectedRarity() == null) ? current.getSetRarity() : current.getDropdownSelectedRarity();

            String setNumber = (current.getDropdownSelectedSetNumber() == null) ? current.getSetNumber() : current.getDropdownSelectedSetNumber();

            boolean isFirstEdition = current.getEditionPrinting().contains(Const.CARD_PRINTING_CONTAINS_FIRST);

            current.setPriceSold(getAPIPriceFromRarity(rarity, current.getAnalyzeResultsCardSets(),
                    current.getSetName(), current.getGamePlayCardUUID(), setNumber, isFirstEdition));
        }
    }

    public void setAllPricesZero(){
        for(OwnedCard current: cardsList){
            current.setPriceSold(Const.ZERO_PRICE_STRING);
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
                                        String setName, String gamePlayCardUUID, String setNumber, boolean isFirstEdition){

        if(analyzeResultsCardSets == null){
            analyzeResultsCardSets = AndroidUtil.getDBInstance().
                    getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName);
        }

        if(analyzeResultsCardSets.size() == 1){
            return analyzeResultsCardSets.get(0).getBestExistingPrice(isFirstEdition);
        }

        for (CardSet cardSet : analyzeResultsCardSets) {
            if (cardSet.getSetRarity().equalsIgnoreCase(rarity) &&
                    cardSet.getSetNumber().equalsIgnoreCase(setNumber)) {
                return cardSet.getBestExistingPrice(isFirstEdition);
            }
        }

        return getEstimatePriceFromRarity(rarity);

    }

    public void removeNewFromOwnedCard(OwnedCard current){

        String key = current.getUuid();

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