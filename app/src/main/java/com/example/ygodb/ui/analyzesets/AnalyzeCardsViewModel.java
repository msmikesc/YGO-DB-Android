package com.example.ygodb.ui.analyzesets;

import com.example.ygodb.comparator.OwnedCardNameComparatorAsc;
import com.example.ygodb.comparator.OwnedCardNameComparatorDesc;
import com.example.ygodb.comparator.OwnedCardPriceComparatorAsc;
import com.example.ygodb.comparator.OwnedCardPriceComparatorDesc;
import com.example.ygodb.comparator.OwnedCardQuantityComparatorAsc;
import com.example.ygodb.comparator.OwnedCardQuantityComparatorDesc;
import com.example.ygodb.comparator.OwnedCardSetNumberComparatorAsc;
import com.example.ygodb.comparator.OwnedCardSetNumberComparatorDesc;
import com.example.ygodb.model.completedata.ViewCardsLoadCompleteDataViewModel;
import com.example.ygodb.model.popupsortmenu.MenuItemComparatorBean;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.bean.OwnedCard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeCardsViewModel extends ViewCardsLoadCompleteDataViewModel {

	public AnalyzeCardsViewModel() {
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

	@Override
	public List<OwnedCard> getInitialData(String setName) {

		AnalyzeCardsInSet runner = new AnalyzeCardsInSet();

		List<AnalyzeData> results = null;
		ArrayList<OwnedCard> newList = new ArrayList<>();

		if (setName == null || setName.isBlank() || setName.trim().length() < 3) {
			isCardNameMode = true;
			return getInitialCardNameData(cardNameSearch);
		}

		try {
			results = runner.runFor(setName, AndroidUtil.getDBInstance());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		for (AnalyzeData current : results) {
			OwnedCard currentCard = new OwnedCard();
			currentCard.setCardName(current.getCardName());
			currentCard.setGamePlayCardUUID(current.getGamePlayCardUUID());
			currentCard.setQuantity(current.getQuantity());
			currentCard.setPriceBought(current.getDisplaySummaryPrice());
			currentCard.setPasscode(current.getPasscode());

			currentCard.setAnalyzeResultsCardSets(current.getCardSets());
			currentCard.setSetRarity(current.getStringOfRarities());
			currentCard.setSetName(current.getStringOfSetNames());
			currentCard.setSetNumber(current.getStringOfSetNumbers());

			currentCard.setSetName(current.getStringOfSetNames());

			newList.add(currentCard);
		}

		if (!newList.isEmpty()) {
			isCardNameMode = false;
		}

		return newList;
	}

}