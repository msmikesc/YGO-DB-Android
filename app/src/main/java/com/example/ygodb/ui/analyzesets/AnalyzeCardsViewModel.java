package com.example.ygodb.ui.analyzesets;

import com.example.ygodb.abs.AndroidUtil;
import com.example.ygodb.abs.OwnedCardQuantityComparator;
import com.example.ygodb.ui.viewcardset.ViewCardSetViewModel;
import ygodb.commonlibrary.analyze.AnalyzeCardsInSet;
import ygodb.commonlibrary.bean.AnalyzeData;
import ygodb.commonlibrary.bean.OwnedCard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeCardsViewModel extends ViewCardSetViewModel {

	public AnalyzeCardsViewModel() {
		currentComparator = new OwnedCardQuantityComparator();
		sortOption = "Quantity";
		cardsList = new ArrayList<>();
		filteredCardsList = new ArrayList<>();
		isCardNameMode = true;
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

		sortData(newList, currentComparator);

		if (!newList.isEmpty()) {
			isCardNameMode = false;
		}

		return newList;
	}

}