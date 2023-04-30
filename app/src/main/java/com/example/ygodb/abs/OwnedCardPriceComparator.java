package com.example.ygodb.abs;

import bean.OwnedCard;

import java.math.BigDecimal;
import java.util.Comparator;

public class OwnedCardPriceComparator implements Comparator<OwnedCard> {

    @Override
    public int compare(OwnedCard ownedCard, OwnedCard t1) {

        BigDecimal p1 = ownedCard.priceBought != null ? new BigDecimal(ownedCard.priceBought): new BigDecimal(0);
        BigDecimal p2 = t1.priceBought != null ? new BigDecimal(t1.priceBought): new BigDecimal(0);

        int val = p2.compareTo(p1);

        if(val!=0){
            return val;
        }

        val = ownedCard.cardName.compareTo(t1.cardName);

        if(val!=0){
            return val;
        }

        val = ownedCard.quantity - t1.quantity;

        return val;
    }
}
