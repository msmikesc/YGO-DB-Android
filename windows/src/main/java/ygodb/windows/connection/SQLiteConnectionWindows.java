package ygodb.windows.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import ygodb.commonLibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.utility.Util;
import ygodb.commonLibrary.constant.Const;
import ygodb.commonLibrary.utility.YGOLogger;

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

		String setQuery = "Select * from cardSets";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		ResultSet rarities = statementSetQuery.executeQuery();

		HashMap<String, ArrayList<CardSet>> setRarities = new HashMap<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);

			ArrayList<CardSet> currentList = setRarities.computeIfAbsent(set.setNumber, k -> new ArrayList<>());

			currentList.add(set);
		}

		statementSetQuery.close();
		rarities.close();

		return setRarities;
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets where gamePlayCardUUID=? and setName = ?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);
		statementSetQuery.setString(2, setName);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> setRarities = new ArrayList<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);

			setRarities.add(set);
		}

		statementSetQuery.close();
		rarities.close();

		return setRarities;
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets where setNumber = ?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, setNumber);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> setRarities = new ArrayList<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);

			setRarities.add(set);
		}

		statementSetQuery.close();
		rarities.close();

		return setRarities;
	}

	private void getAllCardSetFieldsFromRS(ResultSet rarities, CardSet set) throws SQLException {
		set.gamePlayCardUUID = rarities.getString(Const.GAME_PLAY_CARD_UUID);
		set.cardName = rarities.getString(Const.CARD_NAME);
		set.setNumber = rarities.getString(Const.SET_NUMBER);
		set.setName = rarities.getString(Const.SET_NAME);
		set.setRarity = rarities.getString(Const.SET_RARITY);
		set.setPrice = rarities.getString(Const.SET_PRICE);
		set.setPriceUpdateTime = rarities.getString(Const.SET_PRICE_UPDATE_TIME);
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where a.gamePlayCardUUID=?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);


		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> results = new ArrayList<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);
			set.cardType = rarities.getString(Const.TYPE);

			results.add(set);
		}

		rarities.close();

		return results;
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName)
			throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b " +
				"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where a.gamePlayCardUUID=? and UPPER(a.setName) = UPPER(?)";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);
		statementSetQuery.setString(2, setName);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> setRarities = new ArrayList<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);
			set.cardType = rarities.getString(Const.TYPE);

			setRarities.add(set);
		}

		statementSetQuery.close();
		rarities.close();

		return setRarities;
	}

	@Override
	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {
		return null;
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where gamePlayCardUUID=?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<String> titlesFound = new ArrayList<>();

		while (rarities.next()) {

			titlesFound.add(rarities.getString(Const.GAME_PLAY_CARD_NAME));

		}

		statementSetQuery.close();
		rarities.close();

		if (titlesFound.size() == 1) {
			return titlesFound.get(0);
		}

		return null;
	}

	@Override
	public ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where gamePlayCardUUID=?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<String> titlesFound = new ArrayList<>();

		while (rarities.next()) {

			titlesFound.add(rarities.getString(Const.GAME_PLAY_CARD_NAME));

		}

		statementSetQuery.close();
		rarities.close();

		return titlesFound;
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where UPPER(title)=UPPER(?)";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, title);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<String> idsFound = new ArrayList<>();

		while (rarities.next()) {

			idsFound.add(rarities.getString(Const.GAME_PLAY_CARD_UUID));

		}

		statementSetQuery.close();
		rarities.close();

		if (idsFound.size() == 1) {
			return idsFound.get(0);
		}

		return null;
	}

	@Override
	public String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where passcode = ?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setInt(1, passcode);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<String> idsFound = new ArrayList<>();

		while (rarities.next()) {

			idsFound.add(rarities.getString(Const.GAME_PLAY_CARD_UUID));

		}

		statementSetQuery.close();
		rarities.close();

		if (idsFound.size() == 1) {
			return idsFound.get(0);
		}

		return null;
	}

	@Override
	public ArrayList<OwnedCard> getNumberOfOwnedCardsByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "select sum(quantity), cardName, " +
				"group_concat(DISTINCT setName), MAX(dateBought) as maxDate, " +
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
				"gamePlayCardUUID " +
				"from ownedCards where gamePlayCardUUID = ? group by cardName";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		setQueryStatement.setString(1, gamePlayCardUUID);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			current.gamePlayCardUUID = rs.getString(6);
			current.quantity = rs.getInt(1);
			current.cardName = rs.getString(2);
			current.setName = rs.getString(3);
			current.dateBought = rs.getString(4);
			current.priceBought = rs.getString(5);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCards() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);
		}

		rs.close();
		setQueryStatement.close();

		return cardsInSetList;
	}

	private static void getAllOwnedCardFieldsFromRS(ResultSet rs, OwnedCard current) throws SQLException {
		current.gamePlayCardUUID = rs.getString(Const.GAME_PLAY_CARD_UUID);
		current.rarityUnsure = rs.getInt(Const.RARITY_UNSURE);
		current.quantity = rs.getInt(Const.QUANTITY);
		current.cardName = rs.getString(Const.CARD_NAME);
		current.setCode = rs.getString(Const.SET_CODE);
		current.setNumber = rs.getString(Const.SET_NUMBER);
		current.setName = rs.getString(Const.SET_NAME);
		current.setRarity = rs.getString(Const.SET_RARITY);
		current.colorVariant = rs.getString(Const.SET_RARITY_COLOR_VARIANT);
		current.folderName = rs.getString(Const.FOLDER_NAME);
		current.condition = rs.getString(Const.CONDITION);
		current.editionPrinting = rs.getString(Const.EDITION_PRINTING);
		current.dateBought = rs.getString(Const.DATE_BOUGHT);
		current.priceBought = Util.normalizePrice(rs.getString(Const.PRICE_BOUGHT));
		current.creationDate = rs.getString(Const.CREATION_DATE);
		current.modificationDate = rs.getString(Const.MODIFICATION_DATE);
		current.uuid = rs.getString(Const.UUID);
		current.passcode = rs.getInt(Const.PASSCODE);
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		return null;
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		return null;
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		return null;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards where setNumber is null";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);
		}

		rs.close();
		setQueryStatement.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards where passcode = -1";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);
		}

		rs.close();
		setQueryStatement.close();

		return cardsInSetList;
	}

	@Override
	public HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			String key = current.setNumber + current.priceBought + current.dateBought + current.folderName
					+ current.condition + current.editionPrinting;

			ArrayList<OwnedCard> currentList = ownedCards.computeIfAbsent(key, k -> new ArrayList<>());

			currentList.add(current);
		}

		rs.close();
		setQueryStatement.close();

		return ownedCards;
	}

	@Override
	public ArrayList<OwnedCard> getRarityUnsureOwnedCards() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards where rarityUnsure = 1 order by setName";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);
		}

		rs.close();
		setQueryStatement.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select distinct gamePlayCardUUID from cardSets where setName = ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		setQueryStatement.setString(1, setName);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<String> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			cardsInSetList.add(rs.getString(1));
		}

		rs.close();
		setQueryStatement.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select a.* from gamePlayCard a left join cardSets b " +
				"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where b.setName = ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
		setQueryStatement.setString(1, setName);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			GamePlayCard current = new GamePlayCard();
			getAllGamePlayCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndIdsByArchetype(String archetype) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
		setQueryStatement.setString(1, "%"+archetype+"%");

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			GamePlayCard current = new GamePlayCard();

			getAllGamePlayCardFieldsFromRS(rs, current);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getSortedCardsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select setNumber from cardSets where setName = ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		setQueryStatement.setString(1, setName);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<String> cardsInSetList = new ArrayList<>();

		while (rs.next()) {
			cardsInSetList.add(rs.getString(1));
		}

		rs.close();
		setQueryStatement.close();

		Collections.sort(cardsInSetList);
		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getDistinctSetNames() throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select distinct setName from cardSets";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		ResultSet rs = distinctQueryStatement.executeQuery();

		ArrayList<String> setsList = new ArrayList<>();

		while (rs.next()) {
			setsList.add(rs.getString(1));
		}

		rs.close();
		distinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<String> getDistinctSetAndArchetypeNames() {
		return null;
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select count (distinct setNumber) from cardSets where setName = ?";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		distinctQueryStatement.setString(1, setName);

		ResultSet rs = distinctQueryStatement.executeQuery();

		int results = -1;

		while (rs.next()) {
			results = rs.getInt(1);
		}

		rs.close();
		distinctQueryStatement.close();

		return results;
	}

	@Override
	public int getCountQuantity() throws SQLException {

		Connection connection = this.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName <> 'Manual Folder'";

		PreparedStatement queryStatement = connection.prepareStatement(query);

		ResultSet rs = queryStatement.executeQuery();

		int results = -1;

		while (rs.next()) {
			results = rs.getInt(1);
		}

		rs.close();
		queryStatement.close();

		return results;
	}

	@Override
	public int getCountQuantityManual() throws SQLException {

		Connection connection = this.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName = 'Manual Folder'";

		PreparedStatement queryStatement = connection.prepareStatement(query);

		ResultSet rs = queryStatement.executeQuery();

		int results = -1;

		while (rs.next()) {
			results = rs.getInt(1);
		}

		rs.close();
		queryStatement.close();

		return results;
	}

	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select * from cardSets where UPPER(setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		distinctQueryStatement.setString(1, setName);
		distinctQueryStatement.setString(2, cardName);

		ResultSet rs = distinctQueryStatement.executeQuery();

		CardSet set = new CardSet();

		if (rs.next()) {
			getAllCardSetFieldsFromRS(rs, set);
		}

		rs.close();
		distinctQueryStatement.close();

		return set;
	}

	@Override
	public List<CardSet> getCardSetsForValues(String setNumber, String rarity, String setName)
			throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "select * from cardSets where UPPER(setName) = UPPER(?) " +
				"and UPPER(setNumber) = UPPER(?) and UPPER(setRarity) = UPPER(?) ";

		PreparedStatement statement = connection.prepareStatement(setQuery);

		statement.setString(1, setName);
		statement.setString(2, setNumber);
		statement.setString(3, rarity);

		ResultSet rs = statement.executeQuery();

		ArrayList<CardSet> results = new ArrayList<>();

		while (rs.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rs, set);
			results.add(set);
		}

		rs.close();
		statement.close();

		return results;
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select setName,setCode,numOfCards,releaseDate  from setData where UPPER(setName) = UPPER(?)";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		distinctQueryStatement.setString(1, setName);

		ResultSet rs = distinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.setName = rs.getString(1);
			current.setCode = rs.getString(2);
			current.numOfCards = rs.getInt(3);
			current.tcgDate = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select setName,setCode,numOfCards,releaseDate  from setData where setCode = ?";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		distinctQueryStatement.setString(1, setCode);

		ResultSet rs = distinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.setName = rs.getString(1);
			current.setCode = rs.getString(2);
			current.numOfCards = rs.getInt(3);
			current.tcgDate = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select distinct setName,setCode,numOfCards,releaseDate  from setData";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		ResultSet rs = distinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.setName = rs.getString(1);
			current.setCode = rs.getString(2);
			current.numOfCards = rs.getInt(3);
			current.tcgDate = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distinctQueryStatement.close();

		return setsList;
	}

	@Override
	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException {

		Connection connection = this.getInstance();

		String distinctQuery = "select cardSets.gamePlayCardUUID, cardname, type, setNumber,setRarity, " +
				"cardSets.setName, releaseDate, archetype from cardSets " +
				"join setData on setData.setName = cardSets.setName "
				+ "join gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID "
				+ "where cardName in (select cardName from "
				+ "(Select DISTINCT cardName, setName from cardSets join gamePlayCard on " +
				" gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where type <>'Token') "
				+ "group by cardname having count(cardname) = 1) "
				+ "order by releaseDate";

		PreparedStatement distinctQueryStatement = connection.prepareStatement(distinctQuery);

		ResultSet rs = distinctQueryStatement.executeQuery();

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
				current.gamePlayCardUUID = gamePlayCardUUID;
				current.cardName = cardName;
				current.cardType = type;
				current.releaseDate = releaseDate;
				current.archetype = archetype;
			}

			current.setNumber.add(setNumber);
			current.setRarities.add(setRarity);
			current.setName.add(setName);

			setsList.put(cardName, current);
		}

		rs.close();
		distinctQueryStatement.close();

		return setsList;
	}

	@Override
	public void replaceIntoCardSetMetaData(String setName, String setCode, int numOfCards, String tcgDate)
			throws SQLException {

		Connection connection = this.getInstance();

		String cardSets = "Replace into setData(setName,setCode,numOfCards,releaseDate) values(?,?,?,?)";

		PreparedStatement statementInsertSets = connection.prepareStatement(cardSets);

		statementInsertSets.setString(1, setName);
		statementInsertSets.setString(2, setCode);
		statementInsertSets.setInt(3, numOfCards);
		statementInsertSets.setString(4, tcgDate);

		statementInsertSets.execute();

		statementInsertSets.close();

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

		String gamePlayCard = "select * from gamePlayCard where gamePlayCardUUID = ?";

		PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard);

		setStringOrNull(statementGamePlayCard, 1, gamePlayCardUUID);

		ResultSet rs = statementGamePlayCard.executeQuery();

		GamePlayCard current = new GamePlayCard();

		if (!rs.next()) {
			return null;
		}

		getAllGamePlayCardFieldsFromRS(rs, current);

		rs.close();
		statementGamePlayCard.close();

		return current;

	}

	private void getAllGamePlayCardFieldsFromRS(ResultSet rs, GamePlayCard current) throws SQLException {
		current.gamePlayCardUUID = rs.getString(Const.GAME_PLAY_CARD_UUID);
		current.cardName = rs.getString(Const.GAME_PLAY_CARD_NAME);
		current.cardType = rs.getString(Const.TYPE);
		current.passcode = rs.getInt(Const.PASSCODE);
		current.desc = rs.getString(Const.GAME_PLAY_CARD_TEXT);
		current.attribute = rs.getString(Const.ATTRIBUTE);
		current.race = rs.getString(Const.RACE);
		current.linkval = rs.getString(Const.LINK_VALUE);
		current.level = rs.getString(Const.LEVEL_RANK);
		current.scale = rs.getString(Const.PENDULUM_SCALE);
		current.atk = rs.getString(Const.ATTACK);
		current.def = rs.getString(Const.DEFENSE);
		current.archetype = rs.getString(Const.ARCHETYPE);
		current.modificationDate = rs.getString(Const.MODIFICATION_DATE);
	}

	@Override
	public List<GamePlayCard> getAllGamePlayCard() throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = "select * from gamePlayCard";

		PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard);

		ResultSet rs = statementGamePlayCard.executeQuery();

		ArrayList<GamePlayCard> results = new ArrayList<>();

		while (rs.next()) {

			GamePlayCard current = new GamePlayCard();

			getAllGamePlayCardFieldsFromRS(rs, current);

			results.add(current);
		}

		rs.close();
		statementGamePlayCard.close();

		return results;

	}

	@Override
	public void replaceIntoGamePlayCard(GamePlayCard input) throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = "Replace into gamePlayCard(gamePlayCardUUID,title,type,passcode,lore," +
				"attribute,race,linkValue,level,pendScale,atk,def,archetype, " +
				"modificationDate) " +
				"values(?,?,?,?,?,?,?,?,?,?,?,?,?,datetime('now','localtime'))";

		PreparedStatement statementGamePlayCard = connection.prepareStatement(gamePlayCard);

		setStringOrNull(statementGamePlayCard, 1, input.gamePlayCardUUID);
		setStringOrNull(statementGamePlayCard, 2, input.cardName);
		setStringOrNull(statementGamePlayCard, 3, input.cardType);
		setIntegerOrNull(statementGamePlayCard, 4, input.passcode);
		setStringOrNull(statementGamePlayCard, 5, input.desc);
		setStringOrNull(statementGamePlayCard, 6, input.attribute);
		setStringOrNull(statementGamePlayCard, 7, input.race);
		setStringOrNull(statementGamePlayCard, 8, input.linkval);
		setStringOrNull(statementGamePlayCard, 9, input.level);
		setStringOrNull(statementGamePlayCard, 10, input.scale);
		setStringOrNull(statementGamePlayCard, 11, input.atk);
		setStringOrNull(statementGamePlayCard, 12, input.def);
		setStringOrNull(statementGamePlayCard, 13, input.archetype);

		statementGamePlayCard.execute();

		statementGamePlayCard.close();
	}

	@Override
	public void updateOwnedCardByUUID(OwnedCard card) throws SQLException {

		String gamePlayCardUUID = card.gamePlayCardUUID;
		String folder = card.folderName;
		String name = card.cardName;
		int quantity = card.quantity;
		String setCode = card.setCode;
		String condition = card.condition;
		String printing = card.editionPrinting;
		String priceBought = card.priceBought;
		String dateBought = card.dateBought;
		int rarityUnsure = card.rarityUnsure;
		String colorVariant = card.colorVariant;
		String setNumber = card.setNumber;
		String setName = card.setName;
		String setRarity = card.setRarity;

		int passcode = card.passcode;

		String uuid = card.uuid;

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = "update ownedCards set gamePlayCardUUID = ?,folderName = ?,cardName = ?,quantity = ?,"
				+ "setCode = ?, setNumber = ?,setName = ?,setRarity = ?,setRarityColorVariant = ?,"
				+ "condition = ?,editionPrinting = ?,dateBought = ?,priceBought = ?,rarityUnsure = ?, "
				+ "modificationDate = datetime('now','localtime'), passcode = ? "
				+ "where UUID = ?";

		PreparedStatement statement = connection.prepareStatement(ownedInsert);

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
		statement.close();

	}

	@Override
	public void sellCards(OwnedCard card, int quantity, String priceSold) {

	}

	@Override
	public void upsertOwnedCardBatch(OwnedCard card) throws SQLException {

		String gamePlayCardUUID = card.gamePlayCardUUID;
		String folder = card.folderName;
		String name = card.cardName;
		int quantity = card.quantity;
		String setCode = card.setCode;
		String condition = card.condition;
		String printing = card.editionPrinting;
		String priceBought = card.priceBought;
		String dateBought = card.dateBought;
		int rarityUnsure = card.rarityUnsure;
		String colorVariant = card.colorVariant;
		String setNumber = card.setNumber;
		String setName = card.setName;
		String setRarity = card.setRarity;

		String uuid = card.uuid;
		int passcode = card.passcode;

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = "insert into ownedCards(gamePlayCardUUID,folderName,cardName,quantity,setCode,"
				+ "setNumber,setName,setRarity,setRarityColorVariant,condition,editionPrinting,dateBought"
				+ ",priceBought,rarityUnsure, creationDate, modificationDate, UUID, passcode) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
				+ "datetime('now','localtime'),datetime('now','localtime'),?,?)"
				+ "on conflict (gamePlayCardUUID," +
				"setNumber," +
				"condition," +
				"editionPrinting," +
				"dateBought," +
				"priceBought," +
				"folderName) "
				+ "do update set quantity = ?, rarityUnsure = ?, setRarity = ?, setRarityColorVariant = ?, "
				+ "modificationDate = datetime('now','localtime'), "
				+ "UUID = ?";

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

		String setInsert = "INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName,setRarity,cardName) values(?,?,?,?,?)";

		PreparedStatement statementSetInsert  = connection.prepareStatement(setInsert);

		setStringOrNull(statementSetInsert,1, gamePlayCardUUID);
		setStringOrNull(statementSetInsert,2, setNumber);
		setStringOrNull(statementSetInsert,3, setName);
		setStringOrNull(statementSetInsert,4, rarity);
		setStringOrNull(statementSetInsert,5, cardName);

		if(price != null && !Util.normalizePrice(price).equals(Util.normalizePrice("0"))){

			List<CardSet> list = getCardSetsForValues(setNumber, rarity, setName);

			if(!list.isEmpty() && list.get(0).setPrice == null || Util.normalizePrice(price).equals(Util.normalizePrice("0"))) {
				updateCardSetPriceWithSetName(setNumber, rarity, price, setName);
			}
		}

		statementSetInsert.execute();
		statementSetInsert.close();
	}

	@Override
	public void updateSetName(String original, String newName) throws SQLException {

		Connection connection = this.getInstance();

		String setInsert = "update cardSets set setName = ? where setName = ?";

		PreparedStatement statementSetInsert = connection.prepareStatement(setInsert);

		try {
			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
			statementSetInsert.close();
		} catch (Exception e) {
			YGOLogger.error("Unable to update cardSets for " + original);
		}

		try {
			setInsert = "update ownedCards set setName = ? where setName = ?";

			statementSetInsert = connection.prepareStatement(setInsert);

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
			statementSetInsert.close();
		} catch (Exception e) {
			YGOLogger.error("Unable to update ownedCards for " + original);
		}

		try {
			setInsert = "update setData set setName = ? where setName = ?";

			statementSetInsert = connection.prepareStatement(setInsert);

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
			statementSetInsert.close();
		} catch (Exception e) {
			YGOLogger.error("Unable to update set data for " + original);
		}
	}

	@Override
	public int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException {

		Connection connection = this.getInstance();

		String update = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
				+ " where setNumber = ? and setRarity = ?";

		PreparedStatement statement = connection.prepareStatement(update);

		statement.setString(1, price);
		statement.setString(2, setNumber);
		statement.setString(3, rarity);

		statement.execute();
		statement.close();

		return getUpdatedRowCount();

	}

	@Override
	public int getUpdatedRowCount() throws SQLException {
		Connection connection = this.getInstance();
		PreparedStatement statement;
		String query = "select changes()";

		statement = connection.prepareStatement(query);

		ResultSet rs = statement.executeQuery();

		rs.next();
		int updated = rs.getInt(1);

		rs.close();

		return updated;
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName)
			throws SQLException {

		Connection connection = this.getInstance();

		String update = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
				+ " where setNumber = ? and setRarity = ? and setName = ?";

		PreparedStatement statement = connection.prepareStatement(update);

		statement.setString(1, price);
		statement.setString(2, setNumber);
		statement.setString(3, rarity);
		statement.setString(4, setName);

		statement.execute();
		statement.close();

		return getUpdatedRowCount();

	}

	@Override
	public int updateCardSetPrice(String setNumber, String price) throws SQLException {

		Connection connection = this.getInstance();

		String update = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
				+ " where setNumber = ?";

		PreparedStatement statement = connection.prepareStatement(update);

		statement.setString(1, price);
		statement.setString(2, setNumber);

		statement.execute();
		statement.close();

		return getUpdatedRowCount();

	}

}