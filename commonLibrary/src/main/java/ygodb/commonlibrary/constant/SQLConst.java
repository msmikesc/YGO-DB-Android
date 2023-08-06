package ygodb.commonlibrary.constant;

public class SQLConst {

	private SQLConst() {
	}

	public static final String OWNED_CARDS_TABLE = "ownedCards";

	public static final String SELECT_STAR_FROM_CARD_SETS_WITH_SET_CODE =
			"Select cardSets.*, setData.setCode from cardSets left join setData on cardSets.setName = setData.setName";

	public static final String GET_ALL_CARD_RARITIES = SELECT_STAR_FROM_CARD_SETS_WITH_SET_CODE;
	public static final String GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID =
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_CODE + " where gamePlayCardUUID=?";
	public static final String GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID =
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_CODE + " where gamePlayCardUUID=? and UPPER(cardSets.setName) = UPPER(?)";
	public static final String GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID = "Select * from gamePlayCard where gamePlayCardUUID=?";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_TITLE = "Select * from gamePlayCard where UPPER(title)=UPPER(?)";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE = "Select * from gamePlayCard where passcode = ?";
	public static final String GET_ANALYZE_DATA_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID = "select sum(quantity), cardName, group_concat" +
			"(DISTINCT setName), MAX(dateBought) as maxDate, sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
			"gamePlayCardUUID from ownedCards where gamePlayCardUUID = ? group by cardName";
	public static final String GET_ALL_OWNED_CARDS = "select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_SET_NUMBER = "select * from ownedCards where setCode is null";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE = "select * from ownedCards where passcode = -1";
	public static final String GET_ALL_OWNED_CARDS_FOR_HASH_MAP = "select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_RARITY_UNSURE_OWNED_CARDS = "select * from ownedCards where rarityUnsure = 1 order by setName";
	public static final String GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME =
			"select distinct gamePlayCardUUID from cardSets where setName = ?";
	public static final String GET_DISTINCT_GAMEPLAYCARDS_IN_SET_BY_NAME =
			"select distinct a.* from gamePlayCard a left join cardSets b on a.gamePlayCardUUID = b.gamePlayCardUUID where b" +
					".setName = ?";
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
			SELECT_STAR_FROM_CARD_SETS_WITH_SET_CODE + " where UPPER(cardSets.setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_NAME =
			"select setName,setCode,numOfCards,releaseDate from setData where UPPER(setName) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_PREFIX =
			"select setName,setCode,numOfCards,releaseDate from setData where setCode = ?";
	public static final String GET_ALL_SET_META_DATA_FROM_SET_DATA = "select distinct setName,setCode,numOfCards,releaseDate from setData";
	public static final String GET_CARDS_ONLY_PRINTED_ONCE = "select cardSets.gamePlayCardUUID, cardName, type, setNumber,setRarity, " +
			"cardSets.setName, releaseDate, archetype from cardSets join setData on setData.setName = cardSets.setName join " +
			"gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where cardName in (select cardName from " +
			"(Select DISTINCT cardName, setName from cardSets join gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets" +
			".gamePlayCardUUID where type <>'Token') group by cardName having count(cardName) = 1) order by releaseDate";
	public static final String REPLACE_INTO_CARD_SET_META_DATA =
			"Replace into setData(setName,setCode,numOfCards,releaseDate) values(?,?,?,?)";
	public static final String GET_GAME_PLAY_CARD_BY_UUID = "select * from gamePlayCard where gamePlayCardUUID = ?";
	public static final String GET_ALL_GAME_PLAY_CARD = "select * from gamePlayCard";
	public static final String REPLACE_INTO_GAME_PLAY_CARD = "Replace into gamePlayCard(gamePlayCardUUID,title,type,passcode,lore," +
			"attribute,race,linkValue,level,pendScale,atk,def,archetype, modificationDate) values(?,?,?,?,?,?,?,?,?,?,?,?,?," +
			"datetime('now','localtime'))";
	public static final String UPDATE_OWNED_CARD_BY_UUID = "update ownedCards set gamePlayCardUUID = ?,folderName = ?,cardName = ?," +
			"quantity = ?,setCode = ?, setNumber = ?,setName = ?,setRarity = ?,setRarityColorVariant = ?,condition = ?," +
			"editionPrinting = ?,dateBought = ?,priceBought = ?,rarityUnsure = ?, modificationDate = datetime('now','localtime'), " +
			"passcode = ? where UUID = ?";

	public static final String DELETE_FROM_OWNED_CARDS_WHERE_UUID = "DELETE FROM ownedCards WHERE UUID = ?";
	public static final String UPDATE_OWNED_CARDS_SET_QUANTITY_WHERE_UUID =
			"UPDATE ownedCards SET quantity = ?, modificationDate = datetime('now','localtime') WHERE UUID = ?";
	public static final String INSERT_INTO_SOLD_CARDS =
			"INSERT INTO soldCards (gamePlayCardUUID, cardName, quantity, setCode, setNumber, " +
					"setName, setRarity, setRarityColorVariant, condition, editionPrinting, dateBought, priceBought, dateSold, " +
					"priceSold, UUID, creationDate, modificationDate, passcode) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
					"datetime('now','localtime'), ?)";
	public static final String INSERT_OR_IGNORE_INTO_OWNED_CARDS =
			"insert OR IGNORE into ownedCards(gamePlayCardUUID,folderName,cardName," +
					"quantity,setCode,setNumber,setName,setRarity,setRarityColorVariant,condition,editionPrinting,dateBought," +
					"priceBought,rarityUnsure, creationDate, modificationDate, UUID, passcode) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
					"datetime('now','localtime'),datetime('now','localtime'),?,?)";
	public static final String INSERT_OR_IGNORE_INTO_CARD_SETS = "INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName," +
			"setRarity,cardName,colorVariant,setURL) values(?,?,?,?,?,?,?)";
	public static final String UPDATE_CARD_SETS_SET_NAME = "update cardSets set setName = ? where setName = ?";
	public static final String UPDATE_OWNED_CARDS_SET_NAME = "update ownedCards set setName = ? where setName = ?";
	public static final String UPDATE_SET_DATA_SET_NAME = "update setData set setName = ? where setName = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime =" +
					" datetime('now','localtime') where setNumber = ? and setRarity = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime" +
			"('now','localtime') where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime" +
					" = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? " +
					"and" +
					" " + "setName = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME_AND_CARD_NAME_FIRST = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and setName = ? and UPPER" +
			"(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME = "update cardSets set setPrice = ?, setPriceUpdateTime = datetime" +
			"('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE_WITH_CARD_NAME_FIRST = "update cardSets set setPriceFirst = ?, " +
			"setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ? and setRarity = ? and UPPER(cardName) = UPPER(?)";

	public static final String UPDATE_CARD_SET_PRICE =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setNumber = ?";

	public static final String UPDATE_CARD_SET_PRICE_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime = datetime('now','localtime') where setNumber = ?";

	public static final String GET_NEW_LOWEST_PASSCODE = "select min(cast(passcode as INTEGER)) from gamePlayCard";

	public static final String GET_ALL_SET_BOXES = "select * from setBoxes order by boxLabel";

	public static final String GET_SET_BOXES_BY_NAME_OR_CODE_OR_LABEL =
			"select * from setBoxes where setCode = UPPER(?) or setName like ? or UPPER(boxLabel) = UPPER(?) order by boxLabel";

	public static final String UPDATE_CARD_SET_URL = "update cardSets set setURL = ? where setNumber = ? and setRarity = ? and " +
			"setName = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_URL_AND_COLOR = "update cardSets set setURL = ?, colorVariant = ? where setNumber = ? " +
			"and setRarity = ? and setName = ? and UPPER(cardName) = UPPER(?) and colorVariant = ?";

	public static final String UPDATE_CARD_SET_PRICE_BATCHED_BY_URL =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime') where setURL = ?";

	public static final String UPDATE_CARD_SET_PRICE_BATCHED_BY_URL_FIRST =
			"update cardSets set setPriceFirst = ?, setPriceFirstUpdateTime = datetime('now','localtime') where setURL = ?";

	public static final String UPDATE_SET_BOX_BY_UUID = "update setBoxes set boxLabel = ?, setCode = ?, setName = ? where setBoxUUID = ?";

	public static final String INSERT_OR_IGNORE_INTO_SET_BOX =
			"insert OR IGNORE into setBoxes(boxLabel,setCode,setName,setBoxUUID) values(?,?,?,?)";

	public static final String GET_NEW_SET_BOX_DATA_FOR_VALID_SET_PREFIX =
			"select setBoxUUID, setData.setCode, setData.setName, '' as boxLabel from setData left join setBoxes on setBoxes.setCode = " +
					"setData.setCode where setBoxes.setCode is null and UPPER(setData.setCode) = UPPER(?)";
}
