package com.example.ygodb.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.example.ygodb.abs.AndroidUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ygodb.commonLibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonLibrary.bean.CardSet;
import ygodb.commonLibrary.bean.GamePlayCard;
import ygodb.commonLibrary.bean.OwnedCard;
import ygodb.commonLibrary.bean.SetMetaData;
import ygodb.commonLibrary.connection.FileHelper;
import ygodb.commonLibrary.connection.SQLiteConnection;
import ygodb.commonLibrary.utility.Util;
import ygodb.commonLibrary.constant.Const;

public class SQLiteConnectionAndroid extends SQLiteOpenHelper implements SQLiteConnection {

	private SQLiteDatabase connection = null;
	private final Context cont;
	private static final String DB_NAME = "database.sqlite";
	//private static String OLD_DB_PATH = DB_DIR + "old_" + DB_NAME;

	private static final String dbFilePath = "database/YGO-DB.db";

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public SQLiteConnectionAndroid(){
		super(AndroidUtil.getAppContext(),
				AndroidUtil.getAppContext().getFilesDir().getAbsolutePath() + "/" + SQLiteConnectionAndroid.DB_NAME,
				null, 1);
		this.cont = AndroidUtil.getAppContext();
		this.getWritableDatabase();

		if (this.createDatabase) {
			/*
			 * If the database is created by the copy method, then the creation
			 * code needs to go here. This method consists of copying the new
			 * database from assets into internal storage and then caching it.
			 */
			try {
				/*
				 * Write over the empty data that was created in internal
				 * storage with the one in assets and then cache it.
				 */
				this.copyDataBaseFromAppResources();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error("Error copying database");
			}
		}
		//else if (instance.upgradeDatabase) {
		//}

	}

	private SQLiteDatabase getInstance() {
		return this.getWritableDatabase();
	}

	private void copyDataBaseFromAppResources() throws IOException {
		/*
		 * Close SQLiteOpenHelper so it will commit the created empty database
		 * to internal storage.
		 */
		close();

		/*
		 * Open the database in the assets folder as the input stream.
		 */
		InputStream myInput = cont.getAssets().open(dbFilePath);

		/*
		 * Open the empty db in internal storage as the output stream.
		 */
		File output = new File(cont.getFilesDir(),DB_NAME);
		try {
			output.getParentFile().mkdirs();
		}
		catch(Exception e){
			throw new IOException(e);
		}
		OutputStream myOutput = new FileOutputStream(output);

		/*
		 * Copy over the empty db in internal storage with the database in the
		 * assets folder.
		 */
		FileHelper.copyFile(myInput, myOutput);

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	public void copyDataBaseFromURI(InputStream myInput) throws IOException {

		if(myInput == null){
			return;
		}

		close();

		/*
		 * Open the empty db in internal storage as the output stream.
		 */
		File output = new File(cont.getFilesDir(),DB_NAME);
		try {
			output.getParentFile().mkdirs();
		}
		catch(Exception e){
			throw new IOException(e);
		}
		OutputStream myOutput = new FileOutputStream(output);

		/*
		 * Copy over the empty db in internal storage with the database in the
		 * assets folder.
		 */
		FileHelper.copyFile(myInput, myOutput);

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	public void copyDataBaseToURI(OutputStream output) throws IOException {

		if(output == null){
			return;
		}

		close();

		/*
		 * Open the database in the internal folder as the input stream.
		 */
		File input = new File(cont.getFilesDir(),DB_NAME);
		InputStream myInput = new FileInputStream(input);


		/*
		 * Copy over the empty db in internal storage with the database in the
		 * assets folder.
		 */
		FileHelper.copyFile(myInput, output);

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	@Override
	public void closeInstance() {

		if (connection == null) {
			return;
		}

		connection.close();

		connection = null;
	}
	
	private int getColumn(String[] col, String columnName){
		for(int i =0; i < col.length; i++){
			if(col[i].equals(columnName)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public HashMap<String, ArrayList<CardSet>> getAllCardRarities() {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "Select * from cardSets";

		Cursor rs = connection.rawQuery(setQuery, null);

		HashMap<String, ArrayList<CardSet>> setrs = new HashMap<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rs, col, set);

			ArrayList<CardSet> currentList = setrs.get(set.setNumber);

			if (currentList == null) {
				currentList = new ArrayList<>();
				setrs.put(set.setNumber, currentList);
			}

			currentList.add(set);
			
		}
		rs.close();

		return setrs;
	}

	private void getAllCardSetFieldsFromRS(Cursor rs, String[] col, CardSet set) {
		set.gamePlayCardUUID = rs.getString(getColumn(col,Const.gamePlayCardUUID));
		set.cardName = rs.getString(getColumn(col,"cardName"));
		set.setNumber = rs.getString(getColumn(col,"setNumber"));
		set.setName = rs.getString(getColumn(col,"setName"));
		set.setRarity = rs.getString(getColumn(col,"setRarity"));
		set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
		set.setPriceUpdateTime = rs.getString(getColumn(col,"setPriceUpdateTime"));
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException {
		return null;
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where a.gamePlayCardUUID=?";

		String[] params = new String[]{gamePlayCardUUID};


		Cursor rs = connection.rawQuery(setQuery, params);

		String[] col = rs.getColumnNames();

		ArrayList<CardSet> setrs = new ArrayList<>();

		while (rs.moveToNext ()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rs, col, set);
			set.cardType = rs.getString(getColumn(col,"type"));

			setrs.add(set);
		}

		rs.close();

		return setrs;
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b " +
				"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where a.gamePlayCardUUID=? and UPPER(a.setName) = UPPER(?)";

		String[] params = new String[]{gamePlayCardUUID, setName};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<CardSet> setrs = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rs, col, set);
			set.cardType = rs.getString(getColumn(col,"type"));

			setrs.add(set);
			
		}

		rs.close();

		return setrs;
	}

	@Override
	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] Columns = new String[]{"a.gamePlayCardUUID","a.cardName as cardNameCol","a.setNumber as setNumberCol","a.setName",
				"a.setRarity as setRarityCol","a.setPrice","sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate, c.setCode", "d.passcode"};

		String selection = "a.cardName like ?";
		String[] selectionArgs = new String[]{'%' + cardName.trim() + '%'};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol";

		Cursor rs = connection.query("cardSets a left outer join ownedCards b " +
						"on a.gamePlayCardUUID = b.gamePlayCardUUID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity " +
						"left outer join setData c on a.setName = c.setName " +
						"left outer join gamePlayCard d on a.gamePlayCardUUID = d.gamePlayCardUUID",
				Columns, selection,selectionArgs, groupBy,null, orderBy, null);

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			OwnedCard current = new OwnedCard();
			current.gamePlayCardUUID = rs.getString(getColumn(col,Const.gamePlayCardUUID));
			current.cardName = rs.getString(getColumn(col,"cardNameCol"));
			current.setNumber = rs.getString(getColumn(col,"setNumberCol"));
			current.setCode = rs.getString(getColumn(col,"setCode"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarityCol"));
			current.priceBought = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.dateBought = rs.getString(getColumn(col,"maxDate"));
			current.passcode = rs.getInt(getColumn(col,"passcode"));

			results.add(current);
		}

		rs.close();

		return results;
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where gamePlayCardUUID=?";

		String[] params = new String[]{gamePlayCardUUID};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<String> titlesFound = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			titlesFound.add(rs.getString(getColumn(col,"title")));
			
		}
		rs.close();

		if (titlesFound.size() == 1) {
			return titlesFound.get(0);
		}

		return null;
	}

	@Override
	public ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		return null;
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "Select * from gamePlayCard where UPPER(title)=UPPER(?)";

		String[] params = new String[]{title};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<String> idsFound = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			idsFound.add(rs.getString(getColumn(col,Const.gamePlayCardUUID)));

		}

		rs.close();

		if (idsFound.size() == 1) {
			return idsFound.get(0);
		}

		return null;
	}

	@Override
	public ArrayList<OwnedCard> getNumberOfOwnedCardsByGamePlayCardUUID(String name) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select sum(quantity), cardName, " +
				"group_concat(DISTINCT setName), MAX(dateBought) as maxDate, " +
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
				"gamePlayCardUUID " +
				"from ownedCards where gamePlayCardUUID = ? group by cardName";

		String[] params = new String[]{name};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.gamePlayCardUUID = rs.getString(5);
			current.quantity = rs.getInt(0);
			current.cardName = rs.getString(1);
			current.setName = rs.getString(2);
			current.dateBought = rs.getString(3);
			current.priceBought = rs.getString(4);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCards() throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		Cursor rs = connection.rawQuery(setQuery, null);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		SQLiteDatabase connection = this.getInstance();

		String[] Columns = new String[]{"gamePlayCardUUID","rarityUnsure","quantity","cardName","setCode",
				"setNumber","setName","setRarity","setRarityColorVariant","folderName","condition",
				"editionPrinting","dateBought","priceBought","creationDate","modificationDate",
				"UUID", "passcode"};

		//PRIMARY KEY("gamePlayCardUUID","folderName","setNumber","setRarity","setRarityColorVariant",
		// "condition","editionPrinting","dateBought","priceBought")


		if (query.folderName == null || query.setNumber == null ||
				query.setRarity == null || query.colorVariant == null || query.condition == null ||
				query.editionPrinting == null || query.dateBought == null || query.priceBought == null) {
			return null;
		}

		String selection = "gamePlayCardUUID = ? AND folderName = ? AND setNumber = ? AND setRarity = ? AND " +
				"setRarityColorVariant = ? AND condition = ? AND editionPrinting = ? AND " +
				"dateBought = ? AND priceBought = ?";
		String[] selectionArgs = new String[]{query.gamePlayCardUUID, query.folderName, query.setNumber, query.setRarity,
				query.colorVariant, query.condition, query.editionPrinting, query.dateBought, query.priceBought};

		Cursor rs = connection.query("ownedCards", Columns, selection,selectionArgs,
				null,null,null, null);

		String[] col = rs.getColumnNames();

		rs.moveToNext();

		if(rs.isAfterLast()){
			rs.close();
			return null;
		}

		OwnedCard current = new OwnedCard();

		getAllOwnedCardFieldsFromRS(rs, col, current);

		rs.close();

		return current;
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] Columns = new String[]{Const.gamePlayCardUUID, "quantity", "cardName", "setNumber", "setName",
				"setRarity", "setRarityColorVariant", "editionPrinting", "dateBought", "priceBought",
				"UUID", "setCode", "folderName", "rarityUnsure", "condition", "creationDate", "modificationDate", "passcode"};

		String selection = null;
		String[] selectionArgs = null;

		if(cardNameSearch != null && !cardNameSearch.equals("")){
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%"+cardNameSearch+"%"};
		}

		Cursor rs = connection.query("ownedCards", Columns, selection,selectionArgs,
				null,null,orderBy, offset + "," + limit);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	private void getAllOwnedCardFieldsFromRS(Cursor rs, String[] col, OwnedCard current) {
		current.gamePlayCardUUID = rs.getString(getColumn(col,Const.gamePlayCardUUID));
		current.quantity = rs.getInt(getColumn(col,"quantity"));
		current.cardName = rs.getString(getColumn(col,"cardName"));
		current.setNumber = rs.getString(getColumn(col,"setNumber"));
		current.setName = rs.getString(getColumn(col,"setName"));
		current.setRarity = rs.getString(getColumn(col,"setRarity"));
		current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
		current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
		current.dateBought = rs.getString(getColumn(col,"dateBought"));
		current.priceBought = rs.getString(getColumn(col,"priceBought"));
		current.UUID = rs.getString(getColumn(col,"UUID"));
		current.setCode = rs.getString(getColumn(col,"setCode"));
		current.folderName = rs.getString(getColumn(col,"folderName"));
		current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
		current.condition = rs.getString(getColumn(col,"condition"));
		current.creationDate = rs.getString(getColumn(col,"creationDate"));
		current.modificationDate = rs.getString(getColumn(col,"modificationDate"));
		current.passcode = rs.getInt(getColumn(col, "passcode"));

	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] Columns = new String[]{Const.gamePlayCardUUID, "sum(quantity) as totalQuantity", "cardName",
				"group_concat(DISTINCT setName)", "MAX(dateBought) as maxDate",
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice",
				"group_concat(DISTINCT setRarity) as rs", "passcode"};

		String selection = null;
		String[] selectionArgs = null;

		if(cardNameSearch != null && !cardNameSearch.equals("")){
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%"+cardNameSearch+"%"};
		}

		Cursor rs = connection.query("ownedCards", Columns, selection,selectionArgs,
				"cardName",null,orderBy, offset + "," + limit);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.gamePlayCardUUID = rs.getString(0);
			current.quantity = rs.getInt(1);
			current.cardName = rs.getString(2);
			current.setName = rs.getString(3);
			current.dateBought = rs.getString(4);
			current.priceBought = rs.getString(5);
			current.setRarity = rs.getString(6);
			current.passcode = rs.getInt(7);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from ownedCards where setCode is null";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from ownedCards where passcode = -1";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

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

		return ownedCards;
	}

	@Override
	public ArrayList<OwnedCard> getRarityUnsureOwnedCards() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from ownedCards where rarityUnsure = 1 order by setName";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			getAllOwnedCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select distinct gamePlayCardUUID from cardSets where setName = ?";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<String> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			cardsInSetList.add(rs.getString(0));
			
		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select a.* from gamePlayCard a left join cardSets b " +
				"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
				"where b.setName = ?";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			GamePlayCard current = new GamePlayCard();

			getAllGamePlayCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndIdsByArchetype(String archetype) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";

		String[] params = new String[]{archetype, "%"+archetype+"%"};
		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			GamePlayCard current = new GamePlayCard();

			getAllGamePlayCardFieldsFromRS(rs, col, current);

			cardsInSetList.add(current);
		}
		rs.close();

		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getSortedCardsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select setNumber from cardSets where setName = ?";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(setQuery, params);
		String[] col = rs.getColumnNames();

		ArrayList<String> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {
			cardsInSetList.add(rs.getString(getColumn(col,"setNumber")));
			
		}

		rs.close();

		Collections.sort(cardsInSetList);
		return cardsInSetList;
	}

	@Override
	public ArrayList<String> getDistinctSetNames() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select distinct cardSets.setName from cardSets " +
				"inner join setData on cardSets.setName = setData.setName " +
				"order by setData.releaseDate desc";

		Cursor rs = connection.rawQuery(query, null);
		String[] col = rs.getColumnNames();

		ArrayList<String> setsList = new ArrayList<>();

		while (rs.moveToNext()) {
			setsList.add(rs.getString(getColumn(col,"setName")));
			
		}

		rs.close();

		return setsList;
	}

	@Override
	public ArrayList<String> getDistinctSetAndArchetypeNames() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select * from (select distinct cardSets.setName from " +
				"cardSets inner join setData on cardSets.setName = setData.setName " +
				"order by setData.releaseDate desc) " +
				"UNION ALL " +
				"select * from (select distinct archetype from gamePlayCard where archetype is not null order by archetype asc)";

		Cursor rs = connection.rawQuery(query, null);
		String[] col = rs.getColumnNames();

		ArrayList<String> setsList = new ArrayList<>();

		while (rs.moveToNext()) {
			setsList.add(rs.getString(getColumn(col,"setName")));

		}

		rs.close();

		return setsList;
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) {

		SQLiteDatabase connection = this.getInstance();

		String query = "select count (distinct setNumber) from cardSets where setName = ?";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(query, params);

		int results = -1;

		while (rs.moveToNext()) {
			results = rs.getInt(0);
			
		}

		rs.close();

		return results;
	}

	@Override
	public int getCountQuantity() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName <> 'Manual Folder'";

		Cursor rs = connection.rawQuery(query, null);

		int results = -1;

		while (rs.moveToNext()) {
			results = rs.getInt(0);
		}

		rs.close();

		return results;
	}

	@Override
	public int getCountQuantityManual() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName = 'Manual Folder'";

		Cursor rs = connection.rawQuery(query, null);

		int results = -1;

		while (rs.moveToNext()) {
			results = rs.getInt(0);
			
		}

		rs.close();

		return results;
	}

	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) {

		SQLiteDatabase connection = this.getInstance();

		String query = "select * from cardSets where UPPER(setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";

		String[] params = new String[]{setName, cardName};
		Cursor rs = connection.rawQuery(query, params);
		String[] col = rs.getColumnNames();

		CardSet set = null;

		if (rs.moveToNext()) {
			set = new CardSet();
			getAllCardSetFieldsFromRS(rs, col, set);
		}

		rs.close();

		return set;
	}

	@Override
	public List<CardSet> getCardSetsForValues(String setNumber, String rarity, String setName) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = "select * from cardSets where UPPER(setName) = UPPER(?) " +
				"and UPPER(setNumber) = UPPER(?) and UPPER(setRarity) = UPPER(?) ";

		String[] params = new String[]{setName, setNumber, rarity};
		Cursor rs = connection.rawQuery(setQuery, params);
		String[] col = rs.getColumnNames();

		ArrayList<CardSet> results = new ArrayList<>();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			getAllCardSetFieldsFromRS(rs, col, set);
			results.add(set);
		}

		rs.close();

		return results;
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) {

		SQLiteDatabase connection = this.getInstance();

		String query = "select setName,setCode,numOfCards,releaseDate  from setData where UPPER(setName) = UPPER(?)";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(query, params);

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.moveToNext()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(0);
			current.set_code = rs.getString(1);
			current.num_of_cards = rs.getInt(2);
			current.tcg_date = rs.getString(3);

			setsList.add(current);
			
		}

		rs.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) {

		SQLiteDatabase connection = this.getInstance();

		String query = "select setName,setCode,numOfCards,releaseDate  from setData where setCode = ?";

		String[] params = new String[]{setCode};
		Cursor rs = connection.rawQuery(query, params);

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.moveToNext()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(0);
			current.set_code = rs.getString(1);
			current.num_of_cards = rs.getInt(2);
			current.tcg_date = rs.getString(3);

			setsList.add(current);
			
		}

		rs.close();

		return setsList;
	}

	@Override
	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select distinct setName,setCode,numOfCards,releaseDate  from setData";

		Cursor rs = connection.rawQuery(query, null);

		ArrayList<SetMetaData> setsList = new ArrayList<>();

		while (rs.moveToNext()) {

			SetMetaData current = new SetMetaData();
			current.set_name = rs.getString(0);
			current.set_code = rs.getString(1);
			current.num_of_cards = rs.getInt(2);
			current.tcg_date = rs.getString(3);

			setsList.add(current);
			
		}

		rs.close();

		return setsList;
	}

	@Override
	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select cardSets.gamePlayCardUUID, cardname, type, setNumber,setRarity, " +
				"cardSets.setName, releaseDate, archetype from cardSets " +
				"join setData on setData.setName = cardSets.setName "
				+ "join gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID "
				+ "where cardName in (select cardName from "
				+ "(Select DISTINCT cardName, setName from cardSets join gamePlayCard on " +
				" gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where type <>'Token') "
				+ "group by cardname having count(cardname) = 1) "
				+ "order by releaseDate";

		Cursor rs = connection.rawQuery(query, null);
		String[] col = rs.getColumnNames();

		HashMap<String, AnalyzePrintedOnceData> setsList = new HashMap<>();

		while (rs.moveToNext()) {

			String gamePlayCardUUID = rs.getString(getColumn(col,Const.gamePlayCardUUID));

			String cardName = rs.getString(getColumn(col,"cardname"));
			String type = rs.getString(getColumn(col,"type"));
			String setNumber = rs.getString(getColumn(col,"setNumber"));
			String setRarity = rs.getString(getColumn(col,"setRarity"));
			String setName = rs.getString(getColumn(col,"setName"));
			String releaseDate = rs.getString(getColumn(col,"releaseDate"));
			String archetype = rs.getString(getColumn(col,"archetype"));


			AnalyzePrintedOnceData current = setsList.get(cardName);

			if(current == null) {
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

			setsList.put(cardName,current);
			
		}

		rs.close();

		return setsList;
	}

	@Override
	public void replaceIntoCardSetMetaData(String set_name, String set_code, int num_of_cards, String tcg_date) {

		SQLiteDatabase connection = this.getInstance();

		String cardSets = "Replace into setData(setName,setCode,numOfCards,releaseDate) values(?,?,?,?)";

		SQLiteStatement statementInsertSets = connection.compileStatement(cardSets);

		statementInsertSets.bindString(1, set_name);
		statementInsertSets.bindString(2, set_code);
		statementInsertSets.bindLong(3, num_of_cards);
		statementInsertSets.bindString(4, tcg_date);

		statementInsertSets.execute();

		statementInsertSets.close();

	}

	public void setStringOrNull(SQLiteStatement p, int index, String s) {
		if (s == null) {
			p.bindNull(index);
		} else {
			p.bindString(index, s);
		}
	}

	public void setIntegerOrNull(SQLiteStatement p, int index, Integer value) {
		if (value == null) {
			p.bindNull(index);
		} else {
			p.bindLong(index, value);
		}
	}

	@Override
	public GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) {
		SQLiteDatabase connection = this.getInstance();

		String gamePlayCard = "select * from gamePlayCard where gamePlayCardUUID = ?";

		String[] params = new String[]{gamePlayCardUUID};
		Cursor rs = connection.rawQuery(gamePlayCard, params);
		String[] col = rs.getColumnNames();

		GamePlayCard current = new GamePlayCard();
		
		if(!rs.moveToNext()) {
			return null;
		}

		getAllGamePlayCardFieldsFromRS(rs, col, current);

		rs.close();

		return current;
	}

	private void getAllGamePlayCardFieldsFromRS(Cursor rs, String[] col, GamePlayCard current) {
		current.gamePlayCardUUID = rs.getString(getColumn(col,Const.gamePlayCardUUID));
		current.cardName = rs.getString(getColumn(col,"title"));
		current.cardType = rs.getString(getColumn(col,"type"));
		current.passcode = rs.getInt(getColumn(col,"passcode"));
		current.desc = rs.getString(getColumn(col,"lore"));
		current.attribute = rs.getString(getColumn(col,"attribute"));
		current.race = rs.getString(getColumn(col,"race"));
		current.linkval = rs.getString(getColumn(col,"linkValue"));
		current.level = rs.getString(getColumn(col,"level"));
		current.scale = rs.getString(getColumn(col,"pendScale"));
		current.atk = rs.getString(getColumn(col,"atk"));
		current.def = rs.getString(getColumn(col,"def"));
		current.archetype = rs.getString(getColumn(col,"archetype"));
		current.modificationDate = rs.getString(getColumn(col, "modificationDate"));
	}

	@Override
	public List<GamePlayCard> getAllGamePlayCard() throws SQLException {
		return null;
	}

	@Override
	public void replaceIntoGamePlayCard(GamePlayCard input) {
		SQLiteDatabase connection = this.getInstance();

		String gamePlayCard = "Replace into gamePlayCard(gamePlayCardUUID,title,type,passcode,lore," +
				"attribute,race,linkValue,level,pendScale,atk,def,archetype,modificationDate) " +
				"values(?,?,?,?,?,?,?,?,?,?,?,?,?,datetime('now','localtime'))";

		SQLiteStatement statement = connection.compileStatement(gamePlayCard);

		setStringOrNull(statement, 1, input.gamePlayCardUUID);
		setStringOrNull(statement, 2, input.cardName);
		setStringOrNull(statement, 3, input.cardType);
		setIntegerOrNull(statement, 4, input.passcode);
		setStringOrNull(statement, 5, input.desc);
		setStringOrNull(statement, 6, input.attribute);
		setStringOrNull(statement, 7, input.race);
		setStringOrNull(statement, 8, input.linkval);
		setStringOrNull(statement, 9, input.level);
		setStringOrNull(statement, 10, input.scale);
		setStringOrNull(statement, 11, input.atk);
		setStringOrNull(statement, 12, input.def);
		setStringOrNull(statement, 13, input.archetype);

		statement.execute();

		statement.close();
	}

	@Override
	public void UpdateOwnedCardByUUID(OwnedCard card) {
		
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

		SQLiteDatabase connection = this.getInstance();

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

		SQLiteStatement statement = connection.compileStatement(ownedInsert);

		setStringOrNull(statement,1, gamePlayCardUUID);
		setStringOrNull(statement,2, folder);
		setStringOrNull(statement,3, name);
		setIntegerOrNull(statement,4, quantity);
		setStringOrNull(statement,5, setCode);
		setStringOrNull(statement,6, setNumber);
		setStringOrNull(statement,7, setName);
		setStringOrNull(statement,8, setRarity);
		setStringOrNull(statement,9, colorVariant);
		setStringOrNull(statement,10, condition);
		setStringOrNull(statement,11, printing);
		setStringOrNull(statement,12, dateBought);
		setStringOrNull(statement,13, normalizedPrice);
		setIntegerOrNull(statement,14, rarityUnsure);
		
		setIntegerOrNull(statement,15, passcode);
		setStringOrNull(statement,16, UUID);
		
		statement.execute();
		statement.close();

	}

	@Override
	public void sellCards(OwnedCard card, int quantity, String priceSold) {
		SQLiteDatabase connection = this.getInstance();

		// Decrease the quantity of ownedCards by the amount sold
		int newQuantity = card.quantity - quantity;
		if (newQuantity < 0) {
			newQuantity = 0;
		}

		// If the quantity reaches 0, remove the entry from the ownedCards table
		if (newQuantity == 0) {
			String ownedDelete = "DELETE FROM ownedCards WHERE UUID = ?";
			SQLiteStatement statement = connection.compileStatement(ownedDelete);
			setStringOrNull(statement,1, card.UUID);
			statement.execute();
			statement.close();
		}
		else{
			// Update the ownedCards table
			String ownedInsert = "UPDATE ownedCards SET quantity = ?, " +
					"modificationDate = datetime('now','localtime') WHERE UUID = ?";
			SQLiteStatement statement = connection.compileStatement(ownedInsert);
			setIntegerOrNull(statement,1, newQuantity);
			setStringOrNull(statement,2, card.UUID);
			statement.execute();
			statement.close();
		}

		// Insert a corresponding entry into the soldCards table
		String soldInsert = "INSERT INTO soldCards (gamePlayCardUUID, cardName, quantity, setCode, setNumber, " +
				"setName, setRarity, setRarityColorVariant, condition, editionPrinting, dateBought, " +
				"priceBought, dateSold, priceSold, UUID, creationDate, modificationDate, passcode) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now','localtime'), ?)";
		SQLiteStatement statement = connection.compileStatement(soldInsert);
		setStringOrNull(statement,1, card.gamePlayCardUUID);
		setStringOrNull(statement,2, card.cardName);
		setIntegerOrNull(statement,3, quantity);
		setStringOrNull(statement,4, card.setCode);
		setStringOrNull(statement,5, card.setNumber);
		setStringOrNull(statement,6, card.setName);
		setStringOrNull(statement,7, card.setRarity);
		setStringOrNull(statement,8, card.colorVariant);
		setStringOrNull(statement,9, card.condition);
		setStringOrNull(statement,10, card.editionPrinting);
		setStringOrNull(statement,11, card.dateBought);
		setStringOrNull(statement,12, card.priceBought);
		setStringOrNull(statement,13, sdf.format(new Date()));
		setStringOrNull(statement,14, priceSold);
		setStringOrNull(statement,15, card.UUID);
		setStringOrNull(statement,16, card.creationDate);
		setIntegerOrNull(statement,17, card.passcode);
		statement.execute();
		statement.close();
	}
	
	@Override
	public void upsertOwnedCardBatch(OwnedCard card) {
		
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

		if(UUID == null || UUID.equals("")) {
			UUID = java.util.UUID.randomUUID().toString();
		}

		SQLiteDatabase connection = this.getInstance();

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
				+ "datetime('now','localtime'),datetime('now','localtime'),?,?) "
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

		SQLiteStatement batchUpsertOwnedCard = connection.compileStatement(ownedInsert);

		setStringOrNull(batchUpsertOwnedCard,1, gamePlayCardUUID);
		setStringOrNull(batchUpsertOwnedCard, 2, folder);
		setStringOrNull(batchUpsertOwnedCard,3, name);
		setIntegerOrNull(batchUpsertOwnedCard,4, quantity);
		setStringOrNull(batchUpsertOwnedCard,5, setCode);
		setStringOrNull(batchUpsertOwnedCard,6, setNumber);
		setStringOrNull(batchUpsertOwnedCard,7, setName);
		setStringOrNull(batchUpsertOwnedCard,8, setRarity);
		setStringOrNull(batchUpsertOwnedCard,9, colorVariant);
		setStringOrNull(batchUpsertOwnedCard,10, condition);
		setStringOrNull(batchUpsertOwnedCard,11, printing);
		setStringOrNull(batchUpsertOwnedCard,12, dateBought);
		setStringOrNull(batchUpsertOwnedCard,13, normalizedPrice);
		setIntegerOrNull(batchUpsertOwnedCard,14, rarityUnsure);
		
		setStringOrNull(batchUpsertOwnedCard,15, UUID);
		setIntegerOrNull(batchUpsertOwnedCard, 16, passcode);
		
		//conflict fields
		
		setIntegerOrNull(batchUpsertOwnedCard,17, quantity);
		setIntegerOrNull(batchUpsertOwnedCard,18, rarityUnsure);
		setStringOrNull(batchUpsertOwnedCard,19, setRarity);
		setStringOrNull(batchUpsertOwnedCard,20, colorVariant);
		
		setStringOrNull(batchUpsertOwnedCard,21, UUID);

		batchUpsertOwnedCard.execute();
	}

	@Override
	public void replaceIntoCardSetWithSoftPriceUpdate(String setNumber, String rarity, String setName, String gamePlayCardUUID, String price,
													  String cardName) throws SQLException {

		SQLiteDatabase connection = this.getInstance();

		String setInsert = "INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName,setRarity, cardName) values(?,?,?,?,?)";

		SQLiteStatement statementSetInsert = connection.compileStatement(setInsert);

		statementSetInsert.bindString(1, gamePlayCardUUID);
		statementSetInsert.bindString(2, setNumber);
		statementSetInsert.bindString(3, setName);
		statementSetInsert.bindString(4, rarity);
		statementSetInsert.bindString(5, cardName);

		if(price != null && !Util.normalizePrice(price).equals(Util.normalizePrice("0"))){

			List<CardSet> list = getCardSetsForValues(setNumber, rarity, setName);

			if(list.size() > 0 && list.get(0).setPrice != null) {
				updateCardSetPriceWithSetName(setNumber, rarity, price, setName);
			}
		}

		statementSetInsert.execute();
		statementSetInsert.close();
	}
	
	@Override
	public void updateSetName(String original, String newName) {

		SQLiteDatabase connection = this.getInstance();

		String setInsert = "update cardSets set setName = ? where setName = ?";

		SQLiteStatement statementSetInsert = connection.compileStatement(setInsert);

		statementSetInsert.bindString(1, newName);
		statementSetInsert.bindString(2, original);

		statementSetInsert.execute();
		statementSetInsert.close();
		
		setInsert = "update ownedCards set setName = ? where setName = ?";

		statementSetInsert = connection.compileStatement(setInsert);

		statementSetInsert.bindString(1, newName);
		statementSetInsert.bindString(2, original);

		statementSetInsert.execute();
		statementSetInsert.close();
		
		setInsert = "update setData set setName = ? where setName = ?";

		statementSetInsert = connection.compileStatement(setInsert);

		statementSetInsert.bindString(1, newName);
		statementSetInsert.bindString(2, original);

		statementSetInsert.execute();
		statementSetInsert.close();
	}

	@Override
	public int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException {
		return 0;
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String update = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
				+ " where setNumber = ? and setRarity = ? and setName = ?";

		SQLiteStatement statement = connection.compileStatement(update);

		statement.bindString(1, price);
		statement.bindString(2, setNumber);
		statement.bindString(3, rarity);
		statement.bindString(4, setName);

		statement.execute();
		statement.close();

		int updated = getUpdatedRowCount();

		return updated;

	}

	@Override
	public int getUpdatedRowCount() {

		SQLiteDatabase connection = this.getInstance();

		String query = "select changes()";

		Cursor rs = connection.rawQuery(query,null);

		rs.moveToNext();
		int updated = rs.getInt(1);
		return updated;
	}

	@Override
	public int updateCardSetPrice(String setNumber, String price) throws SQLException {
		return 0;
	}

	private boolean createDatabase = false;
	private boolean upgradeDatabase  = false;


	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createDatabase = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		upgradeDatabase = true;
	}
}
