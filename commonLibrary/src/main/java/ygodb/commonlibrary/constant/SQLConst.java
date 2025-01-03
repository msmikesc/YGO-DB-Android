package ygodb.commonlibrary.constant;

public class SQLConst {

	private SQLConst() {
	}

	//So far only used in db browser
	private static final String ALT_ART_PASSCODES_TRANSLATE =
			"select distinct altArtPasscode, passcode from cardSets a join ownedCards b on a.gamePlayCardUUID = b.gamePlayCardUUID where" +
					" " + "a" + ".altArtPasscode is not null";

	public static final String OWNED_CARDS_TABLE = "ownedCards";

	public static final String OWNED_CARDS_TABLE_JOIN_CARD_SETS =
			"ownedCards a left outer join cardsets b on a.gamePlayCardUUID=b.gamePlayCardUUID and a.setNumber = b.setNumber and a" +
					".setRarity = b.setRarity and a.setRarityColorVariant = b.colorVariant and UPPER(a.setName) = UPPER(b.setName)";

	public static final String SOLD_CARDS_TABLE_JOIN_CARD_SETS =
			"soldCards a left outer join cardsets b on a.gamePlayCardUUID=b.gamePlayCardUUID and a.setNumber = b.setNumber and a" +
					".setRarity = b.setRarity and a.setRarityColorVariant = b.colorVariant and UPPER(a.setName) = UPPER(b.setName)";

	public static final String SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX =
			"Select cardSets.*, setData.setPrefix from cardSets left join setData on cardSets.setName = setData.setName";

	public static final String GET_ALL_CARD_RARITIES = SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX;
	public static final String GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID =
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX + " where gamePlayCardUUID=?";
	public static final String GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID =
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX + " where gamePlayCardUUID=? and UPPER(cardSets.setName) = UPPER(?)";
	public static final String GET_RARITIES_OF_CARD_IN_SET_BY_NUMBER_AND_RARITY = SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX +
			" where gamePlayCardUUID=? and setNumber = ? and setRarity = ? and colorVariant = ?";
	public static final String GET_RARITIES_OF_EXACT_CARD_IN_SET = SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX +
			" where gamePlayCardUUID=? and setNumber = ? and setRarity = ? and colorVariant = ? and UPPER(cardSets.setName) = UPPER(?)";
	public static final String GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID = "Select * from gamePlayCard where gamePlayCardUUID=?";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_TITLE = "Select * from gamePlayCard where UPPER(title)=UPPER(?)";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE = "Select * from gamePlayCard where passcode = ?";
	public static final String GET_PASSCODE_FROM_GAME_PLAY_CARD_UUID = "Select * from gamePlayCard where gamePlayCardUUID = ?";
	public static final String GET_ANALYZE_DATA_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID = "select sum(quantity), cardName, group_concat" +
			"(DISTINCT setName), MAX(dateBought) as maxDate, sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
			"gamePlayCardUUID from ownedCards where gamePlayCardUUID = ? group by cardName";
	public static final String GET_ALL_OWNED_CARDS = "select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_SET_PREFIX = "select * from ownedCards where setPrefix is null";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE = "select * from ownedCards where passcode < 0";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_PRICE_BOUGHT = "select * from ownedCards where priceBought = 0";
	public static final String GET_ALL_OWNED_CARDS_FOR_HASH_MAP = "select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_RARITY_UNSURE_OWNED_CARDS = "select * from ownedCards where rarityUnsure = 1 order by setName";
	public static final String GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME =
			"select distinct gamePlayCardUUID from cardSets where setName = ?";
	public static final String GET_DISTINCT_GAMEPLAYCARDS_IN_SET_BY_NAME =
			"select distinct a.* from gamePlayCard a left join cardSets b on a.gamePlayCardUUID = b.gamePlayCardUUID where " +
					"UPPER(b.setName) = ?";
	public static final String GET_DISTINCT_GAMEPLAYCARDS_BY_ARCHETYPE =
			"select distinct * from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";
	public static final String GET_SORTED_SET_NUMBERS_IN_SET_BY_NAME = "select setNumber from cardSets where setName = ?";
	public static final String GET_DISTINCT_SET_NAMES = "select distinct cardSets.setName from cardSets left join setData on cardSets" +
			".setName = setData.setName order by setData.releaseDate desc";
	public static final String GET_DISTINCT_SET_AND_ARCHETYPE_NAMES = "select * from (select distinct cardSets.setName from cardSets " +
			"inner join setData on cardSets.setName = setData.setName order by setData.releaseDate desc) UNION ALL select *" +
			" from (select distinct archetype from gamePlayCard where archetype is not null order by archetype asc)";
	public static final String GET_COUNT_DISTINCT_CARDS_IN_SET = "select count (distinct setNumber) from cardSets where setName = ?";
	public static final String GET_COUNT_QUANTITY =
			"select sum(quantity) from ownedCards where ownedCards.folderName <> '" + Const.FOLDER_MANUAL + "'";
	public static final String GET_COUNT_QUANTITY_MANUAL =
			"select sum(quantity) from ownedCards where ownedCards.folderName = '" + Const.FOLDER_MANUAL + "'";
	public static final String GET_FIRST_CARD_SET_FOR_CARD_IN_SET =
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_PREFIX + " where UPPER(cardSets.setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_NAME =
			"select setName,setPrefix,numOfCards,releaseDate from setData where UPPER(setName) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_PREFIX =
			"select setName,setPrefix,numOfCards,releaseDate from setData where setPrefix = ?";
	public static final String GET_ALL_SET_META_DATA_FROM_SET_DATA =
			"select distinct setName,setPrefix,numOfCards,releaseDate from setData";
	public static final String GET_CARDS_ONLY_PRINTED_ONCE = "select cardSets.gamePlayCardUUID, cardName, type, setNumber,setRarity, " +
			"cardSets.setName, releaseDate, archetype from cardSets join setData on setData.setName = cardSets.setName join " +
			"gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where cardName in (select cardName from " +
			"(Select DISTINCT cardName, setName from cardSets join gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets" +
			".gamePlayCardUUID where type <>'Token') group by cardName having count(cardName) = 1) order by releaseDate";
	public static final String REPLACE_INTO_CARD_SET_META_DATA =
			"Replace into setData(setName,setPrefix,numOfCards,releaseDate) values(?,?,?,?)";
	public static final String GET_GAME_PLAY_CARD_BY_UUID = "select * from gamePlayCard where gamePlayCardUUID = ?";
	public static final String GET_ALL_GAME_PLAY_CARD = "select * from gamePlayCard";
	public static final String REPLACE_INTO_GAME_PLAY_CARD = "Replace into gamePlayCard(gamePlayCardUUID,title,type,passcode,lore," +
			"attribute,race,linkValue,level,pendScale,atk,def,archetype, modificationDate) values(?,?,?,?,?,?,?,?,?,?,?,?,?," +
			"datetime('now','localtime'))";
	public static final String UPDATE_OWNED_CARD_BY_UUID = "update ownedCards set gamePlayCardUUID = ?,folderName = ?,cardName = ?," +
			"quantity = ?,setPrefix = ?, setNumber = ?,setName = ?,setRarity = ?,setRarityColorVariant = ?,condition = ?," +
			"editionPrinting = ?,dateBought = ?,priceBought = ?,rarityUnsure = ?, modificationDate = datetime('now','localtime'), " +
			"passcode = ? where UUID = ?";

	public static final String DELETE_FROM_OWNED_CARDS_WHERE_UUID = "DELETE FROM ownedCards WHERE UUID = ?";
	public static final String UPDATE_OWNED_CARDS_SET_QUANTITY_WHERE_UUID =
			"UPDATE ownedCards SET quantity = ?, modificationDate = datetime('now','localtime') WHERE UUID = ?";
	public static final String INSERT_INTO_SOLD_CARDS =
			"INSERT INTO soldCards (gamePlayCardUUID, cardName, quantity, setPrefix, setNumber, " +
					"setName, setRarity, setRarityColorVariant, condition, editionPrinting, dateBought, priceBought, dateSold, " +
					"priceSold, UUID, creationDate, modificationDate, passcode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"datetime('now','localtime'), ?)";
	public static final String INSERT_OR_IGNORE_INTO_OWNED_CARDS =
			"insert OR IGNORE into ownedCards(gamePlayCardUUID,folderName,cardName," +
					"quantity,setPrefix,setNumber,setName,setRarity,setRarityColorVariant,condition,editionPrinting,dateBought," +
					"priceBought,rarityUnsure, creationDate, modificationDate, UUID, passcode) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
					"datetime('now','localtime'),datetime('now','localtime'),?,?)";
	public static final String INSERT_OR_IGNORE_INTO_CARD_SETS = "INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName," +
			"setRarity,cardName,colorVariant,setURL) values(?,?,?,?,?,?,?)";

	public static final String INSERT_OR_IGNORE_INTO_CARD_SETS_WITH_ALT_ART = "INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName," +
			"setRarity,cardName,colorVariant,setURL, altArtPasscode) values(?,?,?,?,?,?,?,?)";
	public static final String UPDATE_CARD_SETS_SET_NAME = "update cardSets set setName = ? where setName = ?";
	public static final String UPDATE_OWNED_CARDS_SET_NAME = "update ownedCards set setName = ? where setName = ?";
	public static final String UPDATE_SET_DATA_SET_NAME = "update setData set setName = ? where setName = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime =" +
					" datetime('now','localtime') where setNumber = ? and setRarity = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY_LIMITED =
			"update cardSets set setPriceLimited = ?, setPriceLimitedUpdateTime =" +
					" datetime('now','localtime') where setNumber = ? and setRarity = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime" +
			"('now','localtime') where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime" +
					" = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_LIMITED =
			"update cardSets set setPriceLimited = ?, setPriceLimitedUpdateTime" +
					" = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? " +
					"and setName = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_FIRST = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ? and UPPER" +
			"(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_LIMITED = "update cardSets set setPriceLimited = ?, " +
			"setPriceLimitedUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ? and UPPER" +
			"(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_COLOR =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? " +
					"and setName = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_FIRST_COLOR = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ? and UPPER" +
			"(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_LIMITED_COLOR = "update cardSets set setPriceLimited = ?, " +
			"setPriceLimitedUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ? and UPPER" +
			"(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime" +
			"('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_FIRST = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_LIMITED = "update cardSets set setPriceLimited = ?, " +
			"setPriceLimitedUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_COLOR = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime" +
			"('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_FIRST_COLOR = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_LIMITED_COLOR = "update cardSets set setPriceLimited = ?, " +
			"setPriceLimitedUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ?";

	public static final String UPDATE_CARD_SET_PRICE_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ?";

	public static final String UPDATE_CARD_SET_PRICE_LIMITED =
			"update cardSets set setPriceLimited = ?, setPriceLimitedUpdateTime = datetime('now','localtime') where setNumber = ?";

	public static final String GET_NEW_LOWEST_PASSCODE = "select min(cast(passcode as INTEGER)) from gamePlayCard";

	public static final String GET_ALL_SET_BOXES = "select * from setBoxes order by boxLabel";

	public static final String GET_SET_BOXES_BY_NAME_OR_CODE_OR_LABEL =
			"select * from setBoxes where setPrefix = UPPER(?) or setName like ? or UPPER(boxLabel) = UPPER(?) order by boxLabel";

	public static final String UPDATE_CARD_SET_URL = "update cardSets set setURL = ? where setNumber = ? and setRarity = ? and " +
			"setName = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_URL_WITHOUT_SET_NAME = "update cardSets set setURL = ? where setNumber = ? and setRarity = ? and " +
			"UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_URL_WITHOUT_SET_NAME_OR_COLOR = "update cardSets set setURL = ? where setNumber = ? and setRarity = ? and " +
			"UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_URL_AND_COLOR = "update cardSets set setURL = ?, colorVariant = ? where setNumber = ? " +
			"and setRarity = ? and setName = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_BATCHED_BY_URL =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setURL = ?";

	public static final String UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime = datetime('now','localtime') where setURL = ?";

	public static final String UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_LIMITED =
			"update cardSets set setPriceLimited = ?, setPriceLimitedUpdateTime = datetime('now','localtime') where setURL = ?";

	public static final String UPDATE_SET_BOX_BY_UUID =
			"update setBoxes set boxLabel = ?, setPrefix = ?, setName = ? where setBoxUUID =" + " ?";

	public static final String INSERT_OR_IGNORE_INTO_SET_BOX =
			"insert OR IGNORE into setBoxes(boxLabel,setPrefix,setName,setBoxUUID) values(?,?,?,?)";

	public static final String GET_NEW_SET_BOX_DATA_FOR_VALID_SET_PREFIX =
			"select setBoxUUID, setData.setPrefix, setData.setName, '' as boxLabel from setData left join setBoxes on setBoxes.setPrefix" +
					" " + "=" + " setData.setPrefix where setBoxes.setPrefix is null and UPPER(setData.setPrefix) = UPPER(?)";

	public static final String GET_ALL_ART_PASSCODES_BY_NAME =
			"SELECT DISTINCT altArtPasscode from cardSets where upper(cardName) = upper(?) and altArtPasscode is not null union all " +
					"select passcode from gamePlayCard where  upper(title) = upper(?)";

	public static final String GET_ONLY_ART_PASSCODES_BY_GPC =
			"SELECT DISTINCT altArtPasscode from cardSets where gamePlayCardUUID = ? and altArtPasscode is not null";

	public static final String GET_ALL_PASSCODES =
			"SELECT DISTINCT altArtPasscode from cardSets where altArtPasscode is not null union all " +
					"select DISTINCT passcode from gamePlayCard where passcode > 0";
}
