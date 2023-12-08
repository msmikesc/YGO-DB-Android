package com.example.ygodb.ui.viewcards;

import com.example.ygodb.util.AndroidUtil;
import com.example.ygodb.popupmenu.MenuItemBean;
import com.example.ygodb.model.ViewCardsLoadPartialScrollViewModel;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCardsViewModel extends ViewCardsLoadPartialScrollViewModel<OwnedCard> {

	public ViewCardsViewModel() {
		super();
	}

	protected Map<Integer, MenuItemBean> createMenuMap(){

		Map<Integer, MenuItemBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemBean(
				0,
				"Date Bought",
				"dateBought desc, modificationDate desc",
				"dateBought asc, modificationDate asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc, dateBought desc",
				"cardName asc, dateBought desc",
				true));
		menuItemMap.put(2, new MenuItemBean(
				2,
				"Set Number",
				"setName desc, setNumber desc",
				"setName asc, setNumber asc",
				true));
		menuItemMap.put(3, new MenuItemBean(
				3,
				"Price",
				"priceBought desc, cardName asc",
				"priceBought asc, cardName asc",
				false));

		return menuItemMap;
	}

	public List<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
		return AndroidUtil.getDBInstance().queryOwnedCards(orderBy, limit, offset, cardNameSearch);
	}
}