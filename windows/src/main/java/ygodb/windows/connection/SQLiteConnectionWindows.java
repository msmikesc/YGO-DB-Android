package ygodb.windows.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.SQLConst;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

public class SQLiteConnectionWindows implements SQLiteConnection {

	private Connection connectionInstance = null;

	private PreparedStatement batchUpsertOwnedCard = null;

	private static final int BATCH_UPSERT_SIZE = 1000;

	private int batchUpsertCurrentSize = 0;

	public Connection getInstance() throws SQLException {
		if (connectionInstance == null) {
			connectionInstance = DriverManager
					.getConnection("jdbc:sqlite:C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\db\\YGO-DB.db");
		}

		return connectionInstance;
	}

	@Override
	public void closeInstance() throws SQLException {
		if (batchUpsertOwnedCard != null) {
			batchUpsertOwnedCard.executeBatch();
			batchUpsertOwnedCard.close();
			batchUpsertOwnedCard = null;
		}

		if (connectionInstance == null) {
			return;
		}

		connectionInstance.close();

		connectionInstance = null;
	}

	@Override
	public HashMap<String, ArrayList<CardSet>> getAllCardRarities() throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_CARD_RARITIES;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
			 ResultSet rarities = statementSetQuery.executeQuery()) {

			HashMap<String, ArrayList<CardSet>> setRarities = new HashMap<>();

			while (rarities.next()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rarities, set);

				ArrayList<CardSet> currentList = setRarities.computeIfAbsent(set.getSetNumber(), k -> new ArrayList<>());

				currentList.add(set);
			}
			return setRarities;
		}
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_CARD_SETS_OF_CARD_BY_GAME_PLAY_CARD_UUID_AND_SET;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {
			statementSetQuery.setString(1, gamePlayCardUUID);
			statementSetQuery.setString(2, setName);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {

				ArrayList<CardSet> setRarities = new ArrayList<>();

				while (rarities.next()) {
					CardSet set = new CardSet();
					getAllCardSetFieldsFromRS(rarities, set);

					setRarities.add(set);
				}
				return setRarities;
			}

		}
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ALL_CARD_SETS_OF_CARD_BY_SET_NUMBER;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, setNumber);
			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<CardSet> setRarities = new ArrayList<>();

				while (rarities.next()) {
					CardSet set = new CardSet();
					getAllCardSetFieldsFromRS(rarities, set);
					setRarities.add(set);
				}

				return setRarities;
			}
		}
	}

	private void getAllCardSetFieldsFromRS(ResultSet rarities, CardSet set) throws SQLException {
		set.setGamePlayCardUUID(rarities.getString(Const.GAME_PLAY_CARD_UUID));
		set.setCardName(rarities.getString(Const.CARD_NAME));
		set.setSetNumber(rarities.getString(Const.SET_NUMBER));
		set.setSetName(rarities.getString(Const.SET_NAME));
		set.setSetRarity(rarities.getString(Const.SET_RARITY));
		set.setSetPrice(rarities.getString(Const.SET_PRICE));
		set.setSetPriceUpdateTime(rarities.getString(Const.SET_PRICE_UPDATE_TIME));
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, gamePlayCardUUID);
			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<CardSet> results = new ArrayList<>();

				while (rarities.next()) {
					CardSet set = new CardSet();
					getAllCardSetFieldsFromRS(rarities, set);
					set.setCardType(rarities.getString(Const.TYPE));

					results.add(set);
				}

				return results;
			}
		}
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName)
			throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, gamePlayCardUUID);
			statementSetQuery.setString(2, setName);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<CardSet> setRarities = new ArrayList<>();

				while (rarities.next()) {
					CardSet set = new CardSet();
					getAllCardSetFieldsFromRS(rarities, set);
					set.setCardType(rarities.getString(Const.TYPE));

					setRarities.add(set);
				}

				return setRarities;
			}
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, gamePlayCardUUID);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<String> titlesFound = new ArrayList<>();

				while (rarities.next()) {
					titlesFound.add(rarities.getString(Const.GAME_PLAY_CARD_NAME));
				}

				if (titlesFound.size() == 1) {
					return titlesFound.get(0);
				}
			}
		}

		return null;
	}

	@Override
	public ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, gamePlayCardUUID);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<String> titlesFound = new ArrayList<>();

				while (rarities.next()) {
					titlesFound.add(rarities.getString(Const.GAME_PLAY_CARD_NAME));
				}

				return titlesFound;
			}
		}
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {
		String setQuery = SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_TITLE;

		Connection connection = this.getInstance();
		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setString(1, title);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<String> idsFound = new ArrayList<>();

				while (rarities.next()) {
					idsFound.add(rarities.getString(Const.GAME_PLAY_CARD_UUID));
				}

				if (idsFound.size() == 1) {
					return idsFound.get(0);
				}
			}
		}

		return null;
	}

	@Override
	public String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE;

		try (PreparedStatement statementSetQuery = connection.prepareStatement(setQuery)) {

			statementSetQuery.setInt(1, passcode);

			try (ResultSet rarities = statementSetQuery.executeQuery()) {
				ArrayList<String> idsFound = new ArrayList<>();

				while (rarities.next()) {
					idsFound.add(rarities.getString(Const.GAME_PLAY_CARD_UUID));
				}

				if (idsFound.size() == 1) {
					return idsFound.get(0);
				} else if (idsFound.size() > 1) {
					YGOLogger.error("More than 1 GamePlayCard found for passcode:" + passcode);
				}
			}
		}

		return null;
	}

	@Override
	public ArrayList<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ANALYZE_DATA_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery)) {

			setQueryStatement.setString(1, gamePlayCardUUID);

			try (ResultSet rs = setQueryStatement.executeQuery()) {
				ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

				while (rs.next()) {
					OwnedCard current = new OwnedCard();
					current.setGamePlayCardUUID(rs.getString(6));
					current.setQuantity(rs.getInt(1));
					current.setCardName(rs.getString(2));
					current.setSetName(rs.getString(3));
					current.setDateBought(rs.getString(4));
					current.setPriceBought(rs.getString(5));

					cardsInSetList.add(current);
				}

				return cardsInSetList;
			}
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCards() throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ALL_OWNED_CARDS;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
			 ResultSet rs = setQueryStatement.executeQuery()) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.next()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	private static void getAllOwnedCardFieldsFromRS(ResultSet rs, OwnedCard current) throws SQLException {
		current.setGamePlayCardUUID(rs.getString(Const.GAME_PLAY_CARD_UUID));
		current.setRarityUnsure(rs.getInt(Const.RARITY_UNSURE));
		current.setQuantity(rs.getInt(Const.QUANTITY));
		current.setCardName(rs.getString(Const.CARD_NAME));
		current.setSetCode(rs.getString(Const.SET_CODE));
		current.setSetNumber(rs.getString(Const.SET_NUMBER));
		current.setSetName(rs.getString(Const.SET_NAME));
		current.setSetRarity(rs.getString(Const.SET_RARITY));
		current.setColorVariant(rs.getString(Const.SET_RARITY_COLOR_VARIANT));
		current.setFolderName(rs.getString(Const.FOLDER_NAME));
		current.setCondition(rs.getString(Const.CONDITION));
		current.setEditionPrinting(rs.getString(Const.EDITION_PRINTING));
		current.setDateBought(rs.getString(Const.DATE_BOUGHT));
		current.setPriceBought(Util.normalizePrice(rs.getString(Const.PRICE_BOUGHT)));
		current.setCreationDate(rs.getString(Const.CREATION_DATE));
		current.setModificationDate(rs.getString(Const.MODIFICATION_DATE));
		current.setUuid(rs.getString(Const.UUID));
		current.setPasscode(rs.getInt(Const.PASSCODE));
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		return null;
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<OwnedCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_SET_NUMBER;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
			 ResultSet rs = setQueryStatement.executeQuery()) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.next()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
			 ResultSet rs = setQueryStatement.executeQuery()) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.next()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_FOR_HASH_MAP;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
			 ResultSet rs = setQueryStatement.executeQuery()) {

			HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<>();

			while (rs.next()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, current);

				String key = current.getSetNumber() + current.getPriceBought() + current.getDateBought() + current.getFolderName()
						+ current.getCondition() + current.getEditionPrinting();

				ArrayList<OwnedCard> currentList = ownedCards.computeIfAbsent(key, k -> new ArrayList<>());
				currentList.add(current);
			}

			return ownedCards;
		}
	}

	@Override
	public ArrayList<OwnedCard> getRarityUnsureOwnedCards() throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_RARITY_UNSURE_OWNED_CARDS;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
			 ResultSet rs = setQueryStatement.executeQuery()) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.next()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery)) {

			setQueryStatement.setString(1, setName);

			try (ResultSet rs = setQueryStatement.executeQuery()) {
				ArrayList<String> cardsInSetList = new ArrayList<>();

				while (rs.next()) {
					cardsInSetList.add(rs.getString(1));
				}

				return cardsInSetList;
			}
		}
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_DISTINCT_GAMEPLAYCARDS_IN_SET_BY_NAME;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery)) {

			setQueryStatement.setString(1, setName);

			try (ResultSet rs = setQueryStatement.executeQuery()) {
				ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

				while (rs.next()) {
					GamePlayCard current = new GamePlayCard();
					getAllGamePlayCardFieldsFromRS(rs, current);
					cardsInSetList.add(current);
				}

				return cardsInSetList;
			}
		}
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_DISTINCT_GAMEPLAYCARDS_BY_ARCHETYPE;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery)) {

			setQueryStatement.setString(1, "%" + archetype + "%");

			try (ResultSet rs = setQueryStatement.executeQuery()) {
				ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

				while (rs.next()) {
					GamePlayCard current = new GamePlayCard();
					getAllGamePlayCardFieldsFromRS(rs, current);
					cardsInSetList.add(current);
				}

				return cardsInSetList;
			}
		}
	}

	@Override
	public ArrayList<String> getSortedCardsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();
		String setQuery = SQLConst.GET_SORTED_CARDS_IN_SET_BY_NAME;

		try (PreparedStatement setQueryStatement = connection.prepareStatement(setQuery)) {

			setQueryStatement.setString(1, setName);

			try (ResultSet rs = setQueryStatement.executeQuery()) {
				ArrayList<String> cardsInSetList = new ArrayList<>();

				while (rs.next()) {
					cardsInSetList.add(rs.getString(1));
				}

				Collections.sort(cardsInSetList);
				return cardsInSetList;
			}
		}
	}

	@Override
	public ArrayList<String> getDistinctSetNames() throws SQLException {
		Connection connection = this.getInstance();
		String distinctQuery = SQLConst.GET_DISTINCT_SET_NAMES;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);
			 ResultSet rs = distinctQueryStatement.executeQuery()) {

			ArrayList<String> setsList = new ArrayList<>();

			while (rs.next()) {
				setsList.add(rs.getString(1));
			}

			return setsList;
		}
	}

	@Override
	public ArrayList<String> getDistinctSetAndArchetypeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) throws SQLException {
		Connection connection = this.getInstance();
		String distinctQuery = SQLConst.GET_COUNT_DISTINCT_CARDS_IN_SET;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery)) {

			distinctQueryStatement.setString(1, setName);

			try (ResultSet rs = distinctQueryStatement.executeQuery()) {
				int results = -1;

				while (rs.next()) {
					results = rs.getInt(1);
				}

				return results;
			}
		}
	}


	@Override
	public int getCountQuantity() throws SQLException {

		Connection connection = this.getInstance();

		String query = SQLConst.GET_COUNT_QUANTITY;

		try (PreparedStatement queryStatement = connection.prepareStatement(query);
			 ResultSet rs = queryStatement.executeQuery()) {

			int results = -1;

			while (rs.next()) {
				results = rs.getInt(1);
			}

			return results;
		}
	}

	@Override
	public int getCountQuantityManual() throws SQLException {

		Connection connection = this.getInstance();

		String query = SQLConst.GET_COUNT_QUANTITY_MANUAL;

		try (PreparedStatement queryStatement = connection.prepareStatement(query);
			 ResultSet rs = queryStatement.executeQuery()) {

			int results = -1;

			while (rs.next()) {
				results = rs.getInt(1);
			}

			return results;
		}
	}


	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = SQLConst.GET_FIRST_CARD_SET_FOR_CARD_IN_SET;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery)) {

			distinctQueryStatement.setString(1, setName);
			distinctQueryStatement.setString(2, cardName);

			try (ResultSet rs = distinctQueryStatement.executeQuery()) {

				CardSet set = new CardSet();

				if (rs.next()) {
					getAllCardSetFieldsFromRS(rs, set);
				}

				return set;
			}
		}
	}

	@Override
	public List<CardSet> getCardSetsForValues(String setNumber, String rarity, String setName)
			throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = SQLConst.GET_CARD_SETS_FOR_VALUES;

		try (PreparedStatement statement = connection.prepareStatement(setQuery)) {

			statement.setString(1, setName);
			statement.setString(2, setNumber);
			statement.setString(3, rarity);

			try (ResultSet rs = statement.executeQuery()) {

				ArrayList<CardSet> results = new ArrayList<>();

				while (rs.next()) {
					CardSet set = new CardSet();
					getAllCardSetFieldsFromRS(rs, set);
					results.add(set);
				}

				return results;
			}
		}
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = SQLConst.GET_SET_META_DATA_FROM_SET_NAME;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery)) {

			distinctQueryStatement.setString(1, setName);

			try (ResultSet rs = distinctQueryStatement.executeQuery()) {

				ArrayList<SetMetaData> setsList = new ArrayList<>();

				while (rs.next()) {

					SetMetaData current = new SetMetaData();
					current.setSetName(rs.getString(1));
					current.setSetCode(rs.getString(2));
					current.setNumOfCards(rs.getInt(3));
					current.setTcgDate(rs.getString(4));

					setsList.add(current);
				}

				return setsList;
			}
		}
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = SQLConst.GET_SET_META_DATA_FROM_SET_CODE;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery)) {
			distinctQueryStatement.setString(1, setCode);
			try (ResultSet rs = distinctQueryStatement.executeQuery()) {

				ArrayList<SetMetaData> setsList = new ArrayList<>();

				while (rs.next()) {

					SetMetaData current = new SetMetaData();
					current.setSetName(rs.getString(1));
					current.setSetCode(rs.getString(2));
					current.setNumOfCards(rs.getInt(3));
					current.setTcgDate(rs.getString(4));

					setsList.add(current);
				}

				return setsList;
			}
		}
	}

	@Override
	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = SQLConst.GET_ALL_SET_META_DATA_FROM_SET_DATA;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);
			 ResultSet rs = distinctQueryStatement.executeQuery()) {

			ArrayList<SetMetaData> setsList = new ArrayList<>();

			while (rs.next()) {

				SetMetaData current = new SetMetaData();
				current.setSetName(rs.getString(1));
				current.setSetCode(rs.getString(2));
				current.setNumOfCards(rs.getInt(3));
				current.setTcgDate(rs.getString(4));

				setsList.add(current);
			}

			return setsList;
		}
	}

	@Override
	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = SQLConst.GET_CARDS_ONLY_PRINTED_ONCE;

		try (PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);
			 ResultSet rs = distinctQueryStatement.executeQuery()) {

			HashMap<String, AnalyzePrintedOnceData> setsList = new HashMap<>();

			while (rs.next()) {

				String gamePlayCardUUID = rs.getString(Const.GAME_PLAY_CARD_UUID);

				String cardName = rs.getString(Const.CARD_NAME);
				String type = rs.getString(Const.TYPE);
				String setNumber = rs.getString(Const.SET_NUMBER);
				String setRarity = rs.getString(Const.SET_RARITY);
				String setName = rs.getString(Const.SET_NAME);
				String releaseDate = rs.getString(Const.RELEASE_DATE);
				String archetype = rs.getString(Const.ARCHETYPE);

				AnalyzePrintedOnceData current = setsList.get(cardName);

				if (current == null) {
					current = new AnalyzePrintedOnceData();
					current.setGamePlayCardUUID(gamePlayCardUUID);
					current.setCardName(cardName);
					current.setCardType(type);
					current.setReleaseDate(releaseDate);
					current.setArchetype(archetype);
				}

				current.getSetNumber().add(setNumber);
				current.getSetRarities().add(setRarity);
				current.getSetName().add(setName);

				setsList.put(cardName, current);
			}

			return setsList;
		}
	}

	@Override
	public void replaceIntoCardSetMetaData(String setName, String setCode, int numOfCards, String tcgDate)
			throws SQLException {

		Connection connection = this.getInstance();

		String cardSets = SQLConst.REPLACE_INTO_CARD_SET_META_DATA;

		try (PreparedStatement statementInsertSets = connection.prepareStatement(cardSets)) {

			statementInsertSets.setString(1, setName);
			statementInsertSets.setString(2, setCode);
			statementInsertSets.setInt(3, numOfCards);
			statementInsertSets.setString(4, tcgDate);

			statementInsertSets.execute();
		}
	}


	public static void setStringOrNull(PreparedStatement p, int index, String s) throws SQLException {
		if (s == null) {
			p.setNull(index, Types.VARCHAR);
		} else {
			p.setString(index, s);
		}
	}

	public static void setIntegerOrNull(PreparedStatement p, int index, Integer value) throws SQLException {
		if (value == null) {
			p.setNull(index, Types.INTEGER);
		} else {
			p.setInt(index, value);
		}
	}

	@Override
	public GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = SQLConst.GET_GAME_PLAY_CARD_BY_UUID;

		try (PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard)) {
			setStringOrNull(statementGamePlayCard, 1, gamePlayCardUUID);
			try (ResultSet rs = statementGamePlayCard.executeQuery()) {

				GamePlayCard current = new GamePlayCard();

				if (!rs.next()) {
					return null;
				}

				getAllGamePlayCardFieldsFromRS(rs, current);

				return current;
			}
		}
	}

	private void getAllGamePlayCardFieldsFromRS(ResultSet rs, GamePlayCard current) throws SQLException {
		current.setGamePlayCardUUID(rs.getString(Const.GAME_PLAY_CARD_UUID));
		current.setCardName(rs.getString(Const.GAME_PLAY_CARD_NAME));
		current.setCardType(rs.getString(Const.TYPE));
		current.setPasscode(rs.getInt(Const.PASSCODE));
		current.setDesc(rs.getString(Const.GAME_PLAY_CARD_TEXT));
		current.setAttribute(rs.getString(Const.ATTRIBUTE));
		current.setRace(rs.getString(Const.RACE));
		current.setLinkVal(rs.getString(Const.LINK_VALUE));
		current.setLevel(rs.getString(Const.LEVEL_RANK));
		current.setScale(rs.getString(Const.PENDULUM_SCALE));
		current.setAtk(rs.getString(Const.ATTACK));
		current.setDef(rs.getString(Const.DEFENSE));
		current.setArchetype(rs.getString(Const.ARCHETYPE));
		current.setModificationDate(rs.getString(Const.MODIFICATION_DATE));
	}

	@Override
	public List<GamePlayCard> getAllGamePlayCard() throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = SQLConst.GET_ALL_GAME_PLAY_CARD;

		try (PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard)) {

			try (ResultSet rs = statementGamePlayCard.executeQuery()) {

				ArrayList<GamePlayCard> results = new ArrayList<>();

				while (rs.next()) {

					GamePlayCard current = new GamePlayCard();
					getAllGamePlayCardFieldsFromRS(rs, current);
					results.add(current);
				}

				return results;
			}
		}
	}

	@Override
	public void replaceIntoGamePlayCard(GamePlayCard input) throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = SQLConst.REPLACE_INTO_GAME_PLAY_CARD;

		try (PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard)) {

			setStringOrNull(statementGamePlayCard, 1, input.getGamePlayCardUUID());
			setStringOrNull(statementGamePlayCard, 2, input.getCardName());
			setStringOrNull(statementGamePlayCard, 3, input.getCardType());
			setIntegerOrNull(statementGamePlayCard, 4, input.getPasscode());
			setStringOrNull(statementGamePlayCard, 5, input.getDesc());
			setStringOrNull(statementGamePlayCard, 6, input.getAttribute());
			setStringOrNull(statementGamePlayCard, 7, input.getRace());
			setStringOrNull(statementGamePlayCard, 8, input.getLinkVal());
			setStringOrNull(statementGamePlayCard, 9, input.getLevel());
			setStringOrNull(statementGamePlayCard, 10, input.getScale());
			setStringOrNull(statementGamePlayCard, 11, input.getAtk());
			setStringOrNull(statementGamePlayCard, 12, input.getDef());
			setStringOrNull(statementGamePlayCard, 13, input.getArchetype());

			statementGamePlayCard.execute();
		}
	}

	@Override
	public void updateOwnedCardByUUID(OwnedCard card) throws SQLException {

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

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = SQLConst.UPDATE_OWNED_CARD_BY_UUID;

		try (PreparedStatement statement = connection.prepareStatement(ownedInsert)) {

			statement.setString(1, gamePlayCardUUID);
			statement.setString(2, folder);
			statement.setString(3, name);
			statement.setInt(4, quantity);
			statement.setString(5, setCode);
			statement.setString(6, setNumber);
			statement.setString(7, setName);
			statement.setString(8, setRarity);
			statement.setString(9, colorVariant);
			statement.setString(10, condition);
			statement.setString(11, printing);
			statement.setString(12, dateBought);
			statement.setString(13, normalizedPrice);
			statement.setInt(14, rarityUnsure);

			statement.setInt(15, passcode);
			statement.setString(16, uuid);

			statement.execute();
		}
	}

	@Override
	public void sellCards(OwnedCard card, int quantity, String priceSold) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void upsertOwnedCardBatch(OwnedCard card) throws SQLException {

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

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = SQLConst.UPSERT_OWNED_CARD_BATCH;

		if (batchUpsertOwnedCard == null) {

			batchUpsertOwnedCard = connection.prepareStatement(ownedInsert);
		}

		batchUpsertOwnedCard.setString(1, gamePlayCardUUID);
		batchUpsertOwnedCard.setString(2, folder);
		batchUpsertOwnedCard.setString(3, name);
		batchUpsertOwnedCard.setInt(4, quantity);
		batchUpsertOwnedCard.setString(5, setCode);
		batchUpsertOwnedCard.setString(6, setNumber);
		batchUpsertOwnedCard.setString(7, setName);
		batchUpsertOwnedCard.setString(8, setRarity);
		batchUpsertOwnedCard.setString(9, colorVariant);
		batchUpsertOwnedCard.setString(10, condition);
		batchUpsertOwnedCard.setString(11, printing);
		batchUpsertOwnedCard.setString(12, dateBought);
		batchUpsertOwnedCard.setString(13, normalizedPrice);
		batchUpsertOwnedCard.setInt(14, rarityUnsure);

		batchUpsertOwnedCard.setString(15, uuid);
		batchUpsertOwnedCard.setInt(16, passcode);

		// conflict fields

		batchUpsertOwnedCard.setInt(17, quantity);
		batchUpsertOwnedCard.setInt(18, rarityUnsure);
		batchUpsertOwnedCard.setString(19, setRarity);
		batchUpsertOwnedCard.setString(20, colorVariant);

		batchUpsertOwnedCard.setString(21, uuid);

		batchUpsertOwnedCard.addBatch();
		batchUpsertCurrentSize++;

		if (batchUpsertCurrentSize >= BATCH_UPSERT_SIZE) {
			batchUpsertCurrentSize = 0;
			batchUpsertOwnedCard.executeBatch();
		}
	}

	@Override
	public void replaceIntoCardSetWithSoftPriceUpdate(String setNumber, String rarity, String setName, String gamePlayCardUUID, String price,
													  String cardName) throws SQLException {

		Connection connection = this.getInstance();

		String setInsert = SQLConst.REPLACE_INTO_CARD_SET_WITH_SOFT_PRICE_UPDATE;

		try (PreparedStatement statementSetInsert = connection.prepareStatement(setInsert)) {

			setStringOrNull(statementSetInsert, 1, gamePlayCardUUID);
			setStringOrNull(statementSetInsert, 2, setNumber);
			setStringOrNull(statementSetInsert, 3, setName);
			setStringOrNull(statementSetInsert, 4, rarity);
			setStringOrNull(statementSetInsert, 5, cardName);

			if (price != null && !Util.normalizePrice(price).equals(Util.normalizePrice("0"))) {

				List<CardSet> list = getCardSetsForValues(setNumber, rarity, setName);

				if (!list.isEmpty() && (list.get(0).getSetPrice() == null || Util.normalizePrice(price).equals(Util.normalizePrice("0")))) {
					updateCardSetPriceWithSetName(setNumber, rarity, price, setName);
				}
			}

			statementSetInsert.execute();
		}
	}

	@Override
	public void updateSetName(String original, String newName) throws SQLException {

		Connection connection = this.getInstance();

		String setInsert = SQLConst.UPDATE_CARD_SETS_SET_NAME;

		try (PreparedStatement statementSetInsert = connection.prepareStatement(setInsert)) {

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update cardSets for " + original);
		}

		setInsert = SQLConst.UPDATE_OWNED_CARDS_SET_NAME;

		try (PreparedStatement statementSetInsert = connection.prepareStatement(setInsert)) {

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update ownedCards for " + original);
		}

		setInsert = SQLConst.UPDATE_SET_DATA_SET_NAME;

		try (PreparedStatement statementSetInsert = connection.prepareStatement(setInsert)) {

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update set data for " + original);
		}
	}

	@Override
	public int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException {

		Connection connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_RARITY;

		try (PreparedStatement statement = connection.prepareStatement(update)) {

			statement.setString(1, price);
			statement.setString(2, setNumber);
			statement.setString(3, rarity);

			return statement.executeUpdate();
		}
	}

	@Override
	public int getUpdatedRowCount() throws SQLException {
		Connection connection = this.getInstance();
		String query = SQLConst.GET_UPDATED_ROW_COUNT;

		try (PreparedStatement statement = connection.prepareStatement(query);
			 ResultSet rs = statement.executeQuery()) {

			rs.next();
			return rs.getInt(1);
		}
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName)
			throws SQLException {

		Connection connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME;

		try (PreparedStatement statement = connection.prepareStatement(update)) {

			statement.setString(1, price);
			statement.setString(2, setNumber);
			statement.setString(3, rarity);
			statement.setString(4, setName);

			return statement.executeUpdate();
		}
	}

	@Override
	public int updateCardSetPrice(String setNumber, String price) throws SQLException {
		Connection connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE;

		try (PreparedStatement statement = connection.prepareStatement(update)) {
			statement.setString(1, price);
			statement.setString(2, setNumber);

			return statement.executeUpdate();
		}
	}

	@Override
	public int getNewLowestPasscode() throws SQLException {
		Connection connection = this.getInstance();

		String query = SQLConst.GET_NEW_LOWEST_PASSCODE;

		try (PreparedStatement statement = connection.prepareStatement(query);
			 ResultSet rarities = statement.executeQuery()) {
			if(rarities.next()){
				int currentLowest = rarities.getInt(0);
				return currentLowest - 1;
			}
		}
		return -1;
	}

}
