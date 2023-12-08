package com.example.ygodb.ui.viewcardset;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemComparatorBean;
import com.example.ygodb.abs.OwnedCardNameComparatorAsc;
import com.example.ygodb.abs.OwnedCardNameComparatorDesc;
import com.example.ygodb.abs.OwnedCardPriceComparatorAsc;
import com.example.ygodb.abs.OwnedCardPriceComparatorDesc;
import com.example.ygodb.abs.OwnedCardQuantityComparatorAsc;
import com.example.ygodb.abs.OwnedCardQuantityComparatorDesc;
import com.example.ygodb.abs.OwnedCardSetNumberComparatorAsc;
import com.example.ygodb.abs.OwnedCardSetNumberComparatorDesc;
import com.example.ygodb.model.ViewCardsLoadCompleteDataViewModel;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCardSetViewModel extends ViewCardsLoadCompleteDataViewModel {

	public ViewCardSetViewModel() {
		super();
	}

	protected Map<Integer, MenuItemComparatorBean> createMenuMap(){

		Map<Integer, MenuItemComparatorBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemComparatorBean(
				0,
				"Quantity",
				new OwnedCardQuantityComparatorAsc(),
				new OwnedCardQuantityComparatorDesc(),
				true
		));
		menuItemMap.put(1, new MenuItemComparatorBean(
				1,
				"Card Name",
				new OwnedCardNameComparatorAsc(),
				new OwnedCardNameComparatorDesc(),
				true
		));
		menuItemMap.put(2, new MenuItemComparatorBean(
				2,
				"Set Number",
				new OwnedCardSetNumberComparatorAsc(),
				new OwnedCardSetNumberComparatorDesc(),
				true
		));
		menuItemMap.put(3, new MenuItemComparatorBean(
				3,
				"Price",
				new OwnedCardPriceComparatorAsc(),
				new OwnedCardPriceComparatorDesc(),
				false
		));

		return menuItemMap;
	}

	public List<OwnedCard> getInitialData(String setName) {

		if (setName == null || setName.isBlank() || setName.trim().length() < 3) {
			isCardNameMode = true;
			return getInitialCardNameData(cardNameSearch);
		}

		setName = setName.trim();

		List<OwnedCard> newList = AndroidUtil.getDBInstance().getAllPossibleCardsBySetName(setName, null);

		if (newList.isEmpty()) {
			newList = AndroidUtil.getDBInstance().getAllPossibleCardsByArchetype(setName, null);
		}

		if (!newList.isEmpty()) {
			isCardNameMode = false;
		}

		return newList;
	}

}