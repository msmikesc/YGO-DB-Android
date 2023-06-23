package com.example.ygodb.abs;

import ygodb.commonlibrary.bean.OwnedCard;

import java.util.Comparator;

public class OwnedCardQuantityComparator implements Comparator<OwnedCard> {

    @Override
    public int compare(OwnedCard ownedCard, OwnedCard t1) {

        int val = ownedCard.getQuantity() - t1.getQuantity();

        if(val!=0){
            return val;
        }

        val = ownedCard.getSetNumber().compareTo(t1.getSetNumber());

        if(val!=0){
            return val;
        }

        val = ownedCard.getCardName().compareTo(t1.getCardName());

        return val;
    }
}
