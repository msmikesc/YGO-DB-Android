package com.example.ygodb.ui.viewcardssummary;

import com.example.ygodb.model.partialscroll.ViewCardsLoadPartialScrollViewModel;
import com.example.ygodb.model.popupsortmenu.MenuItemBean;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;

import java.util.Collection;
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
				"maxDate desc, maxModificationDate desc, cardName asc",
				"maxDate asc, maxModificationDate asc, cardName asc",
				false));
		menuItemMap.put(1, new MenuItemBean(
				1,
				"Card Name",
				"cardName desc",
				"cardName asc",
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

	public List<OwnedCard> loadMoreData(String orderBy, int limit, int offset, String cardNameSearch, String rarityFilter) {
		return AndroidUtil.getDBInstance().queryOwnedCardsGrouped(orderBy, limit, offset, cardNameSearch);
	}

	@Override
	public void updateFilterStateFromRarityCollection(Collection<String> raritySet) {
		//Filters not supported
	}
}