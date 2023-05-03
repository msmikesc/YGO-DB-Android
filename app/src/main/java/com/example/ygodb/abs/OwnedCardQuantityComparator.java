package com.example.ygodb.abs;

import ygodb.commonLibrary.bean.OwnedCard;

import java.util.Comparator;

public class OwnedCardQuantityComparator implements Comparator<OwnedCard> {

    @Override
    public int compare(OwnedCard ownedCard, OwnedCard t1) {

        int val = ownedCard.quantity - t1.quantity;

        if(val!=0){
            return val;
        }

        val = ownedCard.setNumber.compareTo(t1.setNumber);

        if(val!=0){
            return val;
        }

        val = ownedCard.cardName.compareTo(t1.cardName);

        return val;
    }
}
