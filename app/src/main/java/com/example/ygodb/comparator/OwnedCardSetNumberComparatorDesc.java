package com.example.ygodb.comparator;

import ygodb.commonlibrary.bean.OwnedCard;

import java.util.Comparator;

public class OwnedCardSetNumberComparatorDesc implements Comparator<OwnedCard> {

	@Override
	public int compare(OwnedCard ownedCard, OwnedCard t1) {

		int val = t1.getSetNumber().compareTo(ownedCard.getSetNumber());

		if (val != 0) {
			return val;
		}

		val = ownedCard.getQuantity() - t1.getQuantity();

		if (val != 0) {
			return val;
		}

		val = ownedCard.getCardName().compareTo(t1.getCardName());

		if (val != 0) {
			return val;
		}

		val = ownedCard.getSetRarity().compareTo(t1.getSetRarity());

		return val;
	}
}
