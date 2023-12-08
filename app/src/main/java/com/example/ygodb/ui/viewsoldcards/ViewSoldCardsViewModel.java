package com.example.ygodb.ui.viewsoldcards;

import com.example.ygodb.util.AndroidUtil;
import com.example.ygodb.popupmenu.MenuItemBean;
import com.example.ygodb.model.partialscroll.ViewCardsLoadPartialScrollViewModel;
import ygodb.commonlibrary.bean.SoldCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewSoldCardsViewModel extends ViewCardsLoadPartialScrollViewModel<SoldCard> {

	public ViewSoldCardsViewModel() {
		super();
	}

	protected Map<Integer, MenuItemBean> createMenuMap(){

		Map<Integer, MenuItemBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemBean(
				0,
				"Date Sold",
				"dateSold desc, modificationDate desc",
				"dateSold asc, modificationDate asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc, dateSold desc",
				"cardName asc, dateSold desc",
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
				"priceSold desc, cardName asc",
				"priceSold asc, cardName asc",
				false));

		return menuItemMap;
	}

	public List<SoldCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
		return AndroidUtil.getDBInstance().querySoldCards(orderBy, limit, offset, cardNameSearch);
	}
}