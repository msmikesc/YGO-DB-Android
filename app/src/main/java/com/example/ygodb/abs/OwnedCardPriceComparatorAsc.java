package com.example.ygodb.abs;

import ygodb.commonlibrary.bean.OwnedCard;

import java.math.BigDecimal;
import java.util.Comparator;

public class OwnedCardPriceComparatorAsc implements Comparator<OwnedCard> {

	@Override
	public int compare(OwnedCard ownedCard, OwnedCard t1) {

		BigDecimal p1 = ownedCard.getPriceBought() != null ? new BigDecimal(ownedCard.getPriceBought()) : new BigDecimal(0);
		BigDecimal p2 = t1.getPriceBought() != null ? new BigDecimal(t1.getPriceBought()) : new BigDecimal(0);

		int val = p1.compareTo(p2);

		if (val != 0) {
			return val;
		}

		val = ownedCard.getCardName().compareTo(t1.getCardName());

		if (val != 0) {
			return val;
		}

		val = ownedCard.getQuantity() - t1.getQuantity();

		if (val != 0) {
			return val;
		}

		val = ownedCard.getSetRarity().compareTo(t1.getSetRarity());

		return val;
	}
}
