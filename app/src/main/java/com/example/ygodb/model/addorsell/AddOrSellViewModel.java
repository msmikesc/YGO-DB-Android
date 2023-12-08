package com.example.ygodb.model.addorsell;

import androidx.lifecycle.ViewModel;
import com.example.ygodb.util.AndroidUtil;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class AddOrSellViewModel<T extends OwnedCard> extends ViewModel {

	protected final HashMap<String, Integer> keyToPosition;
	protected final ArrayList<T> cardsList;
	protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	protected AddOrSellViewModel() {
		cardsList = new ArrayList<>();
		keyToPosition = new HashMap<>();
	}

	protected abstract String getKeyForOwnedCard(OwnedCard input);

	public abstract void saveToDB() throws SQLException;

	public abstract void addNewFromOwnedCard(OwnedCard current, int quantity);

	public List<T> getCardsList() {
		return cardsList;
	}

	public void setAllPricesEstimate() {
		for (T current : cardsList) {
			current.setPriceBought(Util.getEstimatePriceFromRarity(current.getSetRarity()));
		}
	}

	public void setAllPricesAPI() {
		for (T current : cardsList) {
			current.setPriceBought(Util.getAPIPriceFromRarity(current, AndroidUtil.getDBInstance()));
		}
	}

	public void setAllPricesZero() {
		for (T current : cardsList) {
			current.setPriceBought(Const.ZERO_PRICE_STRING);
		}
	}

	public void removeNewFromOwnedCard(OwnedCard current) {

		String key = getKeyForOwnedCard(current);

		Integer position = keyToPosition.get(key);
		OwnedCard newCard = null;

		if (position != null) {
			newCard = cardsList.get(position);
		}
		if (newCard == null) {
			return;
		}

		for (Map.Entry<String, Integer> testEntry : keyToPosition.entrySet()) {

			String testKey = testEntry.getKey();

			Integer testPos = testEntry.getValue();

			if (testPos > position) {
				testPos--;
				keyToPosition.put(testKey, testPos);
			}
		}

		keyToPosition.remove(key);
		cardsList.remove(position.intValue());

	}

}