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
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.FileHelper;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.SQLConst;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.YGOLogger;

public class SQLiteConnectionAndroid extends SQLiteOpenHelper implements SQLiteConnection {

	private SQLiteDatabase connectionInstance = null;
	private final Context cont;
	private static final String DB_NAME = "database.sqlite";

	private static final String DB_FILE_PATH = "database/YGO-DB.db";

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private boolean createDatabase = false;
	private boolean upgradeDatabase  = false;

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
				YGOLogger.logException(e);
				throw new UncheckedIOException(e);
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
		InputStream myInput = cont.getAssets().open(DB_FILE_PATH);

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

		myInput.close();
		myOutput.close();

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

		myOutput.close();

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

		myInput.close();

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createDatabase = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
		upgradeDatabase = true;
	}

	@Override
	public void closeInstance() {

		if (connectionInstance == null) {
			return;
		}

		connectionInstance.close();

		connectionInstance = null;
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

		String setQuery = SQLConst.GET_ALL_CARD_RARITIES;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {

			HashMap<String, ArrayList<CardSet>> results = new HashMap<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);

				ArrayList<CardSet> currentList = results.computeIfAbsent(set.setNumber, k -> new ArrayList<>());

				currentList.add(set);
			}

			return results;
		}
	}

	private void getAllCardSetFieldsFromRS(Cursor rs, String[] col, CardSet set) {
		set.gamePlayCardUUID = rs.getString(getColumn(col,Const.GAME_PLAY_CARD_UUID));
		set.cardName = rs.getString(getColumn(col,Const.CARD_NAME));
		set.setNumber = rs.getString(getColumn(col,Const.SET_NUMBER));
		set.setName = rs.getString(getColumn(col,Const.SET_NAME));
		set.setRarity = rs.getString(getColumn(col,Const.SET_RARITY));
		set.setPrice = Util.normalizePrice(rs.getString(getColumn(col,Const.SET_PRICE)));
		set.setPriceUpdateTime = rs.getString(getColumn(col,Const.SET_PRICE_UPDATE_TIME));
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID;

		String[] params = new String[]{gamePlayCardUUID};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

			String[] col = rs.getColumnNames();

			ArrayList<CardSet> results = new ArrayList<>();

			while (rs.moveToNext()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);
				set.cardType = rs.getString(getColumn(col, Const.TYPE));

				results.add(set);
			}

			return results;
		}
	}

	@Override
	public ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID;

		String[] params = new String[]{gamePlayCardUUID, setName};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

			ArrayList<CardSet> results = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);
				set.cardType = rs.getString(getColumn(col, Const.TYPE));

				results.add(set);
			}

			return results;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] columns = new String[]{"a.gamePlayCardUUID","a.cardName as cardNameCol","a.setNumber as setNumberCol","a.setName",
				"a.setRarity as setRarityCol","a.setPrice","sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate, c.setCode", "d.passcode"};

		String selection = "a.cardName like ?";
		String[] selectionArgs = new String[]{'%' + cardName.trim() + '%'};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol";

		try (Cursor rs = connection.query("cardSets a left outer join ownedCards b " +
						"on a.gamePlayCardUUID = b.gamePlayCardUUID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity " +
						"left outer join setData c on a.setName = c.setName " +
						"left outer join gamePlayCard d on a.gamePlayCardUUID = d.gamePlayCardUUID",
				columns, selection, selectionArgs, groupBy, null, orderBy, null)) {

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				current.gamePlayCardUUID = rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID));
				current.cardName = rs.getString(getColumn(col, "cardNameCol"));
				current.setNumber = rs.getString(getColumn(col, "setNumberCol"));
				current.setCode = rs.getString(getColumn(col, Const.SET_CODE));
				current.setName = rs.getString(getColumn(col, Const.SET_NAME));
				current.setRarity = rs.getString(getColumn(col, "setRarityCol"));
				current.priceBought = Util.normalizePrice(rs.getString(getColumn(col, Const.SET_PRICE)));
				current.quantity = rs.getInt(getColumn(col, Const.QUANTITY));
				current.dateBought = rs.getString(getColumn(col, "maxDate"));
				current.passcode = rs.getInt(getColumn(col, Const.PASSCODE));

				results.add(current);
			}

			return results;
		}
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID;

		String[] params = new String[]{gamePlayCardUUID};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

			ArrayList<String> titlesFound = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				titlesFound.add(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_NAME)));
			}

			if (titlesFound.size() == 1) {
				return titlesFound.get(0);
			}

			return null;
		}
	}

	@Override
	public ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_TITLE;

		String[] params = new String[]{title};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

			ArrayList<String> idsFound = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				idsFound.add(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
			}

			if (idsFound.size() == 1) {
				return idsFound.get(0);
			}

			return null;
		}
	}

	@Override
	public String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE;

		String[] params = new String[]{String.valueOf(passcode)};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

			ArrayList<String> idsFound = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				idsFound.add(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
			}

			if (idsFound.size() == 1) {
				return idsFound.get(0);
			} else if (idsFound.size() > 1) {
				YGOLogger.error("More than 1 GamePlayCard found for passcode:" + passcode);
			}

			return null;
		}
	}

	@Override
	public ArrayList<OwnedCard> getNumberOfOwnedCardsByGamePlayCardUUID(String name) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_NUMBER_OF_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID;

		String[] params = new String[]{name};

		try (Cursor rs = connection.rawQuery(setQuery, params)) {

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

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCards() throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_OWNED_CARDS;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns = new String[]{Const.GAME_PLAY_CARD_UUID,Const.RARITY_UNSURE,Const.QUANTITY,Const.CARD_NAME,Const.SET_CODE,
				Const.SET_NUMBER,Const.SET_NAME,Const.SET_RARITY,Const.SET_RARITY_COLOR_VARIANT,Const.FOLDER_NAME,Const.CONDITION,
				Const.EDITION_PRINTING,Const.DATE_BOUGHT,Const.PRICE_BOUGHT,Const.CREATION_DATE,Const.MODIFICATION_DATE,
				Const.UUID, Const.PASSCODE};

		//PRIMARY KEY(Const.gamePlayCardUUID,Const.folderName,Const.setNumber,Const.setRarity,Const.setRarityColorVariant,
		// Const.condition,Const.editionPrinting,Const.dateBought,Const.priceBought)


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

		try (Cursor rs = connection.query("ownedCards", columns, selection, selectionArgs,
				null, null, null, null)) {

			String[] col = rs.getColumnNames();

			if (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				return current;
			}

			return null;
		}
	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns = new String[]{Const.GAME_PLAY_CARD_UUID, Const.QUANTITY, Const.CARD_NAME, Const.SET_NUMBER, Const.SET_NAME,
				Const.SET_RARITY, Const.SET_RARITY_COLOR_VARIANT, Const.EDITION_PRINTING, Const.DATE_BOUGHT, Const.PRICE_BOUGHT,
				Const.UUID, Const.SET_CODE, Const.FOLDER_NAME, Const.RARITY_UNSURE, Const.CONDITION, Const.CREATION_DATE, Const.MODIFICATION_DATE, Const.PASSCODE};

		String selection = null;
		String[] selectionArgs = null;

		if (cardNameSearch != null && !cardNameSearch.equals("")) {
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%" + cardNameSearch + "%"};
		}

		try (Cursor rs = connection.query("ownedCards", columns, selection, selectionArgs,
				null, null, orderBy, offset + "," + limit)) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();
			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	private void getAllOwnedCardFieldsFromRS(Cursor rs, String[] col, OwnedCard current) {
		current.gamePlayCardUUID = rs.getString(getColumn(col,Const.GAME_PLAY_CARD_UUID));
		current.quantity = rs.getInt(getColumn(col,Const.QUANTITY));
		current.cardName = rs.getString(getColumn(col,Const.CARD_NAME));
		current.setNumber = rs.getString(getColumn(col,Const.SET_NUMBER));
		current.setName = rs.getString(getColumn(col,Const.SET_NAME));
		current.setRarity = rs.getString(getColumn(col,Const.SET_RARITY));
		current.colorVariant = rs.getString(getColumn(col,Const.SET_RARITY_COLOR_VARIANT));
		current.editionPrinting = rs.getString(getColumn(col,Const.EDITION_PRINTING));
		current.dateBought = rs.getString(getColumn(col,Const.DATE_BOUGHT));
		current.priceBought = rs.getString(getColumn(col,Const.PRICE_BOUGHT));
		current.uuid = rs.getString(getColumn(col,Const.UUID));
		current.setCode = rs.getString(getColumn(col,Const.SET_CODE));
		current.folderName = rs.getString(getColumn(col,Const.FOLDER_NAME));
		current.rarityUnsure = rs.getInt(getColumn(col,Const.RARITY_UNSURE));
		current.condition = rs.getString(getColumn(col,Const.CONDITION));
		current.creationDate = rs.getString(getColumn(col,Const.CREATION_DATE));
		current.modificationDate = rs.getString(getColumn(col,Const.MODIFICATION_DATE));
		current.passcode = rs.getInt(getColumn(col, Const.PASSCODE));

	}

	@Override
	public ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns = new String[]{Const.GAME_PLAY_CARD_UUID, "sum(quantity) as totalQuantity", Const.CARD_NAME,
				"group_concat(DISTINCT setName)", "MAX(dateBought) as maxDate",
				"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice",
				"group_concat(DISTINCT setRarity) as rs", Const.PASSCODE};

		String selection = null;
		String[] selectionArgs = null;

		if (cardNameSearch != null && !cardNameSearch.equals("")) {
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%" + cardNameSearch + "%"};
		}

		try (Cursor rs = connection.query("ownedCards", columns, selection, selectionArgs,
				Const.CARD_NAME, null, orderBy, offset + "," + limit)) {

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

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_SET_NUMBER;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {
			String[] col = rs.getColumnNames();

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {
			String[] col = rs.getColumnNames();

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_FOR_HASH_MAP;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {
			String[] col = rs.getColumnNames();

			HashMap<String, ArrayList<OwnedCard>> ownedCards = new HashMap<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				String key = current.setNumber + current.priceBought + current.dateBought + current.folderName
						+ current.condition + current.editionPrinting;
				ArrayList<OwnedCard> currentList = ownedCards.computeIfAbsent(key, k -> new ArrayList<>());
				currentList.add(current);
			}

			return ownedCards;
		}
	}

	@Override
	public ArrayList<OwnedCard> getRarityUnsureOwnedCards() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_RARITY_UNSURE_OWNED_CARDS;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {
			String[] col = rs.getColumnNames();

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME;

		String[] params = new String[]{setName};
		try (Cursor rs = connection.rawQuery(setQuery, params)) {
			ArrayList<String> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				cardsInSetList.add(rs.getString(0));
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_DISTINCT_CARD_NAMES_AND_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME;

		String[] params = new String[]{setName};
		try (Cursor rs = connection.rawQuery(setQuery, params)) {
			ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				GamePlayCard current = new GamePlayCard();
				getAllGamePlayCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<GamePlayCard> getDistinctCardNamesAndIdsByArchetype(String archetype) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_DISTINCT_CARD_NAMES_AND_IDS_BY_ARCHETYPE;

		String[] params = new String[]{archetype, "%" + archetype + "%"};
		try (Cursor rs = connection.rawQuery(setQuery, params)) {
			ArrayList<GamePlayCard> cardsInSetList = new ArrayList<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				GamePlayCard current = new GamePlayCard();
				getAllGamePlayCardFieldsFromRS(rs, col, current);
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<String> getSortedCardsInSetByName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_SORTED_CARDS_IN_SET_BY_NAME;

		String[] params = new String[]{setName};
		try (Cursor rs = connection.rawQuery(setQuery, params)) {
			String[] col = rs.getColumnNames();

			ArrayList<String> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				cardsInSetList.add(rs.getString(getColumn(col, Const.SET_NUMBER)));
			}

			Collections.sort(cardsInSetList);
			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<String> getDistinctSetNames() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_DISTINCT_SET_NAMES;

		try (Cursor rs = connection.rawQuery(query, null)) {
			String[] col = rs.getColumnNames();

			ArrayList<String> setsList = new ArrayList<>();

			while (rs.moveToNext()) {
				setsList.add(rs.getString(getColumn(col, Const.SET_NAME)));
			}

			return setsList;
		}
	}

	@Override
	public ArrayList<String> getDistinctSetAndArchetypeNames() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_DISTINCT_SET_AND_ARCHETYPE_NAMES;

		try (Cursor rs = connection.rawQuery(query, null)) {
			String[] col = rs.getColumnNames();

			ArrayList<String> setsList = new ArrayList<>();

			while (rs.moveToNext()) {
				setsList.add(rs.getString(getColumn(col, Const.SET_NAME)));
			}

			return setsList;
		}
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_COUNT_DISTINCT_CARDS_IN_SET;

		String[] params = new String[]{setName};
		try (Cursor rs = connection.rawQuery(query, params)) {
			int results = -1;

			while (rs.moveToNext()) {
				results = rs.getInt(0);
			}

			return results;
		}
	}

	@Override
	public int getCountQuantity() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_COUNT_QUANTITY;

		try (Cursor rs = connection.rawQuery(query, null)) {
			int results = -1;

			while (rs.moveToNext()) {
				results = rs.getInt(0);
			}

			return results;
		}
	}

	@Override
	public int getCountQuantityManual() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_COUNT_QUANTITY_MANUAL;

		try (Cursor rs = connection.rawQuery(query, null)) {
			int results = -1;

			while (rs.moveToNext()) {
				results = rs.getInt(0);
			}

			return results;
		}
	}

	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_FIRST_CARD_SET_FOR_CARD_IN_SET;

		String[] params = new String[]{setName, cardName};
		try (Cursor rs = connection.rawQuery(query, params)) {
			String[] col = rs.getColumnNames();

			CardSet set = null;

			if (rs.moveToNext()) {
				set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);
			}

			return set;
		}
	}

	@Override
	public List<CardSet> getCardSetsForValues(String setNumber, String rarity, String setName) {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_CARD_SETS_FOR_VALUES;

		String[] params = new String[]{setName, setNumber, rarity};
		try (Cursor rs = connection.rawQuery(setQuery, params)) {
			String[] col = rs.getColumnNames();

			List<CardSet> results = new ArrayList<>();

			while (rs.moveToNext()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);
				results.add(set);
			}

			return results;
		}
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_SET_META_DATA_FROM_SET_NAME;

		String[] params = new String[]{setName};
		try (Cursor rs = connection.rawQuery(query, params)) {
			ArrayList<SetMetaData> setsList = new ArrayList<>();

			while (rs.moveToNext()) {
				SetMetaData current = new SetMetaData();
				current.setName = rs.getString(0);
				current.setCode = rs.getString(1);
				current.numOfCards = rs.getInt(2);
				current.tcgDate = rs.getString(3);

				setsList.add(current);
			}

			return setsList;
		}
	}

	@Override
	public ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_SET_META_DATA_FROM_SET_CODE;

		String[] params = new String[]{setCode};
		try (Cursor rs = connection.rawQuery(query, params)) {
			ArrayList<SetMetaData> setsList = new ArrayList<>();

			while (rs.moveToNext()) {
				SetMetaData current = new SetMetaData();
				current.setName = rs.getString(0);
				current.setCode = rs.getString(1);
				current.numOfCards = rs.getInt(2);
				current.tcgDate = rs.getString(3);

				setsList.add(current);
			}

			return setsList;
		}
	}

	@Override
	public ArrayList<SetMetaData> getAllSetMetaDataFromSetData() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_ALL_SET_META_DATA_FROM_SET_DATA;

		try (Cursor rs = connection.rawQuery(query, null)) {
			ArrayList<SetMetaData> setsList = new ArrayList<>();

			while (rs.moveToNext()) {
				SetMetaData current = new SetMetaData();
				current.setName = rs.getString(0);
				current.setCode = rs.getString(1);
				current.numOfCards = rs.getInt(2);
				current.tcgDate = rs.getString(3);

				setsList.add(current);
			}

			return setsList;
		}
	}

	@Override
	public HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_CARDS_ONLY_PRINTED_ONCE;

		try (Cursor rs = connection.rawQuery(query, null)) {
			String[] col = rs.getColumnNames();

			HashMap<String, AnalyzePrintedOnceData> setsList = new HashMap<>();

			while (rs.moveToNext()) {
				String gamePlayCardUUID = rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID));
				String cardName = rs.getString(getColumn(col, Const.CARD_NAME));
				String type = rs.getString(getColumn(col, Const.TYPE));
				String setNumber = rs.getString(getColumn(col, Const.SET_NUMBER));
				String setRarity = rs.getString(getColumn(col, Const.SET_RARITY));
				String setName = rs.getString(getColumn(col, Const.SET_NAME));
				String releaseDate = rs.getString(getColumn(col, Const.RELEASE_DATE));
				String archetype = rs.getString(getColumn(col, Const.ARCHETYPE));

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

			return setsList;
		}
	}

	@Override
	public void replaceIntoCardSetMetaData(String setName, String setCode, int numOfCards, String tcgDate) {
		SQLiteDatabase connection = this.getInstance();

		String cardSets = SQLConst.REPLACE_INTO_CARD_SET_META_DATA;

		try (SQLiteStatement statementInsertSets = connection.compileStatement(cardSets)) {
			statementInsertSets.bindString(1, setName);
			statementInsertSets.bindString(2, setCode);
			statementInsertSets.bindLong(3, numOfCards);
			statementInsertSets.bindString(4, tcgDate);

			statementInsertSets.execute();
		}
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

		String gamePlayCard = SQLConst.GET_GAME_PLAY_CARD_BY_UUID;
		String[] params = new String[]{gamePlayCardUUID};

		try (Cursor rs = connection.rawQuery(gamePlayCard, params)) {
			String[] col = rs.getColumnNames();

			GamePlayCard current = new GamePlayCard();

			if (!rs.moveToNext()) {
				return null;
			}

			getAllGamePlayCardFieldsFromRS(rs, col, current);

			return current;
		}
	}

	private void getAllGamePlayCardFieldsFromRS(Cursor rs, String[] col, GamePlayCard current) {
		current.gamePlayCardUUID = rs.getString(getColumn(col,Const.GAME_PLAY_CARD_UUID));
		current.cardName = rs.getString(getColumn(col,Const.GAME_PLAY_CARD_NAME));
		current.cardType = rs.getString(getColumn(col,Const.TYPE));
		current.passcode = rs.getInt(getColumn(col,Const.PASSCODE));
		current.desc = rs.getString(getColumn(col,Const.GAME_PLAY_CARD_TEXT));
		current.attribute = rs.getString(getColumn(col,Const.ATTRIBUTE));
		current.race = rs.getString(getColumn(col,Const.RACE));
		current.linkval = rs.getString(getColumn(col,Const.LINK_VALUE));
		current.level = rs.getString(getColumn(col,Const.LEVEL_RANK));
		current.scale = rs.getString(getColumn(col,Const.PENDULUM_SCALE));
		current.atk = rs.getString(getColumn(col,Const.ATTACK));
		current.def = rs.getString(getColumn(col,Const.DEFENSE));
		current.archetype = rs.getString(getColumn(col,Const.ARCHETYPE));
		current.modificationDate = rs.getString(getColumn(col, Const.MODIFICATION_DATE));
	}

	@Override
	public List<GamePlayCard> getAllGamePlayCard() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceIntoGamePlayCard(GamePlayCard input) {
		SQLiteDatabase connection = this.getInstance();

		String gamePlayCard = SQLConst.REPLACE_INTO_GAME_PLAY_CARD;

		try (SQLiteStatement statement = connection.compileStatement(gamePlayCard)) {
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
		}
	}

	@Override
	public void updateOwnedCardByUUID(OwnedCard card) {
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

		SQLiteDatabase connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = SQLConst.UPDATE_OWNED_CARD_BY_UUID;

		try (SQLiteStatement statement = connection.compileStatement(ownedInsert)) {
			setStringOrNull(statement, 1, gamePlayCardUUID);
			setStringOrNull(statement, 2, folder);
			setStringOrNull(statement, 3, name);
			setIntegerOrNull(statement, 4, quantity);
			setStringOrNull(statement, 5, setCode);
			setStringOrNull(statement, 6, setNumber);
			setStringOrNull(statement, 7, setName);
			setStringOrNull(statement, 8, setRarity);
			setStringOrNull(statement, 9, colorVariant);
			setStringOrNull(statement, 10, condition);
			setStringOrNull(statement, 11, printing);
			setStringOrNull(statement, 12, dateBought);
			setStringOrNull(statement, 13, normalizedPrice);
			setIntegerOrNull(statement, 14, rarityUnsure);
			setIntegerOrNull(statement, 15, passcode);
			setStringOrNull(statement, 16, uuid);

			statement.execute();
		}
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
			String ownedDelete = SQLConst.DELETE_FROM_OWNED_CARDS_WHERE_UUID;

			try (SQLiteStatement statement = connection.compileStatement(ownedDelete)) {
				setStringOrNull(statement, 1, card.uuid);
				statement.execute();
			}
		} else {
			// Update the ownedCards table
			String ownedInsert = SQLConst.UPDATE_OWNED_CARDS_SET_QUANTITY_WHERE_UUID;

			try (SQLiteStatement statement = connection.compileStatement(ownedInsert)) {
				setIntegerOrNull(statement, 1, newQuantity);
				setStringOrNull(statement, 2, card.uuid);
				statement.execute();
			}
		}

		// Insert a corresponding entry into the soldCards table
		String soldInsert = SQLConst.INSERT_INTO_SOLD_CARDS;

		try (SQLiteStatement statement = connection.compileStatement(soldInsert)) {
			setStringOrNull(statement, 1, card.gamePlayCardUUID);
			setStringOrNull(statement, 2, card.cardName);
			setIntegerOrNull(statement, 3, quantity);
			setStringOrNull(statement, 4, card.setCode);
			setStringOrNull(statement, 5, card.setNumber);
			setStringOrNull(statement, 6, card.setName);
			setStringOrNull(statement, 7, card.setRarity);
			setStringOrNull(statement, 8, card.colorVariant);
			setStringOrNull(statement, 9, card.condition);
			setStringOrNull(statement, 10, card.editionPrinting);
			setStringOrNull(statement, 11, card.dateBought);
			setStringOrNull(statement, 12, card.priceBought);
			setStringOrNull(statement, 13, sdf.format(new Date()));
			setStringOrNull(statement, 14, priceSold);
			setStringOrNull(statement, 15, card.uuid);
			setStringOrNull(statement, 16, card.creationDate);
			setIntegerOrNull(statement, 17, card.passcode);
			statement.execute();
		}
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
		String uuid = card.uuid;
		int passcode = card.passcode;

		if (uuid == null || uuid.equals("")) {
			uuid = java.util.UUID.randomUUID().toString();
		}

		SQLiteDatabase connection = this.getInstance();

		if (rarityUnsure != 1) {
			rarityUnsure = 0;
		}

		if (colorVariant == null) {
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		String normalizedPrice = Util.normalizePrice(priceBought);

		String ownedInsert = SQLConst.UPSERT_OWNED_CARD_BATCH;

		try (SQLiteStatement batchUpsertOwnedCard = connection.compileStatement(ownedInsert)) {
			setStringOrNull(batchUpsertOwnedCard, 1, gamePlayCardUUID);
			setStringOrNull(batchUpsertOwnedCard, 2, folder);
			setStringOrNull(batchUpsertOwnedCard, 3, name);
			setIntegerOrNull(batchUpsertOwnedCard, 4, quantity);
			setStringOrNull(batchUpsertOwnedCard, 5, setCode);
			setStringOrNull(batchUpsertOwnedCard, 6, setNumber);
			setStringOrNull(batchUpsertOwnedCard, 7, setName);
			setStringOrNull(batchUpsertOwnedCard, 8, setRarity);
			setStringOrNull(batchUpsertOwnedCard, 9, colorVariant);
			setStringOrNull(batchUpsertOwnedCard, 10, condition);
			setStringOrNull(batchUpsertOwnedCard, 11, printing);
			setStringOrNull(batchUpsertOwnedCard, 12, dateBought);
			setStringOrNull(batchUpsertOwnedCard, 13, normalizedPrice);
			setIntegerOrNull(batchUpsertOwnedCard, 14, rarityUnsure);
			setStringOrNull(batchUpsertOwnedCard, 15, uuid);
			setIntegerOrNull(batchUpsertOwnedCard, 16, passcode);
			// conflict fields
			setIntegerOrNull(batchUpsertOwnedCard, 17, quantity);
			setIntegerOrNull(batchUpsertOwnedCard, 18, rarityUnsure);
			setStringOrNull(batchUpsertOwnedCard, 19, setRarity);
			setStringOrNull(batchUpsertOwnedCard, 20, colorVariant);
			setStringOrNull(batchUpsertOwnedCard, 21, uuid);

			batchUpsertOwnedCard.execute();
		}
	}

	@Override
	public void replaceIntoCardSetWithSoftPriceUpdate(String setNumber, String rarity, String setName, String gamePlayCardUUID, String price,
													  String cardName) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String setInsert = SQLConst.REPLACE_INTO_CARD_SET_WITH_SOFT_PRICE_UPDATE;

		try (SQLiteStatement statementSetInsert = connection.compileStatement(setInsert)) {
			statementSetInsert.bindString(1, gamePlayCardUUID);
			statementSetInsert.bindString(2, setNumber);
			statementSetInsert.bindString(3, setName);
			statementSetInsert.bindString(4, rarity);
			statementSetInsert.bindString(5, cardName);

			if (price != null && !Util.normalizePrice(price).equals(Util.normalizePrice("0"))) {
				List<CardSet> list = getCardSetsForValues(setNumber, rarity, setName);

				if (!list.isEmpty() && list.get(0).setPrice != null) {
					updateCardSetPriceWithSetName(setNumber, rarity, price, setName);
				}
			}

			statementSetInsert.execute();
		}
	}

	@Override
	public void updateSetName(String original, String newName) {
		SQLiteDatabase connection = this.getInstance();

		String setInsert = SQLConst.UPDATE_CARD_SETS_SET_NAME;

		try (SQLiteStatement statementSetInsert = connection.compileStatement(setInsert)) {
			statementSetInsert.bindString(1, newName);
			statementSetInsert.bindString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update cardSets for " + original);
		}

		setInsert = SQLConst.UPDATE_OWNED_CARDS_SET_NAME;

		try (SQLiteStatement statementSetInsert = connection.compileStatement(setInsert)) {
			statementSetInsert.bindString(1, newName);
			statementSetInsert.bindString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update ownedCards for " + original);
		}

		setInsert = SQLConst.UPDATE_SET_DATA_SET_NAME;

		try (SQLiteStatement statementSetInsert = connection.compileStatement(setInsert)) {
			statementSetInsert.bindString(1, newName);
			statementSetInsert.bindString(2, original);

			statementSetInsert.execute();
		} catch (Exception e) {
			YGOLogger.error("Unable to update set data for " + original);
		}
	}

	@Override
	public int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_RARITY;

		try (SQLiteStatement statement = connection.compileStatement(update)) {
			statement.bindString(1, price);
			statement.bindString(2, setNumber);
			statement.bindString(3, rarity);

			return statement.executeUpdateDelete();
		}
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE_WITH_SET_NAME;

		try (SQLiteStatement statement = connection.compileStatement(update)) {
			statement.bindString(1, price);
			statement.bindString(2, setNumber);
			statement.bindString(3, rarity);
			statement.bindString(4, setName);

			return statement.executeUpdateDelete();
		}
	}

	@Override
	public int getUpdatedRowCount() {
		SQLiteDatabase connection = this.getInstance();

		String query = SQLConst.GET_UPDATED_ROW_COUNT;

		try (Cursor rs = connection.rawQuery(query, null)) {
			rs.moveToNext();
			return rs.getInt(1);
		}
	}

	@Override
	public int updateCardSetPrice(String setNumber, String price) throws SQLException {
		SQLiteDatabase connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE;

		try (SQLiteStatement statement = connection.compileStatement(update)) {
			statement.bindString(1, price);
			statement.bindString(2, setNumber);
			return statement.executeUpdateDelete();
		}
	}
}
