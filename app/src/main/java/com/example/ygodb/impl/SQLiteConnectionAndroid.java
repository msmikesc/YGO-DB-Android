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
	private boolean upgradeDatabase = false;

	public SQLiteConnectionAndroid() {
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
		} else if (this.upgradeDatabase) {
			//TODO implement anything needed here
		}

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

		try (InputStream myInput = cont.getAssets().open(DB_FILE_PATH);
			 OutputStream myOutput = new FileOutputStream(new File(cont.getFilesDir(), DB_NAME))) {

			/*
			 * Copy over the empty db in internal storage with the database in the
			 * assets folder.
			 */
			FileHelper.copyFile(myInput, myOutput);
		} catch (Exception e) {
			throw new IOException(e);
		}

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	public void copyDataBaseFromURI(InputStream myInput) throws IOException {
		if (myInput == null) {
			return;
		}

		close();

		/*
		 * Open the empty db in internal storage as the output stream.
		 */
		File output = new File(cont.getFilesDir(), DB_NAME);
		try {
			File parent = output.getParentFile();

			if (parent == null || (!parent.exists() && !parent.mkdirs())) {
				YGOLogger.error("Unable to create folder in copyDataBaseFromURI");
			}
		} catch (Exception e) {
			throw new IOException(e);
		}

		try (OutputStream myOutput = new FileOutputStream(output)) {
			/*
			 * Copy over the empty db in internal storage with the database in the
			 * assets folder.
			 */
			FileHelper.copyFile(myInput, myOutput);
		} catch (Exception e) {
			throw new IOException(e);
		}

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();
	}

	public void copyDataBaseToURI(OutputStream output) throws IOException {

		if (output == null) {
			return;
		}

		close();

		/*
		 * Open the database in the internal folder as the input stream.
		 */
		File input = new File(cont.getFilesDir(), DB_NAME);
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

	private int getColumn(String[] col, String columnName) {
		for (int i = 0; i < col.length; i++) {
			if (col[i].equals(columnName)) {
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

				ArrayList<CardSet> currentList = results.computeIfAbsent(set.getSetNumber(), k -> new ArrayList<>());

				currentList.add(set);
			}

			return results;
		}
	}

	private void getAllCardSetFieldsFromRS(Cursor rs, String[] col, CardSet set) {
		set.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
		set.setCardName(rs.getString(getColumn(col, Const.CARD_NAME)));
		set.setSetNumber(rs.getString(getColumn(col, Const.SET_NUMBER)));
		set.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
		set.setSetRarity(rs.getString(getColumn(col, Const.SET_RARITY)));
		set.setSetPrice(Util.normalizePrice(rs.getString(getColumn(col, Const.SET_PRICE))));
		set.setSetPriceUpdateTime(rs.getString(getColumn(col, Const.SET_PRICE_UPDATE_TIME)));
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) {
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
				set.setCardType(rs.getString(getColumn(col, Const.TYPE)));

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
				set.setCardType(rs.getString(getColumn(col, Const.TYPE)));

				results.add(set);
			}

			return results;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] columns = new String[]{"a.gamePlayCardUUID", "a.cardName as cardNameCol", "a.setNumber as setNumberCol", "a.setName",
				"a.setRarity as setRarityCol", "a.setPrice", "sum(b.quantity) as quantity",
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
				current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
				current.setCardName(rs.getString(getColumn(col, "cardNameCol")));
				current.setSetNumber(rs.getString(getColumn(col, "setNumberCol")));
				current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
				current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
				current.setSetRarity(rs.getString(getColumn(col, "setRarityCol")));
				current.setPriceBought(Util.normalizePrice(rs.getString(getColumn(col, Const.SET_PRICE))));
				current.setQuantity(rs.getInt(getColumn(col, Const.QUANTITY)));
				current.setDateBought(rs.getString(getColumn(col, "maxDate")));
				current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));

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
	public ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) {

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
	public String getGamePlayCardUUIDFromPasscode(int passcode) {

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
				current.setGamePlayCardUUID(rs.getString(5));
				current.setQuantity(rs.getInt(0));
				current.setCardName(rs.getString(1));
				current.setSetName(rs.getString(2));
				current.setDateBought(rs.getString(3));
				current.setPriceBought(rs.getString(4));
				cardsInSetList.add(current);
			}

			return cardsInSetList;
		}
	}

	@Override
	public ArrayList<OwnedCard> getAllOwnedCards() {
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

		String[] columns = new String[]{Const.GAME_PLAY_CARD_UUID, Const.RARITY_UNSURE, Const.QUANTITY, Const.CARD_NAME, Const.SET_CODE,
				Const.SET_NUMBER, Const.SET_NAME, Const.SET_RARITY, Const.SET_RARITY_COLOR_VARIANT, Const.FOLDER_NAME, Const.CONDITION,
				Const.EDITION_PRINTING, Const.DATE_BOUGHT, Const.PRICE_BOUGHT, Const.CREATION_DATE, Const.MODIFICATION_DATE,
				Const.UUID, Const.PASSCODE};

		//PRIMARY KEY(Const.gamePlayCardUUID,Const.folderName,Const.setNumber,Const.setRarity,Const.setRarityColorVariant,
		// Const.condition,Const.editionPrinting,Const.dateBought,Const.priceBought)


		if (query.getFolderName() == null || query.getSetNumber() == null ||
				query.getSetRarity() == null || query.getColorVariant() == null || query.getCondition() == null ||
				query.getEditionPrinting() == null || query.getDateBought() == null || query.getPriceBought() == null) {
			return null;
		}

		String selection = "gamePlayCardUUID = ? AND folderName = ? AND setNumber = ? AND setRarity = ? AND " +
				"setRarityColorVariant = ? AND condition = ? AND editionPrinting = ? AND " +
				"dateBought = ? AND priceBought = ?";
		String[] selectionArgs = new String[]{query.getGamePlayCardUUID(), query.getFolderName(), query.getSetNumber(), query.getSetRarity(),
				query.getColorVariant(), query.getCondition(), query.getEditionPrinting(), query.getDateBought(), query.getPriceBought()};

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs,
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

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs,
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
		current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
		current.setQuantity(rs.getInt(getColumn(col, Const.QUANTITY)));
		current.setCardName(rs.getString(getColumn(col, Const.CARD_NAME)));
		current.setSetNumber(rs.getString(getColumn(col, Const.SET_NUMBER)));
		current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
		current.setSetRarity(rs.getString(getColumn(col, Const.SET_RARITY)));
		current.setColorVariant(rs.getString(getColumn(col, Const.SET_RARITY_COLOR_VARIANT)));
		current.setEditionPrinting(rs.getString(getColumn(col, Const.EDITION_PRINTING)));
		current.setDateBought(rs.getString(getColumn(col, Const.DATE_BOUGHT)));
		current.setPriceBought(rs.getString(getColumn(col, Const.PRICE_BOUGHT)));
		current.setUuid(rs.getString(getColumn(col, Const.UUID)));
		current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
		current.setFolderName(rs.getString(getColumn(col, Const.FOLDER_NAME)));
		current.setRarityUnsure(rs.getInt(getColumn(col, Const.RARITY_UNSURE)));
		current.setCondition(rs.getString(getColumn(col, Const.CONDITION)));
		current.setCreationDate(rs.getString(getColumn(col, Const.CREATION_DATE)));
		current.setModificationDate(rs.getString(getColumn(col, Const.MODIFICATION_DATE)));
		current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));

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

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs,
				Const.CARD_NAME, null, orderBy, offset + "," + limit)) {

			ArrayList<OwnedCard> cardsInSetList = new ArrayList<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				current.setGamePlayCardUUID(rs.getString(0));
				current.setQuantity(rs.getInt(1));
				current.setCardName(rs.getString(2));
				current.setSetName(rs.getString(3));
				current.setDateBought(rs.getString(4));
				current.setPriceBought(rs.getString(5));
				current.setSetRarity(rs.getString(6));
				current.setPasscode(rs.getInt(7));
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
	public ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() {
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
				String key = current.getSetNumber() + current.getPriceBought() + current.getDateBought() + current.getFolderName()
						+ current.getCondition() + current.getEditionPrinting();
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
				current.setSetName(rs.getString(0));
				current.setSetCode(rs.getString(1));
				current.setNumOfCards(rs.getInt(2));
				current.setTcgDate(rs.getString(3));

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
				current.setSetName(rs.getString(0));
				current.setSetCode(rs.getString(1));
				current.setNumOfCards(rs.getInt(2));
				current.setTcgDate(rs.getString(3));

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
				current.setSetName(rs.getString(0));
				current.setSetCode(rs.getString(1));
				current.setNumOfCards(rs.getInt(2));
				current.setTcgDate(rs.getString(3));

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
		current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
		current.setCardName(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_NAME)));
		current.setCardType(rs.getString(getColumn(col, Const.TYPE)));
		current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));
		current.setDesc(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_TEXT)));
		current.setAttribute(rs.getString(getColumn(col, Const.ATTRIBUTE)));
		current.setRace(rs.getString(getColumn(col, Const.RACE)));
		current.setLinkVal(rs.getString(getColumn(col, Const.LINK_VALUE)));
		current.setLevel(rs.getString(getColumn(col, Const.LEVEL_RANK)));
		current.setScale(rs.getString(getColumn(col, Const.PENDULUM_SCALE)));
		current.setAtk(rs.getString(getColumn(col, Const.ATTACK)));
		current.setDef(rs.getString(getColumn(col, Const.DEFENSE)));
		current.setArchetype(rs.getString(getColumn(col, Const.ARCHETYPE)));
		current.setModificationDate(rs.getString(getColumn(col, Const.MODIFICATION_DATE)));
	}

	@Override
	public List<GamePlayCard> getAllGamePlayCard() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void replaceIntoGamePlayCard(GamePlayCard input) {
		SQLiteDatabase connection = this.getInstance();

		String gamePlayCard = SQLConst.REPLACE_INTO_GAME_PLAY_CARD;

		try (SQLiteStatement statement = connection.compileStatement(gamePlayCard)) {
			setStringOrNull(statement, 1, input.getGamePlayCardUUID());
			setStringOrNull(statement, 2, input.getCardName());
			setStringOrNull(statement, 3, input.getCardType());
			setIntegerOrNull(statement, 4, input.getPasscode());
			setStringOrNull(statement, 5, input.getDesc());
			setStringOrNull(statement, 6, input.getAttribute());
			setStringOrNull(statement, 7, input.getRace());
			setStringOrNull(statement, 8, input.getLinkVal());
			setStringOrNull(statement, 9, input.getLevel());
			setStringOrNull(statement, 10, input.getScale());
			setStringOrNull(statement, 11, input.getAtk());
			setStringOrNull(statement, 12, input.getDef());
			setStringOrNull(statement, 13, input.getArchetype());

			statement.execute();
		}
	}

	@Override
	public void updateOwnedCardByUUID(OwnedCard card) {
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
		int newQuantity = card.getQuantity() - quantity;
		if (newQuantity < 0) {
			newQuantity = 0;
		}

		// If the quantity reaches 0, remove the entry from the ownedCards table
		if (newQuantity == 0) {
			String ownedDelete = SQLConst.DELETE_FROM_OWNED_CARDS_WHERE_UUID;

			try (SQLiteStatement statement = connection.compileStatement(ownedDelete)) {
				setStringOrNull(statement, 1, card.getUuid());
				statement.execute();
			}
		} else {
			// Update the ownedCards table
			String ownedInsert = SQLConst.UPDATE_OWNED_CARDS_SET_QUANTITY_WHERE_UUID;

			try (SQLiteStatement statement = connection.compileStatement(ownedInsert)) {
				setIntegerOrNull(statement, 1, newQuantity);
				setStringOrNull(statement, 2, card.getUuid());
				statement.execute();
			}
		}

		// Insert a corresponding entry into the soldCards table
		String soldInsert = SQLConst.INSERT_INTO_SOLD_CARDS;

		try (SQLiteStatement statement = connection.compileStatement(soldInsert)) {
			setStringOrNull(statement, 1, card.getGamePlayCardUUID());
			setStringOrNull(statement, 2, card.getCardName());
			setIntegerOrNull(statement, 3, quantity);
			setStringOrNull(statement, 4, card.getSetCode());
			setStringOrNull(statement, 5, card.getSetNumber());
			setStringOrNull(statement, 6, card.getSetName());
			setStringOrNull(statement, 7, card.getSetRarity());
			setStringOrNull(statement, 8, card.getColorVariant());
			setStringOrNull(statement, 9, card.getCondition());
			setStringOrNull(statement, 10, card.getEditionPrinting());
			setStringOrNull(statement, 11, card.getDateBought());
			setStringOrNull(statement, 12, card.getPriceBought());
			setStringOrNull(statement, 13, sdf.format(new Date()));
			setStringOrNull(statement, 14, priceSold);
			setStringOrNull(statement, 15, card.getUuid());
			setStringOrNull(statement, 16, card.getCreationDate());
			setIntegerOrNull(statement, 17, card.getPasscode());
			statement.execute();
		}
	}

	@Override
	public void upsertOwnedCardBatch(OwnedCard card) {
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
													  String cardName) {
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

				if (!list.isEmpty() && list.get(0).getSetPrice() != null) {
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
	public int updateCardSetPrice(String setNumber, String rarity, String price) {
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
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName) {
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
	public int updateCardSetPrice(String setNumber, String price) {
		SQLiteDatabase connection = this.getInstance();

		String update = SQLConst.UPDATE_CARD_SET_PRICE;

		try (SQLiteStatement statement = connection.compileStatement(update)) {
			statement.bindString(1, price);
			statement.bindString(2, setNumber);
			return statement.executeUpdateDelete();
		}
	}
}
