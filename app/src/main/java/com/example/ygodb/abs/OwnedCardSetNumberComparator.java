package com.example.ygodb.abs;

import com.example.ygodb.backend.bean.OwnedCard;

import java.util.Comparator;

public class OwnedCardSetNumberComparator implements Comparator<OwnedCard> {

    @Override
    public int compare(OwnedCard ownedCard, OwnedCard t1) {

        int val = ownedCard.setNumber.compareTo(t1.setNumber);

        if(val!=0){
            return val;
        }

        val = ownedCard.quantity - t1.quantity;

        if(val!=0){
            return val;
        }

        val = ownedCard.cardName.compareTo(t1.cardName);

        return val;
    }
}
