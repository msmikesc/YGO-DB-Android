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
import ygodb.commonLibrary.connection.Util;
import ygodb.commonLibrary.constant.Const;

public class SQLiteConnectionWindows implements SQLiteConnection {

	private Connection connection = null;

	public Connection getInstance() throws SQLException {
		if (connection == null) {
			connection = DriverManager
					.getConnection("jdbc:sqlite:C:\\Users\\Mike\\Documents\\GitHub\\YGO-DB\\YGO-DB\\db\\YGO-DB.db");
		}

		return connection;
	}

	@Override
	public void closeInstance() throws SQLException {
		if (batchUpsertOwnedCard != null) {
			batchUpsertOwnedCard.executeBatch();
			batchUpsertOwnedCard.close();
			batchUpsertOwnedCard = null;
		}

		if (connection == null) {
			return;
		}

		connection.close();

		connection = null;
	}

	@Override
	public HashMap<String, ArrayList<CardSet>> getAllCardRarities() throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		ResultSet rarities = statementSetQuery.executeQuery();

		HashMap<String, ArrayList<CardSet>> setRarities = new HashMap<String, ArrayList<CardSet>>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);

			ArrayList<CardSet> currentList = setRarities.get(set.setNumber);

			if (currentList == null) {
				currentList = new ArrayList<CardSet>();
				setRarities.put(set.setNumber, currentList);
			}

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

		ArrayList<CardSet> setRarities = new ArrayList<CardSet>();

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
		set.gamePlayCardUUID = rarities.getString(Const.gamePlayCardUUID);
		set.cardName = rarities.getString("cardName");
		set.setNumber = rarities.getString("setNumber");
		set.setName = rarities.getString("setName");
		set.setRarity = rarities.getString("setRarity");
		set.setPrice = rarities.getString("setPrice");
		set.setPriceUpdateTime = rarities.getString("setPriceUpdateTime");
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"and b.title = a.cardName where a.gamePlayCardUUID=?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);


		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> setrs = new ArrayList<>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);
			set.cardType = rarities.getString("type");

			setrs.add(set);
		}

		rarities.close();

		return setrs;
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUIDAndName(String gamePlayCardUUID, String setName, String cardName)
			throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b " +
				"on a.gamePlayCardUUID = b.gamePlayCardUUID and b.title = a.cardName " +
				"where a.gamePlayCardUUID=? and UPPER(a.setName) = UPPER(?) and " +
				"UPPER(a.cardName) = UPPER(?)";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, gamePlayCardUUID);
		statementSetQuery.setString(2, setName);
		statementSetQuery.setString(3, cardName);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<CardSet> setRarities = new ArrayList<CardSet>();

		while (rarities.next()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rarities, set);
			set.cardType = rarities.getString("type");

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

		ArrayList<String> titlesFound = new ArrayList<String>();

		while (rarities.next()) {

			titlesFound.add(rarities.getString("title"));

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

		ArrayList<String> titlesFound = new ArrayList<String>();

		while (rarities.next()) {

			titlesFound.add(rarities.getString("title"));

		}

		statementSetQuery.close();
		rarities.close();

		return titlesFound;
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where title=?";

		PreparedStatement statementSetQuery = connection.prepareStatement(setQuery);
		statementSetQuery.setString(1, title);

		ResultSet rarities = statementSetQuery.executeQuery();

		ArrayList<String> idsFound = new ArrayList<>();

		while (rarities.next()) {

			idsFound.add(rarities.getString(Const.gamePlayCardUUID));

		}

		statementSetQuery.close();
		rarities.close();

		if (idsFound.size() == 1) {
			return idsFound.get(0);
		}

		return null;
	}

	@Override
	public ArrayList<OwnedCard> getNumberOfOwnedCardsByName(String name) throws SQLException {

		Connection connection = this.getInstance();

		String setQuery = "select sum(quantity), cardName, " +
				"group_concat(DISTINCT setName), MAX(dateBought) as maxDate, " +
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
				"gamePlayCardUUID " +
				"from ownedCards where UPPER(TRIM(cardName)) = UPPER(?) group by cardName";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		setQueryStatement.setString(1, name.trim());

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

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<OwnedCard>();

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
		current.gamePlayCardUUID = rs.getString(Const.gamePlayCardUUID);
		current.rarityUnsure = rs.getInt("rarityUnsure");
		current.quantity = rs.getInt("quantity");
		current.cardName = rs.getString("cardName");
		current.setCode = rs.getString("setCode");
		current.setNumber = rs.getString("setNumber");
		current.setName = rs.getString("setName");
		current.setRarity = rs.getString("setRarity");
		current.colorVariant = rs.getString("setRarityColorVariant");
		current.folderName = rs.getString("folderName");
		current.condition = rs.getString("condition");
		current.editionPrinting = rs.getString("editionPrinting");
		current.dateBought = rs.getString("dateBought");
		current.priceBought = Util.normalizePrice(rs.getString("priceBought"));
		current.creationDate = rs.getString("creationDate");
		current.modificationDate = rs.getString("modificationDate");
		current.UUID = rs.getString("UUID");
		current.passcode = rs.getInt("passcode");
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
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetCode() throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select * from ownedCards where setCode is null";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<OwnedCard>();

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

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<OwnedCard>();

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

		HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<String, ArrayList<OwnedCard>>();

		while (rs.next()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, current);

			String key = current.setNumber + current.priceBought + current.dateBought + current.folderName
					+ current.condition + current.editionPrinting;

			ArrayList<OwnedCard> currentList = ownedCards.get(key);

			if (currentList == null) {
				currentList = new ArrayList<>();
				ownedCards.put(key, currentList);
			}

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

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<OwnedCard>();

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
	public ArrayList<CardSet> getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select distinct cardName, gamePlayCardUUID from cardSets where setName = ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
		setQueryStatement.setString(1, setName);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<CardSet> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			CardSet current = new CardSet();
			current.cardName = rs.getString(1);
			current.gamePlayCardUUID = rs.getString(2);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<CardSet> getDistinctCardNamesAndIdsByArchetype(String archetype) throws SQLException {
		Connection connection = this.getInstance();

		String setQuery = "select distinct title, gamePlayCardUUID from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";

		PreparedStatement setQueryStatement = connection.prepareStatement(setQuery);
		setQueryStatement.setString(1, archetype);

		ResultSet rs = setQueryStatement.executeQuery();

		ArrayList<CardSet> cardsInSetList = new ArrayList<>();

		while (rs.next()) {

			CardSet current = new CardSet();
			current.cardName = rs.getString(1);
			current.gamePlayCardUUID = rs.getString(2);

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

		ArrayList<String> cardsInSetList = new ArrayList<String>();

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

		String distrinctQuery = "select distinct setName from cardSets";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		ArrayList<String> setsList = new ArrayList<String>();

		while (rs.next()) {
			setsList.add(rs.getString(1));
		}

		rs.close();
		distrinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<String> getDistinctSetAndArchetypeNames() {
		return null;
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) throws SQLException {

		Connection connection = this.getInstance();

		String distrinctQuery = "select count (distinct setNumber) from cardSets where setName = ?";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		distrinctQueryStatement.setString(1, setName);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		int results = -1;

		while (rs.next()) {
			results = rs.getInt(1);
		}

		rs.close();
		distrinctQueryStatement.close();

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

		String distrinctQuery = "select * from cardSets where UPPER(setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		distrinctQueryStatement.setString(1, setName);
		distrinctQueryStatement.setString(2, cardName);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		CardSet set = null;

		while (rs.next()) {
			set = new CardSet();
			getAllCardSetFieldsFromRS(rs, set);
			break;
		}

		rs.close();
		distrinctQueryStatement.close();

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

		String distrinctQuery = "select setName,setCode,numOfCards,releaseDate  from setData where UPPER(setName) = UPPER(?)";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		distrinctQueryStatement.setString(1, setName);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<SetMetaData>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(1);
			current.set_code = rs.getString(2);
			current.num_of_cards = rs.getInt(3);
			current.tcg_date = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distrinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) throws SQLException {

		Connection connection = this.getInstance();

		String distrinctQuery = "select setName,setCode,numOfCards,releaseDate  from setData where setCode = ?";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		distrinctQueryStatement.setString(1, setCode);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<SetMetaData>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(1);
			current.set_code = rs.getString(2);
			current.num_of_cards = rs.getInt(3);
			current.tcg_date = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distrinctQueryStatement.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException {

		Connection connection = this.getInstance();

		String distrinctQuery = "select distinct setName,setCode,numOfCards,releaseDate  from setData";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		ArrayList<SetMetaData> setsList = new ArrayList<SetMetaData>();

		while (rs.next()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(1);
			current.set_code = rs.getString(2);
			current.num_of_cards = rs.getInt(3);
			current.tcg_date = rs.getString(4);

			setsList.add(current);
		}

		rs.close();
		distrinctQueryStatement.close();

		return setsList;
	}

	@Override
	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException {

		Connection connection = this.getInstance();

		String distrinctQuery = "select cardSets.gamePlayCardUUID, cardname, type, setNumber, " +
				"setRarity, cardSets.setName, releaseDate, archetype from cardSets join setData on setData.setName = cardSets.setName "
				+ "join gamePlayCard on cardSets.cardName = gamePlayCard.title and gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID "
				+ "where cardName in (select cardName from "
				+ "(Select DISTINCT cardName, setName from cardSets join gamePlayCard on " +
				" gamePlayCard.title = cardSets.cardName and gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where type <>'Token') "
				+ "group by cardname having count(cardname) = 1) " + "order by releaseDate";

		PreparedStatement distrinctQueryStatement = connection.prepareStatement(distrinctQuery);

		ResultSet rs = distrinctQueryStatement.executeQuery();

		HashMap<String, AnalyzePrintedOnceData> setsList = new HashMap<>();

		while (rs.next()) {

			String gamePlayCardUUID = rs.getString(Const.gamePlayCardUUID);

			String cardName = rs.getString("cardname");
			String type = rs.getString("type");
			String setNumber = rs.getString("setNumber");
			String setRarity = rs.getString("setRarity");
			String setName = rs.getString("setName");
			String releaseDate = rs.getString("releaseDate");
			String archetype = rs.getString("archetype");

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
		distrinctQueryStatement.close();

		return setsList;
	}

	@Override
	public void replaceIntoCardSetMetaData(String set_name, String set_code, int num_of_cards, String tcg_date)
			throws SQLException {

		Connection connection = this.getInstance();

		String cardSets = "Replace into setData(setName,setCode,numOfCards,releaseDate) values(?,?,?,?)";

		PreparedStatement statementInsertSets = connection.prepareStatement(cardSets);

		statementInsertSets.setString(1, set_name);
		statementInsertSets.setString(2, set_code);
		statementInsertSets.setInt(3, num_of_cards);
		statementInsertSets.setString(4, tcg_date);

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
			p.setInt(index, value.intValue());
		}
	}

	@Override
	public GamePlayCard getGamePlayCardByNameAndUUID(String gamePlayCardUUID, String name) throws SQLException {
		Connection connection = this.getInstance();

		String gamePlayCard = "select * from gamePlayCard where gamePlayCardUUID = ? and UPPER(title) = UPPER(?)";

		PreparedStatement statementgamePlayCard = connection.prepareStatement(gamePlayCard);

		setStringOrNull(statementgamePlayCard, 1, gamePlayCardUUID);
		setStringOrNull(statementgamePlayCard, 2, name);

		ResultSet rs = statementgamePlayCard.executeQuery();

		GamePlayCard current = new GamePlayCard();

		if (rs.next() == false) {
			return null;
		}

		current.gamePlayCardUUID = rs.getString(Const.gamePlayCardUUID);
		current.cardName = rs.getString("title");
		current.cardType = rs.getString("type");
		current.passcode = rs.getInt("passcode");
		current.desc = rs.getString("lore");
		current.attribute = rs.getString("attribute");
		current.race = rs.getString("race");
		current.linkval = rs.getString("linkValue");
		current.level = rs.getString("level");
		current.scale = rs.getString("pendScale");
		current.atk = rs.getString("atk");
		current.def = rs.getString("def");
		current.archetype = rs.getString("archetype");
		current.modificationDate = rs.getString("modificationDate");

		rs.close();
		statementgamePlayCard.close();

		return current;

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

			current.gamePlayCardUUID = rs.getString(Const.gamePlayCardUUID);
			current.cardName = rs.getString("title");
			current.cardType = rs.getString("type");
			current.passcode = rs.getInt("passcode");
			current.desc = rs.getString("lore");
			current.attribute = rs.getString("attribute");
			current.race = rs.getString("race");
			current.linkval = rs.getString("linkValue");
			current.level = rs.getString("level");
			current.scale = rs.getString("pendScale");
			current.atk = rs.getString("atk");
			current.def = rs.getString("def");
			current.archetype = rs.getString("archetype");
			current.modificationDate = rs.getString("modificationDate");

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

		PreparedStatement statementgamePlayCard = connection.prepareStatement(gamePlayCard);

		setStringOrNull(statementgamePlayCard, 1, input.gamePlayCardUUID);
		setStringOrNull(statementgamePlayCard, 2, input.cardName);
		setStringOrNull(statementgamePlayCard, 3, input.cardType);
		setIntegerOrNull(statementgamePlayCard, 4, input.passcode);
		setStringOrNull(statementgamePlayCard, 5, input.desc);
		setStringOrNull(statementgamePlayCard, 6, input.attribute);
		setStringOrNull(statementgamePlayCard, 7, input.race);
		setStringOrNull(statementgamePlayCard, 8, input.linkval);
		setStringOrNull(statementgamePlayCard, 9, input.level);
		setStringOrNull(statementgamePlayCard, 10, input.scale);
		setStringOrNull(statementgamePlayCard, 11, input.atk);
		setStringOrNull(statementgamePlayCard, 12, input.def);
		setStringOrNull(statementgamePlayCard, 13, input.archetype);

		statementgamePlayCard.execute();

		statementgamePlayCard.close();
	}

	private PreparedStatement batchUpsertOwnedCard = null;

	private int batchUpsertSize = 1000;

	private int batchUpsertCurrentSize = 0;

	@Override
	public void UpdateOwnedCardByUUID(OwnedCard card) throws SQLException {

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

		String UUID = card.UUID;

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Util.defaultColorVariant;
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
		statement.setString(16, UUID);

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

		String UUID = card.UUID;
		int passcode = card.passcode;

		Connection connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Util.defaultColorVariant;
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

		batchUpsertOwnedCard.setString(15, UUID);
		batchUpsertOwnedCard.setInt(16, passcode);

		// conflict fields

		batchUpsertOwnedCard.setInt(17, Integer.valueOf(quantity));
		batchUpsertOwnedCard.setInt(18, rarityUnsure);
		batchUpsertOwnedCard.setString(19, setRarity);
		batchUpsertOwnedCard.setString(20, colorVariant);

		batchUpsertOwnedCard.setString(21, UUID);

		batchUpsertOwnedCard.addBatch();
		batchUpsertCurrentSize++;

		if (batchUpsertCurrentSize >= batchUpsertSize) {
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

			if(list.size() > 0 && list.get(0).setPrice == null || Util.normalizePrice(price).equals(Util.normalizePrice("0"))) {
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
			System.out.println("Unable to update cardSets for " + original);
		}

		try {
			setInsert = "update ownedCards set setName = ? where setName = ?";

			statementSetInsert = connection.prepareStatement(setInsert);

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
			statementSetInsert.close();
		} catch (Exception e) {
			System.out.println("Unable to update ownedCards for " + original);
		}

		try {
			setInsert = "update setData set setName = ? where setName = ?";

			statementSetInsert = connection.prepareStatement(setInsert);

			statementSetInsert.setString(1, newName);
			statementSetInsert.setString(2, original);

			statementSetInsert.execute();
			statementSetInsert.close();
		} catch (Exception e) {
			System.out.println("Unable to update set data for " + original);
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

		int updated = getUpdatedRowCount();

		return updated;

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

		int updated = getUpdatedRowCount();

		return updated;

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

		int updated = getUpdatedRowCount();

		return updated;

	}

}
