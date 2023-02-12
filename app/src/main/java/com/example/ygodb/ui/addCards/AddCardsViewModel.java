package com.example.ygodb.ui.addCards;

import androidx.lifecycle.ViewModel;

import com.example.ygodb.backend.bean.OwnedCard;
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

    public ArrayList<OwnedCard> getCardsList(){
        return cardsList;
    }

    public void addNewFromOwnedCard(OwnedCard current){

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
            newCard.priceBought = "0.10";
            newCard.setName = current.setName;
            newCard.quantity = 1;
            newCard.rarityUnsure= 0;
            newCard.setCode = current.setCode;
            newCard.folderName = "UnSynced Folder";
            newCard.setNumber = current.setNumber;
            newCard.colorVariant = "-1";

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