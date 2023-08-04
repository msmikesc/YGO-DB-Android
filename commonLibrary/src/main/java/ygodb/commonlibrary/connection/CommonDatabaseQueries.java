package ygodb.commonlibrary.connection;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.constant.SQLConst;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class CommonDatabaseQueries {

	private CommonDatabaseQueries() {
	}

	public static <R> List<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID, DatabaseSelectQuery<CardSet, R> query,
			SelectQueryResultMapper<CardSet, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);

		return query.executeQuery(mapper);
	}

	public static <R> List<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName,
			DatabaseSelectQuery<CardSet, R> query, SelectQueryResultMapper<CardSet, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);
		query.bindString(2, setName);

		return query.executeQuery(mapper);
	}

	public static <R> String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);

		List<String> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return null;
	}

	public static <R> List<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);

		return query.executeQuery(mapper);
	}

	public static <R> String getGamePlayCardUUIDFromTitle(String title, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_TITLE);

		query.bindString(1, title);

		List<String> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return null;
	}

	public static <R> String getGamePlayCardUUIDFromPasscode(int passcode, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE);

		query.bindInteger(1, passcode);

		List<String> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return null;
	}

	public static <R> List<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String gamePlayCardUUID,
			DatabaseSelectQuery<OwnedCard, R> query, SelectQueryResultMapper<OwnedCard, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_ANALYZE_DATA_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);

		return query.executeQuery(mapper);
	}

	public static <R> List<String> getDistinctGamePlayCardUUIDsInSetByName(String setName, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME);

		query.bindString(1, setName);

		return query.executeQuery(mapper);
	}

	public static <R> List<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName, DatabaseSelectQuery<GamePlayCard, R> query,
			SelectQueryResultMapper<GamePlayCard, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_DISTINCT_GAMEPLAYCARDS_IN_SET_BY_NAME);

		query.bindString(1, setName);

		return query.executeQuery(mapper);
	}

	public static <R> List<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype, DatabaseSelectQuery<GamePlayCard, R> query,
			SelectQueryResultMapper<GamePlayCard, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_DISTINCT_GAMEPLAYCARDS_BY_ARCHETYPE);

		query.bindString(1, archetype);
		query.bindString(2, "%" + archetype + "%");

		return query.executeQuery(mapper);
	}

	public static <R> List<String> getSortedSetNumbersInSetByName(String setName, DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_SORTED_SET_NUMBERS_IN_SET_BY_NAME);

		query.bindString(1, setName);

		List<String> resultsFound = query.executeQuery(mapper);

		Collections.sort(resultsFound);

		return resultsFound;
	}

	public static <R> List<String> getDistinctSetNames(DatabaseSelectQuery<String, R> query, SelectQueryResultMapper<String, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_DISTINCT_SET_NAMES);

		return query.executeQuery(mapper);
	}

	public static <R> List<String> getDistinctSetAndArchetypeNames(DatabaseSelectQuery<String, R> query,
			SelectQueryResultMapper<String, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_DISTINCT_SET_AND_ARCHETYPE_NAMES);

		return query.executeQuery(mapper);
	}

	public static <R> Integer getCountDistinctCardsInSet(DatabaseSelectQuery<Integer, R> query, SelectQueryResultMapper<Integer, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_COUNT_DISTINCT_CARDS_IN_SET);

		List<Integer> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return -1;
	}

	public static <R> Integer getCountQuantity(DatabaseSelectQuery<Integer, R> query, SelectQueryResultMapper<Integer, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_COUNT_QUANTITY);

		List<Integer> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return -1;
	}

	public static <R> Integer getCountQuantityManual(DatabaseSelectQuery<Integer, R> query, SelectQueryResultMapper<Integer, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_COUNT_QUANTITY_MANUAL);

		List<Integer> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return -1;
	}

	public static <R> CardSet getFirstCardSetForCardInSet(String cardName, String setName, DatabaseSelectQuery<CardSet, R> query,
			SelectQueryResultMapper<CardSet, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_FIRST_CARD_SET_FOR_CARD_IN_SET);

		query.bindString(1, setName);
		query.bindString(2, cardName);

		List<CardSet> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return null;
	}

	public static <R> List<SetMetaData> getSetMetaDataFromSetName(String setName, DatabaseSelectQuery<SetMetaData, R> query,
			SelectQueryResultMapper<SetMetaData, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_SET_META_DATA_FROM_SET_NAME);

		query.bindString(1, setName);

		return query.executeQuery(mapper);
	}

	public static <R> List<SetMetaData> getSetMetaDataFromSetCode(String setCode, DatabaseSelectQuery<SetMetaData, R> query,
			SelectQueryResultMapper<SetMetaData, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_SET_META_DATA_FROM_SET_CODE);

		query.bindString(1, setCode);

		return query.executeQuery(mapper);
	}

	public static <R> List<SetMetaData> getAllSetMetaDataFromSetData(DatabaseSelectQuery<SetMetaData, R> query,
			SelectQueryResultMapper<SetMetaData, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_ALL_SET_META_DATA_FROM_SET_DATA);

		return query.executeQuery(mapper);
	}

	public static <R> GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID, DatabaseSelectQuery<GamePlayCard, R> query,
			SelectQueryResultMapper<GamePlayCard, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_GAME_PLAY_CARD_BY_UUID);

		query.bindString(1, gamePlayCardUUID);

		List<GamePlayCard> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			return resultsFound.get(0);
		}

		return null;
	}

	public static <R> Integer getNewLowestPasscode(DatabaseSelectQuery<Integer, R> query, SelectQueryResultMapper<Integer, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_NEW_LOWEST_PASSCODE);

		List<Integer> resultsFound = query.executeQuery(mapper);

		if (resultsFound.size() == 1) {
			int currentLowest = resultsFound.get(0);
			return currentLowest - 1;
		}

		return -1;
	}

	public static <R> List<SetBox> getAllSetBoxes(DatabaseSelectQuery<SetBox, R> query, SelectQueryResultMapper<SetBox, R> mapper)
			throws SQLException {
		query.prepareStatement(SQLConst.GET_ALL_SET_BOXES);

		return query.executeQuery(mapper);
	}

	public static <R> List<SetBox> getSetBoxesByNameOrCode(String searchText, DatabaseSelectQuery<SetBox, R> query,
			SelectQueryResultMapper<SetBox, R> mapper) throws SQLException {
		query.prepareStatement(SQLConst.GET_SET_BOXES_BY_NAME_OR_CODE);

		query.bindString(1, searchText);
		query.bindString(2, "%" + searchText + "%");

		return query.executeQuery(mapper);
	}

	public static int replaceIntoGamePlayCard(DatabaseUpdateQuery query, GamePlayCard input) throws SQLException {

		String gamePlayCard = SQLConst.REPLACE_INTO_GAME_PLAY_CARD;
		query.prepareStatement(gamePlayCard);

		query.bindString(1, input.getGamePlayCardUUID());
		query.bindString(2, input.getCardName());
		query.bindString(3, input.getCardType());
		query.bindInteger(4, input.getPasscode());
		query.bindString(5, input.getDesc());
		query.bindString(6, input.getAttribute());
		query.bindString(7, input.getRace());
		query.bindString(8, input.getLinkVal());
		query.bindString(9, input.getLevel());
		query.bindString(10, input.getScale());
		query.bindString(11, input.getAtk());
		query.bindString(12, input.getDef());
		query.bindString(13, input.getArchetype());

		return query.executeUpdate();
	}

	public static int replaceIntoCardSetMetaData(DatabaseUpdateQuery query, String setName, String setCode, int numOfCards, String tcgDate)
			throws SQLException {

		String gamePlayCard = SQLConst.REPLACE_INTO_CARD_SET_META_DATA;
		query.prepareStatement(gamePlayCard);

		query.bindString(1, setName);
		query.bindString(2, setCode);
		query.bindInteger(3, numOfCards);
		query.bindString(4, tcgDate);

		return query.executeUpdate();
	}

	public static int insertOrUpdateOwnedCardByUUID(DatabaseUpdateQuery query, OwnedCard card) throws SQLException {
		if (card.getUuid() == null || card.getUuid().equals("")) {
			int rowsInserted = insertIntoOwnedCards(query, card);
			if (rowsInserted != 1) {
				YGOLogger.error(rowsInserted + " rows inserted for insert for:" + card);
			}
			return rowsInserted;
		} else {
			int rowsUpdated = updateOwnedCardByUUID(query, card);
			if (rowsUpdated != 1) {
				YGOLogger.error(rowsUpdated + " rows updated for update for:" + card);
			}
			return rowsUpdated;
		}
	}

	public static int updateOwnedCardByUUID(DatabaseUpdateQuery query, OwnedCard card) throws SQLException {
		String gamePlayCardUUID = card.getGamePlayCardUUID();
		String folder = card.getFolderName();
		String name = card.getCardName();
		int quantity = card.getQuantity();
		String setCode = card.getSetCode();
		String condition = card.getCondition();
		String printing = card.getEditionPrinting();
		String priceBought = card.getPriceBought();
		String dateBought = card.getDateBought();
		int rarityUnsure = card.getRarityUnsure();
		String colorVariant = card.getColorVariant();
		String setNumber = card.getSetNumber();
		String setName = card.getSetName();
		String setRarity = card.getSetRarity();
		int passcode = card.getPasscode();
		String uuid = card.getUuid();

		if (uuid == null || uuid.equals("")) {
			YGOLogger.error("UUID null on updated owned card");
			return 0;
		}

		if (rarityUnsure != Const.RARITY_UNSURE_TRUE) {
			rarityUnsure = Const.RARITY_UNSURE_FALSE;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		query.prepareStatement(SQLConst.UPDATE_OWNED_CARD_BY_UUID);

		query.bindString(1, gamePlayCardUUID);
		query.bindString(2, folder);
		query.bindString(3, name);
		query.bindInteger(4, quantity);
		query.bindString(5, setCode);
		query.bindString(6, setNumber);
		query.bindString(7, setName);
		query.bindString(8, setRarity);
		query.bindString(9, colorVariant);
		query.bindString(10, condition);
		query.bindString(11, printing);
		query.bindString(12, dateBought);
		query.bindString(13, normalizedPrice);
		query.bindInteger(14, rarityUnsure);
		query.bindInteger(15, passcode);
		query.bindString(16, uuid);

		return query.executeUpdate();
	}

	public static int insertIntoOwnedCards(DatabaseUpdateQuery query, OwnedCard card) throws SQLException {
		String gamePlayCardUUID = card.getGamePlayCardUUID();
		String folder = card.getFolderName();
		String name = card.getCardName();
		int quantity = card.getQuantity();
		String setCode = card.getSetCode();
		String condition = card.getCondition();
		String printing = card.getEditionPrinting();
		String priceBought = card.getPriceBought();
		String dateBought = card.getDateBought();
		int rarityUnsure = card.getRarityUnsure();
		String colorVariant = card.getColorVariant();
		String setNumber = card.getSetNumber();
		String setName = card.getSetName();
		String setRarity = card.getSetRarity();
		String uuid = card.getUuid();
		int passcode = card.getPasscode();

		if (uuid == null || uuid.equals("")) {
			uuid = java.util.UUID.randomUUID().toString();
		} else {
			YGOLogger.error("UUID not null on an insert owned card:" + uuid);
			return 0;
		}

		if (rarityUnsure != Const.RARITY_UNSURE_TRUE) {
			rarityUnsure = Const.RARITY_UNSURE_FALSE;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		query.prepareStatement(SQLConst.INSERT_OR_IGNORE_INTO_OWNED_CARDS);

		query.bindString(1, gamePlayCardUUID);
		query.bindString(2, folder);
		query.bindString(3, name);
		query.bindInteger(4, quantity);
		query.bindString(5, setCode);
		query.bindString(6, setNumber);
		query.bindString(7, setName);
		query.bindString(8, setRarity);
		query.bindString(9, colorVariant);
		query.bindString(10, condition);
		query.bindString(11, printing);
		query.bindString(12, dateBought);
		query.bindString(13, normalizedPrice);
		query.bindInteger(14, rarityUnsure);
		query.bindString(15, uuid);
		query.bindInteger(16, passcode);

		return query.executeUpdate();
	}

	public static int insertOrIgnoreIntoCardSet(DatabaseUpdateQuery query, String setNumber, String rarity, String setName,
			String gamePlayCardUUID, String cardName, String colorVariant, String url) throws SQLException {

		if (colorVariant == null || colorVariant.isBlank()) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		query.prepareStatement(SQLConst.INSERT_OR_IGNORE_INTO_CARD_SETS);

		query.bindString(1, gamePlayCardUUID);
		query.bindString(2, setNumber);
		query.bindString(3, setName);
		query.bindString(4, rarity);
		query.bindString(5, cardName);
		query.bindString(6, colorVariant);
		query.bindString(7, url);

		return query.executeUpdate();
	}

	public static int updateCardSetPrice(DatabaseUpdateQuery query, String setNumber, String rarity, String price, boolean isFirstEdition)
			throws SQLException {

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_RARITY;

		if (isFirstEdition) {
			update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_RARITY_FIRST;
		}

		query.prepareStatement(update);

		query.bindString(1, price);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);

		return query.executeUpdate();
	}

	public static int updateCardSetPriceWithSetName(DatabaseUpdateQuery query, String setNumber, String rarity, String price,
			String setName, boolean isFirstEdition) throws SQLException {

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME;

		if (isFirstEdition) {
			update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME_FIRST;
		}

		query.prepareStatement(update);

		query.bindString(1, price);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);
		query.bindString(4, setName);

		return query.executeUpdate();
	}

	public static int updateCardSetPriceWithCardAndSetName(DatabaseUpdateQuery query, String setNumber, String rarity, String price,
			String setName, String cardName, boolean isFirstEdition) throws SQLException {

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME;

		if (isFirstEdition) {
			update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_FIRST;
		}

		query.prepareStatement(update);

		query.bindString(1, price);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);
		query.bindString(4, setName);
		query.bindString(5, cardName);

		return query.executeUpdate();
	}

	public static int updateCardSetPriceWithCardName(DatabaseUpdateQuery query, String setNumber, String rarity, String price,
			String cardName, boolean isFirstEdition) throws SQLException {

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_CARD_NAME;

		if (isFirstEdition) {
			update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_FIRST;
		}

		query.prepareStatement(update);

		query.bindString(1, price);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);
		query.bindString(4, cardName);

		return query.executeUpdate();
	}

	public static int updateCardSetPrice(DatabaseUpdateQuery query, String setNumber, String price, boolean isFirstEdition)
			throws SQLException {

		String update = SQLConst.UPDATE_CARD_SET_PRICE;

		if (isFirstEdition) {
			update = SQLConst.UPDATE_CARD_SET_PRICE_FIRST;
		}

		query.prepareStatement(update);

		query.bindString(1, price);
		query.bindString(2, setNumber);

		return query.executeUpdate();
	}

	public static int updateCardSetUrl(DatabaseUpdateQuery query, String setNumber, String rarity, String setName, String cardName,
			String setURL, String colorVariant) throws SQLException {

		if (colorVariant == null || colorVariant.isBlank()) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		query.prepareStatement(SQLConst.UPDATE_CARD_SET_URL);

		query.bindString(1, setURL);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);
		query.bindString(4, setName);
		query.bindString(5, cardName);
		query.bindString(6, colorVariant);

		return query.executeUpdate();
	}

	public static int updateCardSetUrlAndColor(DatabaseUpdateQuery query, String setNumber, String rarity, String setName, String cardName,
			String setURL, String currentColorVariant, String newColorVariant) throws SQLException {

		if (currentColorVariant == null || currentColorVariant.isBlank()) {
			currentColorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		if (newColorVariant == null || newColorVariant.isBlank()) {
			newColorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		query.prepareStatement(SQLConst.UPDATE_CARD_SET_URL_AND_COLOR);

		query.bindString(1, setURL);
		query.bindString(2, newColorVariant);
		query.bindString(3, setNumber);
		query.bindString(4, rarity);
		query.bindString(5, setName);
		query.bindString(6, cardName);
		query.bindString(7, currentColorVariant);

		return query.executeUpdate();
	}
}
