package com.example.ygodb.backend.connection;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import com.example.ygodb.backend.bean.AnalyzePrintedOnceData;
import com.example.ygodb.backend.bean.CardSet;
import com.example.ygodb.backend.bean.GamePlayCard;
import com.example.ygodb.backend.bean.OwnedCard;
import com.example.ygodb.backend.bean.SetMetaData;

public class SQLiteConnection extends SQLiteOpenHelper {

	private static SQLiteDatabase connection = null;
	private static SQLiteConnection instance = null;
	private static Context cont = null;
	private static final String DB_NAME = "database.sqlite";
	//private static String OLD_DB_PATH = DB_DIR + "old_" + DB_NAME;

	private static final String dbFilePath = "database/YGO-DB.db";

	private SQLiteConnection(Context context, String path){
		super(context, path, null, 1);
	}

	public static void initializeInstance(Context context) throws SQLException {

		cont = context.getApplicationContext();
		String DB_DIR = cont.getFilesDir().getAbsolutePath();
		String DB_PATH = DB_DIR +"/"+ DB_NAME;

		if (instance == null) {
			instance = new SQLiteConnection(cont, DB_PATH);
		}

		instance.getWritableDatabase();

		if (instance.createDatabase) {
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
				instance.copyDataBaseFromAppResources();
			} catch (IOException e) {
				e.printStackTrace();
				throw new Error("Error copying database");
			}
		}
		//else if (instance.upgradeDatabase) {
		//}

	}

	public static SQLiteDatabase getInstance(){
		return instance.getWritableDatabase();
	}

	public static SQLiteConnection getObj(){
		return instance;
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

	public void copyDataBaseFromURI(InputStream input) throws IOException {

		if(input == null){
			return;
		}

		close();

		/*
		 * Open the database in the assets folder as the input stream.
		 */
		InputStream myInput = input;

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

	public HashMap<String, ArrayList<CardSet>> getAllCardRarities() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from cardSets";

		Cursor rs = connection.rawQuery(setQuery, null);

		HashMap<String, ArrayList<CardSet>> setrs = new HashMap<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));

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

	public ArrayList<CardSet> getAllRaritiesOfCardByID(int id) {
		//TODO add name?

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from cardSets where wikiID=?";

		String[] params = new String[]{id+""};

		Cursor rs = connection.rawQuery(setQuery, params);

		String[] col = rs.getColumnNames();

		ArrayList<CardSet> setrs = new ArrayList<>();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));

			setrs.add(set);
			
		}

		rs.close();

		return setrs;
	}

	public ArrayList<CardSet> getRaritiesOfCardInSetByID(int id, String setName) {
		//TODO add name?

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.wikiID = b.wikiID " +
				"and b.title = a.cardName where a.wikiID=? and a.setName = ?";

		String[] params = new String[]{id+"", setName};


		Cursor rs = connection.rawQuery(setQuery, params);

		String[] col = rs.getColumnNames();

		ArrayList<CardSet> setrs = new ArrayList<>();

		while (rs.moveToNext ()) {
			CardSet set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
			set.cardType = rs.getString(getColumn(col,"type"));

			setrs.add(set);
		}

		rs.close();

		return setrs;
	}

	public ArrayList<CardSet> getRaritiesOfCardByID(int id) {
		//TODO add name?

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.wikiID = b.wikiID " +
				"and b.title = a.cardName where a.wikiID=?";

		String[] params = new String[]{id+""};


		Cursor rs = connection.rawQuery(setQuery, params);

		String[] col = rs.getColumnNames();

		ArrayList<CardSet> setrs = new ArrayList<>();

		while (rs.moveToNext ()) {
			CardSet set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
			set.cardType = rs.getString(getColumn(col,"type"));

			setrs.add(set);
		}

		rs.close();

		return setrs;
	}

	public ArrayList<CardSet> getRaritiesOfCardInSetByIDAndName(int id, String setName, String cardName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from cardSets a left join gamePlayCard b on a.wikiID = b.wikiID and b.title = a.cardName where a.wikiID=? and UPPER(a.setName) = UPPER(?) and UPPER(a.cardName) = UPPER(?)";

		String[] params = new String[]{id+"", setName, cardName};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<CardSet> setrs = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			CardSet set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
			set.cardType = rs.getString(getColumn(col,"type"));

			setrs.add(set);
			
		}

		rs.close();

		return setrs;
	}

	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] Columns = new String[]{"a.wikiID","a.cardName as cardNameCol","a.setNumber as setNumberCol","a.setName",
				"a.setRarity as setRarityCol","a.setPrice","sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate"};

		String[] selectionArgs = null;
		String selection = "a.cardName like ?";
		selectionArgs = new String[]{'%' + cardName.trim() + '%'};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol";

		Cursor rs = connection.query("cardSets a left outer join ownedCards b " +
						"on a.wikiID = b.wikiID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity",
				Columns, selection,selectionArgs, groupBy,null, orderBy, null);

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {
			OwnedCard current = new OwnedCard();
			current.id = rs.getInt(getColumn(col,"wikiID"));
			current.cardName = rs.getString(getColumn(col,"cardNameCol"));
			current.setNumber = rs.getString(getColumn(col,"setNumberCol"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarityCol"));
			current.priceBought = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.dateBought = rs.getString(getColumn(col,"maxDate"));

			results.add(current);
		}

		rs.close();

		return results;
	}

	public String getCardTitleFromID(int wikiID) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from gamePlayCard where wikiID=?";

		String[] params = new String[]{wikiID+""};

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

	public int getCardIdFromTitle(String title) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "Select * from gamePlayCard where title=?";

		String[] params = new String[]{title};

		Cursor rs = connection.rawQuery(setQuery, params);

		int id = -1;

		ArrayList<Integer> idsFound = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			idsFound.add(rs.getInt(getColumn(col,"wikiID")));
			
		}

		rs.close();

		if (idsFound.size() == 1) {
			return idsFound.get(0);
		}

		return id;
	}

	public ArrayList<OwnedCard> getNumberOfOwnedCardsById(int id) {
		//TODO add name?

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select sum(quantity), cardName, " +
				"group_concat(DISTINCT setName), MAX(dateBought) as maxDate, " +
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice " +
				"from ownedCards where wikiID = ? group by cardName";

		String[] params = new String[]{id+""};

		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = id;
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

	public ArrayList<OwnedCard> getAllOwnedCards() throws SQLException {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		Cursor rs = connection.rawQuery(setQuery, null);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = rs.getInt(getColumn(col,"wikiID"));
			current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.cardName = rs.getString(getColumn(col,"cardName"));
			current.setCode = rs.getString(getColumn(col,"setCode"));
			current.setNumber = rs.getString(getColumn(col,"setNumber"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarity"));
			current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
			current.folderName = rs.getString(getColumn(col,"folderName"));
			current.condition = rs.getString(getColumn(col,"condition"));
			current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
			current.dateBought = rs.getString(getColumn(col,"dateBought"));
			current.priceBought = rs.getString(getColumn(col,"priceBought"));
			current.creationDate = rs.getString(getColumn(col,"creationDate"));
			current.modificationDate = rs.getString(getColumn(col,"modificationDate"));

			current.priceLow = rs.getString(getColumn(col,"priceLow"));
			current.priceMid = rs.getString(getColumn(col,"priceMid"));
			current.priceMarket = rs.getString(getColumn(col,"priceMarket"));

			current.UUID = rs.getString(getColumn(col,"UUID"));

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String[] Columns = new String[]{"wikiID","rarityUnsure","quantity","cardName","setCode",
				"setNumber","setName","setRarity","setRarityColorVariant","folderName","condition",
				"editionPrinting","dateBought","priceBought","creationDate","modificationDate",
				"priceLow","priceMid","priceMarket","UUID"};

		//PRIMARY KEY("wikiID","folderName","setNumber","setRarity","setRarityColorVariant",
		// "condition","editionPrinting","dateBought","priceBought")

		String[] selectionArgs = null;
		String selection = "wikiID = ? AND folderName = ? AND setNumber = ? AND setRarity = ? AND " +
				"setRarityColorVariant = ? AND condition = ? AND editionPrinting = ? AND " +
				"dateBought = ? AND priceBought = ?";
		selectionArgs = new String[]{query.id+"", query.folderName, query.setNumber, query.setRarity,
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

		current.id = rs.getInt(getColumn(col,"wikiID"));
		current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
		current.quantity = rs.getInt(getColumn(col,"quantity"));
		current.cardName = rs.getString(getColumn(col,"cardName"));
		current.setCode = rs.getString(getColumn(col,"setCode"));
		current.setNumber = rs.getString(getColumn(col,"setNumber"));
		current.setName = rs.getString(getColumn(col,"setName"));
		current.setRarity = rs.getString(getColumn(col,"setRarity"));
		current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
		current.folderName = rs.getString(getColumn(col,"folderName"));
		current.condition = rs.getString(getColumn(col,"condition"));
		current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
		current.dateBought = rs.getString(getColumn(col,"dateBought"));
		current.priceBought = rs.getString(getColumn(col,"priceBought"));
		current.creationDate = rs.getString(getColumn(col,"creationDate"));
		current.modificationDate = rs.getString(getColumn(col,"modificationDate"));

		current.priceLow = rs.getString(getColumn(col,"priceLow"));
		current.priceMid = rs.getString(getColumn(col,"priceMid"));
		current.priceMarket = rs.getString(getColumn(col,"priceMarket"));

		current.UUID = rs.getString(getColumn(col,"UUID"));

		rs.close();

		return current;
	}

	public ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String[] Columns = new String[]{"wikiID", "quantity", "cardName", "setNumber", "setName",
				"setRarity", "setRarityColorVariant", "editionPrinting", "dateBought", "priceBought",
				"UUID", "setCode"};

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

			current.id = rs.getInt(getColumn(col,"wikiID"));
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

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	public ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String[] Columns = new String[]{"wikiID", "sum(quantity) as totalQuantity", "cardName",
				"group_concat(DISTINCT setName)", "MAX(dateBought) as maxDate",
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice",
				"group_concat(DISTINCT setRarity) as rs"};

		String selection = null;
		String[] selectionArgs = null;

		if(cardNameSearch != null && !cardNameSearch.equals("")){
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%"+cardNameSearch+"%"};
		}

		Cursor rs = connection.query("ownedCards", Columns, selection,selectionArgs,
				"cardName",null,orderBy, offset + "," + limit);

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		String[] col = rs.getColumnNames();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = rs.getInt(0);
			current.quantity = rs.getInt(1);
			current.cardName = rs.getString(2);
			current.setName = rs.getString(3);
			current.dateBought = rs.getString(4);
			current.priceBought = rs.getString(5);
			current.setRarity = rs.getString(6);

			cardsInSetList.add(current);

		}

		rs.close();

		return cardsInSetList;
	}

	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetCode() {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select * from ownedCards where setCode is null";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = rs.getInt(getColumn(col,"wikiID"));
			current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.cardName = rs.getString(getColumn(col,"cardName"));
			current.setCode = rs.getString(getColumn(col,"setCode"));
			current.setNumber = rs.getString(getColumn(col,"setNumber"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarity"));
			current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
			current.folderName = rs.getString(getColumn(col,"folderName"));
			current.condition = rs.getString(getColumn(col,"condition"));
			current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
			current.dateBought = rs.getString(getColumn(col,"dateBought"));
			current.priceBought = rs.getString(getColumn(col,"priceBought"));
			current.creationDate = rs.getString(getColumn(col,"creationDate"));
			current.modificationDate = rs.getString(getColumn(col,"modificationDate"));

			current.priceLow = rs.getString(getColumn(col,"priceLow"));
			current.priceMid = rs.getString(getColumn(col,"priceMid"));
			current.priceMarket = rs.getString(getColumn(col,"priceMarket"));

			current.UUID = rs.getString(getColumn(col,"UUID"));

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	public HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select * from ownedCards order by setName, setRarity, cardName";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = rs.getInt(getColumn(col,"wikiID"));
			current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.cardName = rs.getString(getColumn(col,"cardName"));
			current.setCode = rs.getString(getColumn(col,"setCode"));
			current.setNumber = rs.getString(getColumn(col,"setNumber"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarity"));
			current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
			current.folderName = rs.getString(getColumn(col,"folderName"));
			current.condition = rs.getString(getColumn(col,"condition"));
			current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
			current.dateBought = rs.getString(getColumn(col,"dateBought"));
			current.priceBought = rs.getString(getColumn(col,"priceBought"));
			current.creationDate = rs.getString(getColumn(col,"creationDate"));
			current.modificationDate = rs.getString(getColumn(col,"modificationDate"));

			current.priceLow = rs.getString(getColumn(col,"priceLow"));
			current.priceMid = rs.getString(getColumn(col,"priceMid"));
			current.priceMarket = rs.getString(getColumn(col,"priceMarket"));

			current.UUID = rs.getString(getColumn(col,"UUID"));

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

	public ArrayList<OwnedCard> getRarityUnsureOwnedCards() {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select * from ownedCards where rarityUnsure = 1 order by setName";

		Cursor rs = connection.rawQuery(setQuery, null);
		String[] col = rs.getColumnNames();

		ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			OwnedCard current = new OwnedCard();

			current.id = rs.getInt(getColumn(col,"wikiID"));
			current.rarityUnsure = rs.getInt(getColumn(col,"rarityUnsure"));
			current.quantity = rs.getInt(getColumn(col,"quantity"));
			current.cardName = rs.getString(getColumn(col,"cardName"));
			current.setCode = rs.getString(getColumn(col,"setCode"));
			current.setNumber = rs.getString(getColumn(col,"setNumber"));
			current.setName = rs.getString(getColumn(col,"setName"));
			current.setRarity = rs.getString(getColumn(col,"setRarity"));
			current.colorVariant = rs.getString(getColumn(col,"setRarityColorVariant"));
			current.folderName = rs.getString(getColumn(col,"folderName"));
			current.condition = rs.getString(getColumn(col,"condition"));
			current.editionPrinting = rs.getString(getColumn(col,"editionPrinting"));
			current.dateBought = rs.getString(getColumn(col,"dateBought"));
			current.priceBought = rs.getString(getColumn(col,"priceBought"));
			current.creationDate = rs.getString(getColumn(col,"creationDate"));
			current.modificationDate = rs.getString(getColumn(col,"modificationDate"));

			current.priceLow = rs.getString(getColumn(col,"priceLow"));
			current.priceMid = rs.getString(getColumn(col,"priceMid"));
			current.priceMarket = rs.getString(getColumn(col,"priceMarket"));

			current.UUID = rs.getString(getColumn(col,"UUID"));

			cardsInSetList.add(current);
			
		}

		rs.close();

		return cardsInSetList;
	}

	public ArrayList<Integer> getDistinctCardIDsInSetByName(String setName) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select distinct wikiID from cardSets where setName = ?";

		String[] params = new String[]{setName};
		Cursor rs = connection.rawQuery(setQuery, params);

		ArrayList<Integer> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			cardsInSetList.add(rs.getInt(0));
			
		}

		rs.close();

		return cardsInSetList;
	}

	public ArrayList<Integer> getDistinctCardIDsByArchetype(String archetype) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setQuery = "select distinct wikiID from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";

		String[] params = new String[]{archetype, "%"+archetype+"%"};
		Cursor rs = connection.rawQuery(setQuery, params);
		String[] col = rs.getColumnNames();

		ArrayList<Integer> cardsInSetList = new ArrayList<>();

		while (rs.moveToNext()) {

			cardsInSetList.add(rs.getInt(getColumn(col,"wikiID")));
			
		}

		rs.close();

		return cardsInSetList;
	}

	public ArrayList<String> getSortedCardsInSetByName(String setName) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public ArrayList<String> getDistinctSetNames() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select distinct cardSets.setName from cardSets inner join setData on cardSets.setName = setData.setName order by setData.releaseDate desc";

		Cursor rs = connection.rawQuery(query, null);
		String[] col = rs.getColumnNames();

		ArrayList<String> setsList = new ArrayList<>();

		while (rs.moveToNext()) {
			setsList.add(rs.getString(getColumn(col,"setName")));
			
		}

		rs.close();

		return setsList;
	}

	public ArrayList<String> getDistinctSetAndArchetypeNames() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select * from (select distinct cardSets.setName from cardSets inner join setData on cardSets.setName = setData.setName order by setData.releaseDate desc)\n" +
				"UNION ALL\n" +
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

	public int getCountDistinctCardsInSet(String setName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public int getCountQuantity() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName <> 'Manual Folder'";

		Cursor rs = connection.rawQuery(query, null);

		int results = -1;

		while (rs.moveToNext()) {
			results = rs.getInt(0);
		}

		rs.close();

		return results;
	}

	public int getCountQuantityManual() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select sum(quantity) from ownedcards where ownedcards.folderName = 'Manual Folder'";

		Cursor rs = connection.rawQuery(query, null);

		int results = -1;

		while (rs.moveToNext()) {
			results = rs.getInt(0);
			
		}

		rs.close();

		return results;
	}

	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select * from cardSets where UPPER(setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";

		String[] params = new String[]{setName, cardName};
		Cursor rs = connection.rawQuery(query, params);
		String[] col = rs.getColumnNames();

		CardSet set = null;

		if (rs.moveToNext()) {
			set = new CardSet();
			set.id = rs.getInt(getColumn(col,"wikiID"));
			set.cardName = rs.getString(getColumn(col,"cardName"));
			set.setNumber = rs.getString(getColumn(col,"setNumber"));
			set.setName = rs.getString(getColumn(col,"setName"));
			set.setRarity = rs.getString(getColumn(col,"setRarity"));
			set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,"setPrice")));
		}

		rs.close();

		return set;
	}

	public ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String query = "select cardSets.wikiid, cardname, type, setNumber,setRarity, cardSets.setName, releaseDate, archetype from cardSets join setData on setData.setName = cardSets.setName \r\n"
				+ "join gamePlayCard on cardSets.cardName = gamePlayCard.title and gamePlayCard.wikiID = cardSets.wikiID\r\n"
				+ "where cardName in (select cardName from \r\n"
				+ "(Select DISTINCT cardName, setName from cardSets join gamePlayCard on gamePlayCard.title = cardSets.cardName and gamePlayCard.wikiid = cardSets.wikiID where type <>'Token') \r\n"
				+ "group by cardname having count(cardname) = 1) \r\n"
				+ "order by releaseDate";

		Cursor rs = connection.rawQuery(query, null);
		String[] col = rs.getColumnNames();

		HashMap<String, AnalyzePrintedOnceData> setsList = new HashMap<>();

		while (rs.moveToNext()) {

			int wikiID = rs.getInt(getColumn(col,"wikiID"));

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
				current.wikiID = wikiID;
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

	public void replaceIntoCardSetMetaData(String set_name, String set_code, int num_of_cards, String tcg_date) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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

	public GamePlayCard getGamePlayCardByNameAndID(Integer wikiID, String name) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String gamePlayCard = "select * from gamePlayCard where wikiID = ? and UPPER(title) = UPPER(?)";

		String[] params = new String[]{wikiID+"", name};
		Cursor rs = connection.rawQuery(gamePlayCard, params);
		String[] col = rs.getColumnNames();

		GamePlayCard current = new GamePlayCard();
		
		if(!rs.moveToNext()) {
			return null;
		}

		current.wikiID = rs.getInt(getColumn(col,"wikiID"));
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

		rs.close();

		return current;
	}

	public void replaceIntoGamePlayCard(Integer wikiID, String name, String type, Integer passcode, String desc,
			String attribute, String race, Integer linkval, Integer level, Integer scale, Integer atk, Integer def,
			String archetype) {
		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String gamePlayCard = "Replace into gamePlayCard(wikiID,title,type,passcode,lore,attribute,race,linkValue,level,pendScale,atk,def,archetype) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		SQLiteStatement statement = connection.compileStatement(gamePlayCard);

		setIntegerOrNull(statement, 1, wikiID);
		setStringOrNull(statement, 2, name);
		setStringOrNull(statement, 3, type);
		setIntegerOrNull(statement, 4, passcode);
		setStringOrNull(statement, 5, desc);
		setStringOrNull(statement, 6, attribute);
		setStringOrNull(statement, 7, race);
		setIntegerOrNull(statement, 8, linkval);
		setIntegerOrNull(statement, 9, level);
		setIntegerOrNull(statement, 10, scale);
		setIntegerOrNull(statement, 11, atk);
		setIntegerOrNull(statement, 12, def);
		setStringOrNull(statement, 13, archetype);

		statement.execute();

		statement.close();
	}

	public void UpdateOwnedCardByUUID(OwnedCard card) {
		
		int id = card.id;
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
		
		String low = card.priceLow;
		String mid = card.priceMid;
		String market = card.priceMarket;
		
		String UUID = card.UUID;

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Util.defaultColorVariant;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = "update ownedCards set wikiID = ?,folderName = ?,cardName = ?,quantity = ?,"
				+ "setCode = ?, setNumber = ?,setName = ?,setRarity = ?,setRarityColorVariant = ?,"
				+ "condition = ?,editionPrinting = ?,dateBought = ?,priceBought = ?,rarityUnsure = ?, "
				+ "modificationDate = datetime('now','localtime'), priceLow = ?, priceMid = ?, priceMarket = ? "
				+ "where UUID = ?";

		SQLiteStatement statement = connection.compileStatement(ownedInsert);

		statement.bindLong(1, id);
		statement.bindString(2, folder);
		statement.bindString(3, name);
		statement.bindLong(4, quantity);
		statement.bindString(5, setCode);
		statement.bindString(6, setNumber);
		statement.bindString(7, setName);
		statement.bindString(8, setRarity);
		statement.bindString(9, colorVariant);
		statement.bindString(10, condition);
		statement.bindString(11, printing);
		statement.bindString(12, dateBought);
		statement.bindString(13, normalizedPrice);
		statement.bindLong(14, rarityUnsure);
		
		statement.bindString(15, low);
		statement.bindString(16, mid);
		statement.bindString(17, market);
		
		statement.bindString(18, UUID);
		
		statement.execute();
		statement.close();

	}
	
	public void upsertOwnedCardBatch(OwnedCard card) {
		
		int id = card.id;
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
		
		String low = card.priceLow;
		String mid = card.priceMid;
		String market = card.priceMarket;
		
		String UUID = card.UUID;

		if(UUID == null || UUID.equals("")) {
			UUID = java.util.UUID.randomUUID().toString();
		}

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Util.defaultColorVariant;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = "insert into ownedCards(wikiID,folderName,cardName,quantity,setCode,"
				+ "setNumber,setName,setRarity,setRarityColorVariant,condition,editionPrinting,dateBought"
				+ ",priceBought,rarityUnsure, creationDate, modificationDate, priceLow, priceMid, priceMarket, UUID) "
				+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
				+ "datetime('now','localtime'),datetime('now','localtime'),?,?,?,?) "
				+ "on conflict (wikiID,folderName,setNumber," + "condition,editionPrinting,dateBought,priceBought) "
				+ "do update set quantity = ?, rarityUnsure = ?, setRarity = ?, setRarityColorVariant = ?, "
				+ "modificationDate = datetime('now','localtime'), priceLow = ?, priceMid = ?, priceMarket = ?,"
				+ "UUID = ?";

		SQLiteStatement batchUpsertOwnedCard = connection.compileStatement(ownedInsert);

		setIntegerOrNull(batchUpsertOwnedCard,1, id);
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
		
		setStringOrNull(batchUpsertOwnedCard,15, low);
		setStringOrNull(batchUpsertOwnedCard,16, mid);
		setStringOrNull(batchUpsertOwnedCard,17, market);
		
		setStringOrNull(batchUpsertOwnedCard,18, UUID);
		
		//conflict fields
		
		setIntegerOrNull(batchUpsertOwnedCard,19, quantity);
		setIntegerOrNull(batchUpsertOwnedCard,20, rarityUnsure);
		setStringOrNull(batchUpsertOwnedCard,21, setRarity);
		setStringOrNull(batchUpsertOwnedCard,22, colorVariant);
		
		setStringOrNull(batchUpsertOwnedCard,23, low);
		setStringOrNull(batchUpsertOwnedCard,24, mid);
		setStringOrNull(batchUpsertOwnedCard,25, market);
		
		setStringOrNull(batchUpsertOwnedCard,26, UUID);

		batchUpsertOwnedCard.execute();
	}

	public void replaceIntoCardSet(String setNumber, String rarity, String setName, int wikiID, String price,
			String cardName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

		String setInsert = "replace into cardSets(wikiID,setNumber,setName,setRarity,setPrice, cardName) values(?,?,?,?,?,?)";

		SQLiteStatement statementSetInsert = connection.compileStatement(setInsert);

		statementSetInsert.bindLong(1, wikiID);
		statementSetInsert.bindString(2, setNumber);
		statementSetInsert.bindString(3, setName);
		statementSetInsert.bindString(4, rarity);
		statementSetInsert.bindString(5, price);
		statementSetInsert.bindString(6, cardName);

		statementSetInsert.execute();
		statementSetInsert.close();
	}
	
	public void updateSetName(String original, String newName) {

		SQLiteDatabase connection = SQLiteConnection.getInstance();

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
