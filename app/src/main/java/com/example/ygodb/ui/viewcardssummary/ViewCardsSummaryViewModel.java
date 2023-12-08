package com.example.ygodb.ui.viewcardssummary;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.MenuItemBean;
import com.example.ygodb.model.ViewCardsLoadPartialScrollViewModel;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewCardsSummaryViewModel extends ViewCardsLoadPartialScrollViewModel<OwnedCard> {

	public ViewCardsSummaryViewModel() {
		super();
	}

	protected Map<Integer, MenuItemBean> createMenuMap(){

		Map<Integer, MenuItemBean> menuItemMap = new HashMap<>();

		menuItemMap.put(0, new MenuItemBean(
				0,
				"Date Bought",
				"maxDate desc, cardName asc",
				"maxDate asc, cardName asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc, dateBought desc",
				"cardName asc, dateBought desc",
				true));
		menuItemMap.put(2, new MenuItemBean(
				2,
				"Quantity",
				"totalQuantity desc, cardName asc",
				"totalQuantity asc, cardName asc",
				false));
		menuItemMap.put(3, new MenuItemBean(
				3,
				"Price",
				"avgPrice desc, cardName asc",
				"avgPrice asc, cardName asc",
				false));

		return menuItemMap;
	}

	public List<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch) {
		return AndroidUtil.getDBInstance().queryOwnedCardsGrouped(orderBy, limit, offset, cardNameSearch);
	}
}