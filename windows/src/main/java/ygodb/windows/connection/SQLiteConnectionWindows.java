package ygodb.windows.connection;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.bean.SoldCard;
import ygodb.commonlibrary.connection.CommonDatabaseQueries;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.DatabaseSelectMapQuery;
import ygodb.commonlibrary.connection.DatabaseSelectQuery;
import ygodb.commonlibrary.connection.DatabaseUpdateQuery;
import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.connection.SelectQueryMapMapper;
import ygodb.commonlibrary.connection.SelectQueryResultMapper;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.constant.SQLConst;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteConnectionWindows implements SQLiteConnection {

	public static final int BATCH_SIZE = 1000;

	private Connection connectionInstance = null;

	public Connection getInstance() throws SQLException {
		if (connectionInstance == null) {
			connectionInstance =
					DriverManager.getConnection("jdbc:sqlite:C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\db\\YGO-DB.db");
		}

		return connectionInstance;
	}

	@Override
	public void closeInstance() throws SQLException {
		if (connectionInstance == null) {
			return;
		}

		if (!connectionInstance.getAutoCommit()) {
			connectionInstance.commit();
		}

		connectionInstance.close();

		connectionInstance = null;
	}

	public static class OwnedCardMapperSelectQuery implements SelectQueryResultMapper<OwnedCard, ResultSet> {
		@Override
		public OwnedCard mapRow(ResultSet resultSet) throws SQLException {
			OwnedCard entity = new OwnedCard();
			getAllOwnedCardFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	private static void getAllOwnedCardFieldsFromRS(ResultSet rs, OwnedCard current) throws SQLException {
		current.setGamePlayCardUUID(rs.getString(Const.GAME_PLAY_CARD_UUID));
		current.setRarityUnsure(rs.getInt(Const.RARITY_UNSURE));
		current.setQuantity(rs.getInt(Const.QUANTITY));
		current.setCardName(rs.getString(Const.CARD_NAME));
		current.setSetPrefix(rs.getString(Const.SET_PREFIX));
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

	public static class CardSetMapperSelectQuery implements SelectQueryResultMapper<CardSet, ResultSet> {
		@Override
		public CardSet mapRow(ResultSet resultSet) throws SQLException {
			CardSet entity = new CardSet();
			getAllCardSetFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	private static void getAllCardSetFieldsFromRS(ResultSet rarities, CardSet set) throws SQLException {
		set.setGamePlayCardUUID(rarities.getString(Const.GAME_PLAY_CARD_UUID));
		set.setCardName(rarities.getString(Const.CARD_NAME));
		set.setSetNumber(rarities.getString(Const.SET_NUMBER));
		set.setSetName(rarities.getString(Const.SET_NAME));
		set.setSetRarity(rarities.getString(Const.SET_RARITY));
		set.setSetPrice(rarities.getString(Const.SET_PRICE));
		set.setSetPriceUpdateTime(rarities.getString(Const.SET_PRICE_UPDATE_TIME));
		set.setSetPriceFirst(rarities.getString(Const.SET_PRICE_FIRST));
		set.setSetPriceFirstUpdateTime(rarities.getString(Const.SET_PRICE_FIRST_UPDATE_TIME));
		set.setSetPriceLimited(rarities.getString(Const.SET_PRICE_LIMITED));
		set.setSetPriceLimitedUpdateTime(rarities.getString(Const.SET_PRICE_LIMITED_UPDATE_TIME));
		set.setSetPrefix(rarities.getString(Const.SET_PREFIX));
		set.setSetUrl(rarities.getString(Const.SET_URL));
		set.setColorVariant(rarities.getString(Const.COLOR_VARIANT));
		set.setAltArtPasscode(rarities.getInt(Const.ALT_ART_PASSCODE));
	}

	public static class GamePlayCardMapperSelectQuery implements SelectQueryResultMapper<GamePlayCard, ResultSet> {
		@Override
		public GamePlayCard mapRow(ResultSet resultSet) throws SQLException {
			GamePlayCard entity = new GamePlayCard();
			getAllGamePlayCardFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	private static void getAllGamePlayCardFieldsFromRS(ResultSet rs, GamePlayCard current) throws SQLException {
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

	public static class SetMetaDataMapperSelectQuery implements SelectQueryResultMapper<SetMetaData, ResultSet> {
		@Override
		public SetMetaData mapRow(ResultSet resultSet) throws SQLException {
			SetMetaData entity = new SetMetaData();
			getAllSetMetaDataFieldsFromRS(resultSet, entity);
			return entity;
		}

		private static void getAllSetMetaDataFieldsFromRS(ResultSet rarities, SetMetaData set) throws SQLException {
			set.setSetName(rarities.getString(Const.SET_NAME));
			set.setSetPrefix(rarities.getString(Const.SET_PREFIX));
			set.setNumOfCards(rarities.getInt(Const.SET_NUM_OF_CARDS));
			set.setTcgDate(rarities.getString(Const.SET_TCG_DATE));
		}
	}

	public static class SetBoxMapperSelectQuery implements SelectQueryResultMapper<SetBox, ResultSet> {
		@Override
		public SetBox mapRow(ResultSet resultSet) throws SQLException {
			SetBox entity = new SetBox();
			getAllSetBoxesFieldsFromRS(resultSet, entity);
			return entity;
		}

		private static void getAllSetBoxesFieldsFromRS(ResultSet rs, SetBox current) throws SQLException {
			current.setSetBoxUUID(rs.getString(Const.SET_BOX_UUID));
			current.setBoxLabel(rs.getString(Const.BOX_LABEL));
			current.setSetPrefix(rs.getString(Const.SET_PREFIX));
			current.setSetName(rs.getString(Const.SET_NAME));
		}
	}

	public static class GamePlayCardNameMapperSelectQuery implements SelectQueryResultMapper<String, ResultSet> {
		@Override
		public String mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getString(Const.GAME_PLAY_CARD_NAME);
		}
	}

	public static class GamePlayCardUUIDMapperSelectQuery implements SelectQueryResultMapper<String, ResultSet> {
		@Override
		public String mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getString(Const.GAME_PLAY_CARD_UUID);
		}
	}

	public static class PasscodeMapperSelectQuery implements SelectQueryResultMapper<Integer, ResultSet> {
		@Override
		public Integer mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getInt(Const.PASSCODE);
		}
	}

	public static class SetNumberMapperSelectQuery implements SelectQueryResultMapper<String, ResultSet> {
		@Override
		public String mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getString(Const.SET_NUMBER);
		}
	}

	public static class SetNameMapperSelectQuery implements SelectQueryResultMapper<String, ResultSet> {
		@Override
		public String mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getString(Const.SET_NAME);
		}
	}

	public static class FirstIntMapperSelectQuery implements SelectQueryResultMapper<Integer, ResultSet> {
		@Override
		public Integer mapRow(ResultSet resultSet) throws SQLException {
			return resultSet.getInt(1);
		}
	}

	public static class CardSetMapMapperSelectQuery implements SelectQueryMapMapper<CardSet, ResultSet> {
		@Override
		public List<String> getKeys(CardSet entity) {
			return DatabaseHashMap.getCardRarityKeys(entity);
		}

		@Override
		public CardSet mapRow(ResultSet resultSet) throws SQLException {
			CardSet entity = new CardSet();
			getAllCardSetFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	@Override
	public Map<String, List<CardSet>> getAllCardRaritiesForHashMap() throws SQLException {
		DatabaseSelectMapQuery<CardSet, ResultSet> query = new DatabaseSelectMapQueryWindows<>(getInstance());

		query.prepareStatement(SQLConst.GET_ALL_CARD_RARITIES);

		return query.executeQuery(new CardSetMapMapperSelectQuery());
	}

	public static class GamePlayCardMapMapperSelectQuery implements SelectQueryMapMapper<GamePlayCard, ResultSet> {
		@Override
		public List<String> getKeys(GamePlayCard entity) {
			return DatabaseHashMap.getGamePlayCardKeys(entity);
		}

		@Override
		public GamePlayCard mapRow(ResultSet resultSet) throws SQLException {
			GamePlayCard entity = new GamePlayCard();
			getAllGamePlayCardFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	@Override
	public Map<String, List<GamePlayCard>> getAllGamePlayCardsForHashMap() throws SQLException {
		DatabaseSelectMapQuery<GamePlayCard, ResultSet> query = new DatabaseSelectMapQueryWindows<>(getInstance());

		query.prepareStatement(SQLConst.GET_ALL_GAME_PLAY_CARD);

		return query.executeQuery(new GamePlayCardMapMapperSelectQuery());
	}

	public static class OwnedCardMapMapperSelectQuery implements SelectQueryMapMapper<OwnedCard, ResultSet> {
		@Override
		public List<String> getKeys(OwnedCard entity) {
			return new ArrayList<>(Collections.singleton(DatabaseHashMap.getOwnedCardHashMapKey(entity)));
		}

		@Override
		public OwnedCard mapRow(ResultSet resultSet) throws SQLException {
			OwnedCard entity = new OwnedCard();
			getAllOwnedCardFieldsFromRS(resultSet, entity);
			return entity;
		}
	}

	@Override
	public Map<String, List<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException {
		DatabaseSelectMapQuery<OwnedCard, ResultSet> query = new DatabaseSelectMapQueryWindows<>(getInstance());

		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_FOR_HASH_MAP);

		return query.executeQuery(new OwnedCardMapMapperSelectQuery());
	}

	@Override
	public List<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<CardSet, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getRaritiesOfCardByGamePlayCardUUID(gamePlayCardUUID, query, new CardSetMapperSelectQuery());
	}

	@Override
	public List<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) throws SQLException {
		DatabaseSelectQuery<CardSet, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName, query,
																			  new CardSetMapperSelectQuery());
	}

	@Override
	public CardSet getRarityOfCardInSetByNumberAndRarity(String gamePlayCardUUID, String setNumber, String rarity, String colorVariant)
			throws SQLException {
		DatabaseSelectQuery<CardSet, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getRarityOfCardInSetByNumberAndRarity(gamePlayCardUUID, setNumber, rarity, colorVariant, query,
																		   new CardSetMapperSelectQuery());
	}

	@Override
	public CardSet getRarityOfExactCardInSet(String gamePlayCardUUID, String setNumber, String rarity, String colorVariant, String setName)
			throws SQLException {
		DatabaseSelectQuery<CardSet, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getRarityOfExactCardInSet(gamePlayCardUUID, setNumber, rarity, colorVariant, setName, query,
															   new CardSetMapperSelectQuery());
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getCardTitleFromGamePlayCardUUID(gamePlayCardUUID, query, new GamePlayCardNameMapperSelectQuery());
	}

	@Override
	public List<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getMultipleCardNamesFromGamePlayCardUUID(gamePlayCardUUID, query,
																			  new GamePlayCardNameMapperSelectQuery());
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardUUIDFromTitle(title, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardUUIDFromPasscode(passcode, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public Integer getPasscodeFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getPasscodeFromGamePlayCardUUID(gamePlayCardUUID, query, new PasscodeMapperSelectQuery());
	}

	public static class AnalyzeDataOwnedCardSummaryMapperSelectQuery implements SelectQueryResultMapper<OwnedCard, ResultSet> {
		@Override
		public OwnedCard mapRow(ResultSet resultSet) throws SQLException {
			OwnedCard entity = new OwnedCard();

			entity.setQuantity(resultSet.getInt(1));
			entity.setCardName(resultSet.getString(2));
			entity.setSetName(resultSet.getString(3));
			entity.setDateBought(resultSet.getString(4));
			entity.setPriceBought(resultSet.getString(5));
			entity.setGamePlayCardUUID(resultSet.getString(6));
			return entity;
		}
	}

	@Override
	public List<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(gamePlayCardUUID, query,
																					  new AnalyzeDataOwnedCardSummaryMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllOwnedCards() throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_SET_PREFIX);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllOwnedCardsWithoutPriceBought() throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_PRICE_BOUGHT);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getRarityUnsureOwnedCards() throws SQLException {
		DatabaseSelectQuery<OwnedCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		query.prepareStatement(SQLConst.GET_RARITY_UNSURE_OWNED_CARDS);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardUUIDsInSetByName(setName, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public List<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardsInSetByName(setName, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public List<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardsByArchetype(archetype, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public List<String> getSortedSetNumbersInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getSortedSetNumbersInSetByName(setName, query, new SetNumberMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctSetNames() throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getDistinctSetNames(query, new SetNameMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctSetAndArchetypeNames() throws SQLException {
		DatabaseSelectQuery<String, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getDistinctSetAndArchetypeNames(query, new SetNameMapperSelectQuery());
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getCountDistinctCardsInSet(setName, query, new FirstIntMapperSelectQuery());
	}

	@Override
	public int getCountQuantity() throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getCountQuantity(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public int getCountQuantityManual() throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getCountQuantityManual(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException {
		DatabaseSelectQuery<CardSet, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getFirstCardSetForCardInSet(cardName, setName, query, new CardSetMapperSelectQuery());
	}

	@Override
	public List<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException {
		DatabaseSelectQuery<SetMetaData, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getSetMetaDataFromSetName(setName, query, new SetMetaDataMapperSelectQuery());
	}

	@Override
	public List<SetMetaData> getSetMetaDataFromSetPrefix(String setPrefix) throws SQLException {
		DatabaseSelectQuery<SetMetaData, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getSetMetaDataFromSetPrefix(setPrefix, query, new SetMetaDataMapperSelectQuery());
	}

	@Override
	public List<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException {
		DatabaseSelectQuery<SetMetaData, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getAllSetMetaDataFromSetData(query, new SetMetaDataMapperSelectQuery());
	}

	@Override
	public Map<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException {

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
	public GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardByUUID(gamePlayCardUUID, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public int getNewLowestPasscode() throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getNewLowestPasscode(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public List<Integer> getAllArtPasscodesByName(String cardName) throws SQLException {
		DatabaseSelectQuery<Integer, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getAllArtPasscodesByName(query, cardName, new FirstIntMapperSelectQuery());
	}

	@Override
	public List<SetBox> getAllSetBoxes() throws SQLException {
		DatabaseSelectQuery<SetBox, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getAllSetBoxes(query, new SetBoxMapperSelectQuery());
	}

	@Override
	public List<SetBox> getSetBoxesByNameOrCodeOrLabel(String searchText) throws SQLException {
		DatabaseSelectQuery<SetBox, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getSetBoxesByNameOrCodeOrLabel(searchText, query, new SetBoxMapperSelectQuery());
	}

	@Override
	public List<SetBox> getNewSetBoxDataForValidSetPrefix(String setPrefix) throws SQLException {
		DatabaseSelectQuery<SetBox, ResultSet> query = new DatabaseSelectQueryWindows<>(getInstance());
		return CommonDatabaseQueries.getNewSetBoxDataForValidSetPrefix(setPrefix, query, new SetBoxMapperSelectQuery());
	}

	@Override
	public int replaceIntoGamePlayCard(GamePlayCard input) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.replaceIntoGamePlayCard(query, input);
	}

	@Override
	public int replaceIntoCardSetMetaData(String setName, String setPrefix, int numOfCards, String tcgDate) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.replaceIntoCardSetMetaData(query, setName, setPrefix, numOfCards, tcgDate);
	}

	@Override
	public int insertOrUpdateOwnedCardByUUID(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertOrUpdateOwnedCardByUUID(query, card);
	}

	@Override
	public int updateOwnedCardByUUID(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateOwnedCardByUUID(query, card);
	}

	@Override
	public int insertIntoOwnedCards(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertIntoOwnedCards(query, card);
	}

	@Override
	public int insertOrUpdateSetBoxByUUID(SetBox setBox) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertOrUpdateSetBoxByUUID(query, setBox);
	}

	@Override
	public int insertIntoSetBoxes(SetBox setBox) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertIntoSetBoxes(query, setBox);
	}

	@Override
	public int insertOrIgnoreIntoCardSet(String setNumber, String rarity, String setName, String gamePlayCardUUID, String cardName,
			String colorVariant, String url) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertOrIgnoreIntoCardSet(query, setNumber, rarity, setName, gamePlayCardUUID, cardName, colorVariant,
															   url);
	}

	@Override
	public int insertOrIgnoreIntoCardSetWithAltArtPasscode(String setNumber, String rarity, String setName, String gamePlayCardUUID, String cardName,
			String colorVariant, String url, Integer altArtPasscode) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.insertOrIgnoreIntoCardSetWithAltArt(query, setNumber, rarity, setName, gamePlayCardUUID, cardName, colorVariant,
															   url, altArtPasscode);
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
	public int updateCardSetPrice(String setNumber, String rarity, String price, String edition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetPrice(query, setNumber, rarity, price, edition);
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName, String edition)
			throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithSetName(query, setNumber, rarity, price, setName, edition);
	}

	@Override
	public int updateCardSetPriceWithCardAndSetName(String setNumber, String rarity, String price, String setName, String cardName,
			String edition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithCardAndSetName(query, setNumber, rarity, price, setName, cardName, edition);
	}

	@Override
	public int updateCardSetPriceWithCardName(String setNumber, String rarity, String price, String cardName, String edition)
			throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithCardName(query, setNumber, rarity, price, cardName, edition);
	}

	@Override
	public int updateCardSetPrice(String setNumber, String price, String edition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetPrice(query, setNumber, price, edition);
	}

	@Override
	public int updateCardSetUrl(String setNumber, String rarity, String setName, String cardName, String setURL, String colorVariant)
			throws SQLException {

		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetUrl(query, setNumber, rarity, setName, cardName, setURL, colorVariant);
	}

	@Override
	public int updateCardSetUrlAndColor(String setNumber, String rarity, String setName, String cardName, String setURL,
			String currentColorVariant, String newColorVariant) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryWindows(getInstance());
		return CommonDatabaseQueries.updateCardSetUrlAndColor(query, setNumber, rarity, setName, cardName, setURL, currentColorVariant,
															  newColorVariant);
	}

	public PreparedStatementBatchWrapper getBatchedPreparedStatement(String input, BatchSetterWindows setter) throws SQLException {
		Connection connection = this.getInstance();
		return new PreparedStatementBatchWrapperWindows(connection, input, BATCH_SIZE, setter);
	}

	@Override
	public PreparedStatementBatchWrapper getBatchedPreparedStatementUrlFirst() throws SQLException {

		return getBatchedPreparedStatement(SQLConst.UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_FIRST, (stmt, params) -> {
			stmt.setString(1, (String) params.get(0));
			stmt.setString(2, (String) params.get(1));
		});
	}

	@Override
	public PreparedStatementBatchWrapper getBatchedPreparedStatementUrlUnlimited() throws SQLException {

		return getBatchedPreparedStatement(SQLConst.UPDATE_CARD_SET_PRICE_BATCHED_BY_URL, (stmt, params) -> {
			stmt.setString(1, (String) params.get(0));
			stmt.setString(2, (String) params.get(1));
		});
	}

	@Override
	public PreparedStatementBatchWrapper getBatchedPreparedStatementUrlLimited() throws SQLException {

		return getBatchedPreparedStatement(SQLConst.UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_LIMITED, (stmt, params) -> {
			stmt.setString(1, (String) params.get(0));
			stmt.setString(2, (String) params.get(1));
		});
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsBySetName(String setName, String orderBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsByArchetype(String archetype, String orderBy) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SoldCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sellCards(OwnedCard card, int quantity, String priceSold) {
		throw new UnsupportedOperationException();
	}
}
