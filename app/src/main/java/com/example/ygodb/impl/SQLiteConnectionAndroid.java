package com.example.ygodb.impl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import com.example.ygodb.abs.AndroidUtil;
import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.connection.CommonDatabaseQueries;
import ygodb.commonlibrary.connection.DatabaseHashMap;
import ygodb.commonlibrary.connection.DatabaseSelectQuery;
import ygodb.commonlibrary.connection.DatabaseUpdateQuery;
import ygodb.commonlibrary.connection.FileHelper;
import ygodb.commonlibrary.connection.PreparedStatementBatchWrapper;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.connection.SelectQueryResultMapper;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.constant.SQLConst;
import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SQLiteConnectionAndroid extends SQLiteOpenHelper implements SQLiteConnection {

	public static final int BATCH_SIZE = 1000;
	private SQLiteDatabase connectionInstance = null;
	private static final String DB_NAME = "database.sqlite";

	private static final String DB_FILE_PATH = "database/YGO-DB.db";

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	private boolean createDatabase = false;
	private boolean upgradeDatabase = false;

	public File getDatabaseFileReference() {
		return new File(AndroidUtil.getAppContext().getFilesDir(), DB_NAME);
	}

	public SQLiteConnectionAndroid() {
		super(AndroidUtil.getAppContext(),
			  AndroidUtil.getAppContext().getFilesDir().getAbsolutePath() + "/" + SQLiteConnectionAndroid.DB_NAME, null, 1);
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

		try (InputStream myInput = AndroidUtil.getAppContext().getAssets().open(DB_FILE_PATH);
			 OutputStream myOutput = new FileOutputStream(new File(AndroidUtil.getAppContext().getFilesDir(), DB_NAME))) {

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

	public String copyDataBaseFromURI(Activity activity, Uri myInput) throws IOException {
		if (myInput == null) {
			return "Uri input was null";
		}

		close();

		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);

		String prevLoadedDbHash = prefs.getString(Const.LOADED_DB_HASH, null);

		InputStream fileInputStream = activity.getContentResolver().openInputStream(myInput);

		writeURItoDB(fileInputStream);

		fileInputStream.close();

		String newFileHash = null;
		try {
			newFileHash = FileHelper.getFileHash(getDatabaseFileReference());
		} catch (Exception e) {
			throw new IOException(e);
		}

		SharedPreferences.Editor editor = prefs.edit();
		String response;

		if (prevLoadedDbHash == null || prevLoadedDbHash.isBlank() || !prevLoadedDbHash.equals(newFileHash)) {
			editor.putString(Const.LOADED_DB_HASH, newFileHash);
			editor.apply();
			response = "DB file loaded on first try";
		} else {
			//loaded same file, try again
			fileInputStream = activity.getContentResolver().openInputStream(myInput);

			writeURItoDB(fileInputStream);

			fileInputStream.close();

			try {
				newFileHash = FileHelper.getFileHash(getDatabaseFileReference());
			} catch (Exception e) {
				throw new IOException(e);
			}

			if (prevLoadedDbHash.equals(newFileHash)) {
				//same file loaded twice
				response = "DB file hash the same on second try";
			} else {
				//file loaded successfully on second try
				editor.putString(Const.LOADED_DB_HASH, newFileHash);
				editor.apply();
				response = "DB file loaded on second try";
			}
		}

		/*
		 * Access the copied database so SQLiteHelper will cache it and mark it
		 * as created.
		 */
		getWritableDatabase().close();

		return response;
	}

	private void writeURItoDB(InputStream myInput) throws IOException {

		/*
		 * Open the empty db in internal storage as the output stream.
		 */
		File output = getDatabaseFileReference();
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
	}

	public void copyDataBaseToURI(Activity activity, OutputStream output) throws IOException {

		if (output == null) {
			return;
		}

		close();

		/*
		 * Open the database in the internal folder as the input stream.
		 */
		File input = getDatabaseFileReference();
		InputStream myInput = new FileInputStream(input);


		/*
		 * Copy over the empty db in internal storage with the database in the
		 * assets folder.
		 */
		FileHelper.copyFile(myInput, output);

		myInput.close();

		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		String newFileHash;
		try {
			newFileHash = FileHelper.getFileHash(getDatabaseFileReference());
		} catch (Exception e) {
			throw new IOException(e);
		}
		editor.putString(Const.LOADED_DB_HASH, newFileHash);
		editor.apply();

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

		if (connectionInstance.inTransaction()) {
			connectionInstance.setTransactionSuccessful();
			connectionInstance.endTransaction();
		}

		connectionInstance.close();

		connectionInstance = null;
	}

	private static int getColumn(String[] col, String columnName) {
		for (int i = 0; i < col.length; i++) {
			if (col[i].equals(columnName)) {
				return i;
			}
		}
		return -1;
	}

	public static class OwnedCardMapperSelectQuery implements SelectQueryResultMapper<OwnedCard, Cursor> {
		@Override
		public OwnedCard mapRow(Cursor resultSet) throws SQLException {
			OwnedCard entity = new OwnedCard();
			String[] col = resultSet.getColumnNames();
			getAllOwnedCardFieldsFromRS(resultSet, col, entity);
			return entity;
		}
	}

	private static void getAllOwnedCardFieldsFromRS(Cursor rs, String[] col, OwnedCard current) {
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

	public static class CardSetMapperSelectQuery implements SelectQueryResultMapper<CardSet, Cursor> {
		@Override
		public CardSet mapRow(Cursor resultSet) throws SQLException {
			CardSet entity = new CardSet();
			String[] col = resultSet.getColumnNames();
			getAllCardSetFieldsFromRS(resultSet, col, entity);
			return entity;
		}
	}

	private static void getAllCardSetFieldsFromRS(Cursor rs, String[] col, CardSet set) {
		set.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
		set.setCardName(rs.getString(getColumn(col, Const.CARD_NAME)));
		set.setSetNumber(rs.getString(getColumn(col, Const.SET_NUMBER)));
		set.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
		set.setSetRarity(rs.getString(getColumn(col, Const.SET_RARITY)));
		set.setSetPrice(Util.normalizePrice(rs.getString(getColumn(col, Const.SET_PRICE))));
		set.setSetPriceUpdateTime(rs.getString(getColumn(col, Const.SET_PRICE_UPDATE_TIME)));
		set.setSetPriceFirst(Util.normalizePrice(rs.getString(getColumn(col, Const.SET_PRICE_FIRST))));
		set.setSetPriceFirstUpdateTime(rs.getString(getColumn(col, Const.SET_PRICE_FIRST_UPDATE_TIME)));
		set.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
		set.setSetUrl(rs.getString(getColumn(col, Const.SET_URL)));
		set.setColorVariant(rs.getString(getColumn(col, Const.COLOR_VARIANT)));
	}

	public static class GamePlayCardMapperSelectQuery implements SelectQueryResultMapper<GamePlayCard, Cursor> {
		@Override
		public GamePlayCard mapRow(Cursor resultSet) throws SQLException {
			GamePlayCard entity = new GamePlayCard();
			String[] col = resultSet.getColumnNames();
			getAllGamePlayCardFieldsFromRS(resultSet, col, entity);
			return entity;
		}
	}

	private static void getAllGamePlayCardFieldsFromRS(Cursor rs, String[] col, GamePlayCard current) {
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

	public static class SetBoxMapperSelectQuery implements SelectQueryResultMapper<SetBox, Cursor> {
		@Override
		public SetBox mapRow(Cursor resultSet) throws SQLException {
			SetBox entity = new SetBox();
			String[] col = resultSet.getColumnNames();
			getAllSetBoxesFieldsFromRS(resultSet, col, entity);
			return entity;
		}
	}

	private static void getAllSetBoxesFieldsFromRS(Cursor rs, String[] col, SetBox current) {
		current.setBoxLabel(rs.getString(getColumn(col, Const.BOX_LABEL)));
		current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
		current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
	}

	public static class GamePlayCardNameMapperSelectQuery implements SelectQueryResultMapper<String, Cursor> {
		@Override
		public String mapRow(Cursor resultSet) throws SQLException {
			String[] col = resultSet.getColumnNames();
			return resultSet.getString(getColumn(col, Const.GAME_PLAY_CARD_NAME));
		}
	}

	public static class GamePlayCardUUIDMapperSelectQuery implements SelectQueryResultMapper<String, Cursor> {
		@Override
		public String mapRow(Cursor resultSet) throws SQLException {
			String[] col = resultSet.getColumnNames();
			return resultSet.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID));
		}
	}

	public static class SetNumberMapperSelectQuery implements SelectQueryResultMapper<String, Cursor> {
		@Override
		public String mapRow(Cursor resultSet) throws SQLException {
			String[] col = resultSet.getColumnNames();
			return resultSet.getString(getColumn(col, Const.SET_NUMBER));
		}
	}

	public static class SetNameMapperSelectQuery implements SelectQueryResultMapper<String, Cursor> {
		@Override
		public String mapRow(Cursor resultSet) throws SQLException {
			String[] col = resultSet.getColumnNames();
			return resultSet.getString(getColumn(col, Const.SET_NAME));
		}
	}

	public static class FirstIntMapperSelectQuery implements SelectQueryResultMapper<Integer, Cursor> {
		@Override
		public Integer mapRow(Cursor resultSet) throws SQLException {
			return resultSet.getInt(0);
		}
	}

	@Override
	public Map<String, List<CardSet>> getAllCardRaritiesForHashMap() {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_CARD_RARITIES;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {

			HashMap<String, List<CardSet>> results = new HashMap<>(300000, 0.75f);

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				CardSet set = new CardSet();
				getAllCardSetFieldsFromRS(rs, col, set);

				List<String> keysList = DatabaseHashMap.getCardRarityKeys(set);

				for (String key : keysList) {
					if (key != null && !key.isBlank()) {
						List<CardSet> currentList = results.computeIfAbsent(key, k -> new ArrayList<>());
						currentList.add(set);
					}
				}
			}

			return results;
		}
	}

	@Override
	public Map<String, List<GamePlayCard>> getAllGamePlayCardsForHashMap() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_GAME_PLAY_CARD;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {

			HashMap<String, List<GamePlayCard>> cardMap = new HashMap<>();

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				GamePlayCard card = new GamePlayCard();
				getAllGamePlayCardFieldsFromRS(rs, col, card);

				List<String> keysList = DatabaseHashMap.getGamePlayCardKeys(card);

				for (String key : keysList) {
					List<GamePlayCard> currentList = cardMap.computeIfAbsent(key, k -> new ArrayList<>());
					currentList.add(card);
				}
			}
			return cardMap;
		}
	}


	@Override
	public List<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {

		DatabaseSelectQuery<CardSet, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getRaritiesOfCardByGamePlayCardUUID(gamePlayCardUUID, query, new CardSetMapperSelectQuery());
	}

	@Override
	public List<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) throws SQLException {
		DatabaseSelectQuery<CardSet, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getRaritiesOfCardInSetByGamePlayCardUUID(gamePlayCardUUID, setName, query,
																			  new CardSetMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] columns = new String[]{"a.gamePlayCardUUID", "a.cardName as cardNameCol", "a.setNumber as setNumberCol", "a.setName",
				"a.setRarity as setRarityCol", "a.setPrice", "a.setPriceFirst", "sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate, c.setCode", "d.passcode, colorVariant"};

		String selection = "a.cardName like ?";
		String[] selectionArgs = new String[]{'%' + cardName.trim() + '%'};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol, colorVariant";

		try (Cursor rs = connection.query(
				"cardSets a left outer join ownedCards b on a.gamePlayCardUUID = b.gamePlayCardUUID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity and a.colorVariant = b.setRarityColorVariant " +
						"left outer join setData c on a.setName = c.setName left outer join gamePlayCard d " +
						"on a.gamePlayCardUUID = d.gamePlayCardUUID", columns, selection, selectionArgs, groupBy, null, orderBy, null)) {

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
				current.setCardName(rs.getString(getColumn(col, "cardNameCol")));
				current.setSetNumber(rs.getString(getColumn(col, "setNumberCol")));
				current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
				current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
				current.setSetRarity(rs.getString(getColumn(col, "setRarityCol")));

				String lowestPrice = Util.getLowestPriceString(rs.getString(getColumn(col, Const.SET_PRICE)),
															   rs.getString(getColumn(col, Const.SET_PRICE_FIRST)));

				current.setPriceBought(Util.normalizePrice(lowestPrice));

				current.setQuantity(rs.getInt(getColumn(col, Const.QUANTITY)));
				current.setDateBought(rs.getString(getColumn(col, "maxDate")));
				current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));
				current.setColorVariant(rs.getString(getColumn(col, Const.COLOR_VARIANT)));

				results.add(current);
			}

			return results;
		}
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsBySetName(String setName, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] columns = new String[]{"a.gamePlayCardUUID", "a.cardName as cardNameCol", "a.setNumber as setNumberCol", "a.setName",
				"a.setRarity as setRarityCol", "a.setPrice", "a.setPriceFirst", "sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate, c.setCode", "d.passcode, colorVariant"};

		String selection = "a.setName = ? OR UPPER(c.setCode) = UPPER(?)";
		String[] selectionArgs = new String[]{setName, setName};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol, colorVariant";

		try (Cursor rs = connection.query(
				"cardSets a left outer join ownedCards b on a.gamePlayCardUUID = b.gamePlayCardUUID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity and a.colorVariant = b.setRarityColorVariant " +
						"left outer join setData c on a.setName = c.setName left outer join gamePlayCard d " +
						"on a.gamePlayCardUUID = d.gamePlayCardUUID", columns, selection, selectionArgs, groupBy, null, orderBy, null)) {

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
				current.setCardName(rs.getString(getColumn(col, "cardNameCol")));
				current.setSetNumber(rs.getString(getColumn(col, "setNumberCol")));
				current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
				current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
				current.setSetRarity(rs.getString(getColumn(col, "setRarityCol")));

				String lowestPrice = Util.getLowestPriceString(rs.getString(getColumn(col, Const.SET_PRICE)),
															   rs.getString(getColumn(col, Const.SET_PRICE_FIRST)));

				current.setPriceBought(Util.normalizePrice(lowestPrice));

				current.setQuantity(rs.getInt(getColumn(col, Const.QUANTITY)));
				current.setDateBought(rs.getString(getColumn(col, "maxDate")));
				current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));
				current.setColorVariant(rs.getString(getColumn(col, Const.COLOR_VARIANT)));

				results.add(current);
			}

			return results;
		}
	}

	@Override
	public List<OwnedCard> getAllPossibleCardsByArchetype(String archetype, String orderBy) {

		SQLiteDatabase connection = this.getInstance();

		ArrayList<OwnedCard> results = new ArrayList<>();

		String[] columns = new String[]{"a.gamePlayCardUUID", "a.cardName as cardNameCol", "a.setNumber as setNumberCol", "a.setName",
				"a.setRarity as setRarityCol", "a.setPrice", "a.setPriceFirst", "sum(b.quantity) as quantity",
				"MAX(b.dateBought) as maxDate, c.setCode", "d.passcode, colorVariant"};

		String selection = "a.cardName like ? OR UPPER(d.archetype) = UPPER(?)";
		String[] selectionArgs = new String[]{"%" + archetype + "%", archetype};

		String groupBy = "cardNameCol, setNumberCol, setRarityCol, colorVariant";

		try (Cursor rs = connection.query(
				"cardSets a left outer join ownedCards b on a.gamePlayCardUUID = b.gamePlayCardUUID and b.cardName = a.cardName " +
						"and a.setNumber = b.setNumber and a.setRarity = b.setRarity and a.colorVariant = b.setRarityColorVariant " +
						"left outer join setData c on a.setName = c.setName left outer join gamePlayCard d " +
						"on a.gamePlayCardUUID = d.gamePlayCardUUID", columns, selection, selectionArgs, groupBy, null, orderBy, null)) {

			String[] col = rs.getColumnNames();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				current.setGamePlayCardUUID(rs.getString(getColumn(col, Const.GAME_PLAY_CARD_UUID)));
				current.setCardName(rs.getString(getColumn(col, "cardNameCol")));
				current.setSetNumber(rs.getString(getColumn(col, "setNumberCol")));
				current.setSetCode(rs.getString(getColumn(col, Const.SET_CODE)));
				current.setSetName(rs.getString(getColumn(col, Const.SET_NAME)));
				current.setSetRarity(rs.getString(getColumn(col, "setRarityCol")));

				String lowestPrice = Util.getLowestPriceString(rs.getString(getColumn(col, Const.SET_PRICE)),
															   rs.getString(getColumn(col, Const.SET_PRICE_FIRST)));

				current.setPriceBought(Util.normalizePrice(lowestPrice));

				current.setQuantity(rs.getInt(getColumn(col, Const.QUANTITY)));
				current.setDateBought(rs.getString(getColumn(col, "maxDate")));
				current.setPasscode(rs.getInt(getColumn(col, Const.PASSCODE)));
				current.setColorVariant(rs.getString(getColumn(col, Const.COLOR_VARIANT)));

				results.add(current);
			}

			return results;
		}
	}

	@Override
	public String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getCardTitleFromGamePlayCardUUID(gamePlayCardUUID, query, new GamePlayCardNameMapperSelectQuery());
	}

	@Override
	public List<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getMultipleCardNamesFromGamePlayCardUUID(gamePlayCardUUID, query,
																			  new GamePlayCardNameMapperSelectQuery());
	}

	@Override
	public String getGamePlayCardUUIDFromTitle(String title) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardUUIDFromTitle(title, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardUUIDFromPasscode(passcode, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String name) {

		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ANALYZE_DATA_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID;

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
	public List<OwnedCard> getAllOwnedCards() throws SQLException {
		DatabaseSelectQuery<OwnedCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public OwnedCard getExistingOwnedCardByObject(OwnedCard query) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns = new String[]{Const.GAME_PLAY_CARD_UUID, Const.RARITY_UNSURE, Const.QUANTITY, Const.CARD_NAME, Const.SET_CODE,
				Const.SET_NUMBER, Const.SET_NAME, Const.SET_RARITY, Const.SET_RARITY_COLOR_VARIANT, Const.FOLDER_NAME, Const.CONDITION,
				Const.EDITION_PRINTING, Const.DATE_BOUGHT, Const.PRICE_BOUGHT, Const.CREATION_DATE, Const.MODIFICATION_DATE, Const.UUID,
				Const.PASSCODE};

		//PRIMARY KEY(Const.gamePlayCardUUID,Const.folderName,Const.setNumber,Const.setRarity,Const.setRarityColorVariant,
		// Const.condition,Const.editionPrinting,Const.dateBought,Const.priceBought)


		if (query.getFolderName() == null || query.getSetNumber() == null || query.getSetRarity() == null ||
				query.getColorVariant() == null || query.getCondition() == null || query.getEditionPrinting() == null ||
				query.getDateBought() == null || query.getPriceBought() == null) {
			return null;
		}

		String selection = "gamePlayCardUUID = ? AND folderName = ? AND setNumber = ? AND setRarity = ? AND setRarityColorVariant = ?" +
				" AND condition = ? AND editionPrinting = ? AND " + "dateBought = ? AND priceBought = ? AND setName = ? AND setCode = ?";
		String[] selectionArgs =
				new String[]{query.getGamePlayCardUUID(), query.getFolderName(), query.getSetNumber(), query.getSetRarity(),
						query.getColorVariant(), query.getCondition(), query.getEditionPrinting(), query.getDateBought(),
						query.getPriceBought(), query.getSetName(), query.getSetCode()};

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs, null, null, null, null)) {

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
	public List<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns =
				new String[]{Const.GAME_PLAY_CARD_UUID, Const.QUANTITY, Const.CARD_NAME, Const.SET_NUMBER, Const.SET_NAME,
						Const.SET_RARITY,
						Const.SET_RARITY_COLOR_VARIANT, Const.EDITION_PRINTING, Const.DATE_BOUGHT, Const.PRICE_BOUGHT, Const.UUID,
						Const.SET_CODE, Const.FOLDER_NAME, Const.RARITY_UNSURE, Const.CONDITION, Const.CREATION_DATE,
						Const.MODIFICATION_DATE, Const.PASSCODE};

		String selection = null;
		String[] selectionArgs = null;

		if (cardNameSearch != null && !cardNameSearch.equals("")) {
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%" + cardNameSearch + "%"};
		}

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs, null, null, orderBy,
										  offset + "," + limit)) {

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
	public List<OwnedCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns =
				new String[]{Const.GAME_PLAY_CARD_UUID, Const.QUANTITY, Const.CARD_NAME, Const.SET_NUMBER, Const.SET_NAME,
						Const.SET_RARITY,
						Const.SET_RARITY_COLOR_VARIANT, Const.EDITION_PRINTING, Const.DATE_SOLD + " as " + Const.DATE_BOUGHT,
						Const.PRICE_SOLD + " as " + Const.PRICE_BOUGHT, Const.UUID, Const.SET_CODE, "'Sold Cards' as " + Const.FOLDER_NAME,
						"0 as " + Const.RARITY_UNSURE, Const.CONDITION, Const.CREATION_DATE, Const.MODIFICATION_DATE, Const.PASSCODE};

		String selection = null;
		String[] selectionArgs = null;

		if (cardNameSearch != null && !cardNameSearch.equals("")) {
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%" + cardNameSearch + "%"};
		}

		try (Cursor rs = connection.query("soldCards", columns, selection, selectionArgs, null, null, orderBy, offset + "," + limit)) {

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
	public List<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch) {
		SQLiteDatabase connection = this.getInstance();

		String[] columns =
				new String[]{Const.GAME_PLAY_CARD_UUID, "sum(quantity) as totalQuantity", Const.CARD_NAME, "group_concat(DISTINCT " +
						"setName)",
						"MAX(dateBought) as maxDate", "sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice",
						"group_concat(DISTINCT setRarity) as rs", Const.PASSCODE};

		String selection = null;
		String[] selectionArgs = null;

		if (cardNameSearch != null && !cardNameSearch.equals("")) {
			selection = "upper(cardName) like upper(?)";
			selectionArgs = new String[]{"%" + cardNameSearch + "%"};
		}

		try (Cursor rs = connection.query(SQLConst.OWNED_CARDS_TABLE, columns, selection, selectionArgs, Const.CARD_NAME, null, orderBy,
										  offset + "," + limit)) {

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
	public List<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException {
		DatabaseSelectQuery<OwnedCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_SET_NUMBER);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException {
		DatabaseSelectQuery<OwnedCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		query.prepareStatement(SQLConst.GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public Map<String, List<OwnedCard>> getAllOwnedCardsForHashMap() {
		SQLiteDatabase connection = this.getInstance();

		String setQuery = SQLConst.GET_ALL_OWNED_CARDS_FOR_HASH_MAP;

		try (Cursor rs = connection.rawQuery(setQuery, null)) {
			String[] col = rs.getColumnNames();

			HashMap<String, List<OwnedCard>> ownedCards = new HashMap<>();

			while (rs.moveToNext()) {
				OwnedCard current = new OwnedCard();
				getAllOwnedCardFieldsFromRS(rs, col, current);

				String key = DatabaseHashMap.getOwnedCardHashMapKey(current);

				List<OwnedCard> currentList = ownedCards.computeIfAbsent(key, k -> new ArrayList<>());
				currentList.add(current);
			}

			return ownedCards;
		}
	}

	@Override
	public List<OwnedCard> getRarityUnsureOwnedCards() throws SQLException {
		DatabaseSelectQuery<OwnedCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		query.prepareStatement(SQLConst.GET_RARITY_UNSURE_OWNED_CARDS);

		return query.executeQuery(new OwnedCardMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardUUIDsInSetByName(setName, query, new GamePlayCardUUIDMapperSelectQuery());
	}

	@Override
	public List<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardsInSetByName(setName, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public List<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getDistinctGamePlayCardsByArchetype(archetype, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public List<String> getSortedSetNumbersInSetByName(String setName) throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getSortedSetNumbersInSetByName(setName, query, new SetNumberMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctSetNames() throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getDistinctSetNames(query, new SetNameMapperSelectQuery());
	}

	@Override
	public List<String> getDistinctSetAndArchetypeNames() throws SQLException {
		DatabaseSelectQuery<String, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getDistinctSetAndArchetypeNames(query, new SetNameMapperSelectQuery());
	}

	@Override
	public int getCountDistinctCardsInSet(String setName) throws SQLException {
		DatabaseSelectQuery<Integer, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getCountDistinctCardsInSet(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public int getCountQuantity() throws SQLException {
		DatabaseSelectQuery<Integer, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getCountQuantity(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public int getCountQuantityManual() throws SQLException {
		DatabaseSelectQuery<Integer, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getCountQuantityManual(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException {
		DatabaseSelectQuery<CardSet, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getFirstCardSetForCardInSet(cardName, setName, query, new CardSetMapperSelectQuery());
	}

	@Override
	public List<SetMetaData> getSetMetaDataFromSetName(String setName) {
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
	public List<SetMetaData> getSetMetaDataFromSetCode(String setCode) {
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
	public List<SetMetaData> getAllSetMetaDataFromSetData() {
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
	public Map<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() {
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
	public GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) throws SQLException {
		DatabaseSelectQuery<GamePlayCard, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getGamePlayCardByUUID(gamePlayCardUUID, query, new GamePlayCardMapperSelectQuery());
	}

	@Override
	public int getNewLowestPasscode() throws SQLException {
		DatabaseSelectQuery<Integer, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getNewLowestPasscode(query, new FirstIntMapperSelectQuery());
	}

	@Override
	public List<SetBox> getAllSetBoxes() throws SQLException {
		DatabaseSelectQuery<SetBox, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getAllSetBoxes(query, new SetBoxMapperSelectQuery());
	}

	@Override
	public List<SetBox> getSetBoxesByNameOrCode(String searchText) throws SQLException {
		DatabaseSelectQuery<SetBox, Cursor> query = new DatabaseSelectQueryAndroid<>(getInstance());
		return CommonDatabaseQueries.getSetBoxesByNameOrCode(searchText, query, new SetBoxMapperSelectQuery());
	}

	@Override
	public int replaceIntoGamePlayCard(GamePlayCard input) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.replaceIntoGamePlayCard(query, input);
	}

	@Override
	public int replaceIntoCardSetMetaData(String setName, String setCode, int numOfCards, String tcgDate) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.replaceIntoCardSetMetaData(query, setName, setCode, numOfCards, tcgDate);
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
	public int insertOrUpdateOwnedCardByUUID(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.insertOrUpdateOwnedCardByUUID(query, card);
	}

	@Override
	public int updateOwnedCardByUUID(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateOwnedCardByUUID(query, card);
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
			setStringOrNull(statement, 15, java.util.UUID.randomUUID().toString());
			setStringOrNull(statement, 16, card.getCreationDate());
			setIntegerOrNull(statement, 17, card.getPasscode());
			statement.execute();
		}
	}

	@Override
	public int insertIntoOwnedCards(OwnedCard card) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.insertIntoOwnedCards(query, card);
	}

	@Override
	public int insertOrIgnoreIntoCardSet(String setNumber, String rarity, String setName, String gamePlayCardUUID, String cardName,
			String colorVariant, String url) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.insertOrIgnoreIntoCardSet(query, setNumber, rarity, setName, gamePlayCardUUID, cardName, colorVariant,
															   url);
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
	public int updateCardSetPrice(String setNumber, String rarity, String price, boolean isFirstEdition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetPrice(query, setNumber, rarity, price, isFirstEdition);
	}

	@Override
	public int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName, boolean isFirstEdition)
			throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithSetName(query, setNumber, rarity, price, setName, isFirstEdition);
	}

	@Override
	public int updateCardSetPriceWithCardAndSetName(String setNumber, String rarity, String price, String setName, String cardName,
			boolean isFirstEdition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithCardAndSetName(query, setNumber, rarity, price, setName, cardName,
																		  isFirstEdition);
	}

	@Override
	public int updateCardSetPriceWithCardName(String setNumber, String rarity, String price, String cardName, boolean isFirstEdition)
			throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetPriceWithCardName(query, setNumber, rarity, price, cardName, isFirstEdition);
	}

	@Override
	public int updateCardSetPrice(String setNumber, String price, boolean isFirstEdition) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetPrice(query, setNumber, price, isFirstEdition);
	}

	@Override
	public int updateCardSetUrl(String setNumber, String rarity, String setName, String cardName, String setURL, String colorVariant)
			throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetUrl(query, setNumber, rarity, setName, cardName, setURL, colorVariant);
	}

	@Override
	public int updateCardSetUrlAndColor(String setNumber, String rarity, String setName, String cardName, String setURL,
			String currentColorVariant, String newColorVariant) throws SQLException {
		DatabaseUpdateQuery query = new DatabaseUpdateQueryAndroid(getInstance());
		return CommonDatabaseQueries.updateCardSetUrlAndColor(query, setNumber, rarity, setName, cardName, setURL, currentColorVariant,
															  newColorVariant);
	}

	public PreparedStatementBatchWrapper getBatchedPreparedStatement(String input, BatchSetterAndroid setter) {
		SQLiteDatabase connection = this.getInstance();
		return new PreparedStatementBatchWrapperAndroid(connection, input, BATCH_SIZE, setter);
	}

	@Override
	public PreparedStatementBatchWrapper getBatchedPreparedStatementUrlFirst() {
		return getBatchedPreparedStatement(SQLConst.UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_FIRST, (stmt, params) -> {
			stmt.bindString(1, (String) params.get(0));
			stmt.bindString(2, (String) params.get(1));
		});
	}

	@Override
	public PreparedStatementBatchWrapper getBatchedPreparedStatementUrlUnlimited() {
		return getBatchedPreparedStatement(SQLConst.UPDATE_CARD_SET_PRICE_BATCHED_BY_URL, (stmt, params) -> {
			stmt.bindString(1, (String) params.get(0));
			stmt.bindString(2, (String) params.get(1));
		});
	}
}
