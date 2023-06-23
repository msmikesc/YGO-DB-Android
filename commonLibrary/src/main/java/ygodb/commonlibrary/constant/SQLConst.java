package ygodb.commonlibrary.constant;

public class SQLConst {

	private SQLConst() {}

	public static final String GET_ALL_CARD_RARITIES =
			"Select * from cardSets";
	public static final String GET_ALL_CARD_SETS_OF_CARD_BY_GAME_PLAY_CARD_UUID_AND_SET =
			"Select * from cardSets where gamePlayCardUUID=? and setName = ?";
	public static final String GET_ALL_CARD_SETS_OF_CARD_BY_SET_NUMBER =
			"Select * from cardSets where setNumber = ?";
	public static final String GET_RARITIES_OF_CARD_BY_GAME_PLAY_CARD_UUID =
			"Select * from cardSets a left join gamePlayCard b on a.gamePlayCardUUID = b.gamePlayCardUUID " +
			"where a.gamePlayCardUUID=?";
	public static final String GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID =
			"Select * from cardSets a left join gamePlayCard b " +
			"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
			"where a.gamePlayCardUUID=? and UPPER(a.setName) = UPPER(?)";
	public static final String GET_CARD_TITLE_FROM_GAME_PLAY_CARD_UUID =
			"Select * from gamePlayCard where gamePlayCardUUID=?";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_TITLE =
			"Select * from gamePlayCard where UPPER(title)=UPPER(?)";
	public static final String GET_GAME_PLAY_CARD_UUID_FROM_PASSCODE =
			"Select * from gamePlayCard where passcode = ?";
	public static final String GET_NUMBER_OF_OWNED_CARDS_BY_GAME_PLAY_CARD_UUID =
			"select sum(quantity), cardName, " +
			"group_concat(DISTINCT setName), MAX(dateBought) as maxDate, " +
			"sum((1.0*priceBought)*quantity)/sum(quantity) as avgPrice, " +
			"gamePlayCardUUID " +
			"from ownedCards where gamePlayCardUUID = ? group by cardName";
	public static final String GET_ALL_OWNED_CARDS =
			"select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_SET_NUMBER =
			"select * from ownedCards where setCode is null";
	public static final String GET_ALL_OWNED_CARDS_WITHOUT_PASSCODE =
			"select * from ownedCards where passcode = -1";
	public static final String GET_ALL_OWNED_CARDS_FOR_HASH_MAP =
			"select * from ownedCards order by setName, setRarity, cardName";
	public static final String GET_RARITY_UNSURE_OWNED_CARDS =
			"select * from ownedCards where rarityUnsure = 1 order by setName";
	public static final String GET_DISTINCT_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME =
			"select distinct gamePlayCardUUID from cardSets where setName = ?";
	public static final String GET_DISTINCT_CARD_NAMES_AND_GAME_PLAY_CARD_UUIDS_IN_SET_BY_NAME =
			"select a.* from gamePlayCard a left join cardSets b " +
			"on a.gamePlayCardUUID = b.gamePlayCardUUID " +
			"where b.setName = ?";
	public static final String GET_DISTINCT_CARD_NAMES_AND_IDS_BY_ARCHETYPE =
			"select * from gamePlayCard where UPPER(archetype) = UPPER(?) OR title like ?";
	public static final String GET_SORTED_CARDS_IN_SET_BY_NAME =
			"select setNumber from cardSets where setName = ?";
	public static final String GET_DISTINCT_SET_NAMES =
			"select distinct cardSets.setName from cardSets " +
			"inner join setData on cardSets.setName = setData.setName " +
			"order by setData.releaseDate desc";
	public static final String GET_DISTINCT_SET_AND_ARCHETYPE_NAMES =
			"select * from (select distinct cardSets.setName from " +
			"cardSets inner join setData on cardSets.setName = setData.setName " +
			"order by setData.releaseDate desc) " +
			"UNION ALL " +
			"select * from (select distinct archetype from gamePlayCard where archetype is not null order by archetype asc)";
	public static final String GET_COUNT_DISTINCT_CARDS_IN_SET =
			"select count (distinct setNumber) from cardSets where setName = ?";
	public static final String GET_COUNT_QUANTITY =
			"select sum(quantity) from ownedCards where ownedCards.folderName <> 'Manual Folder'";
	public static final String GET_COUNT_QUANTITY_MANUAL =
			"select sum(quantity) from ownedCards where ownedCards.folderName = 'Manual Folder'";
	public static final String GET_FIRST_CARD_SET_FOR_CARD_IN_SET =
			"select * from cardSets where UPPER(setName) = UPPER(?) and UPPER(cardName) = UPPER(?)";
	public static final String GET_CARD_SETS_FOR_VALUES =
			"select * from cardSets where UPPER(setName) = UPPER(?) " +
			"and UPPER(setNumber) = UPPER(?) and UPPER(setRarity) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_NAME =
			"select setName,setCode,numOfCards,releaseDate from setData where UPPER(setName) = UPPER(?)";
	public static final String GET_SET_META_DATA_FROM_SET_CODE =
			"select setName,setCode,numOfCards,releaseDate from setData where setCode = ?";
	public static final String GET_ALL_SET_META_DATA_FROM_SET_DATA =
			"select distinct setName,setCode,numOfCards,releaseDate from setData";
	public static final String GET_CARDS_ONLY_PRINTED_ONCE =
			"select cardSets.gamePlayCardUUID, cardName, type, setNumber,setRarity, " +
			"cardSets.setName, releaseDate, archetype from cardSets " +
			"join setData on setData.setName = cardSets.setName "
			+ "join gamePlayCard on gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID "
			+ "where cardName in (select cardName from "
			+ "(Select DISTINCT cardName, setName from cardSets join gamePlayCard on " +
			" gamePlayCard.gamePlayCardUUID = cardSets.gamePlayCardUUID where type <>'Token') "
			+ "group by cardName having count(cardName) = 1) "
			+ "order by releaseDate";
	public static final String REPLACE_INTO_CARD_SET_META_DATA =
			"Replace into setData(setName,setCode,numOfCards,releaseDate) values(?,?,?,?)";
	public static final String GET_GAME_PLAY_CARD_BY_UUID =
			"select * from gamePlayCard where gamePlayCardUUID = ?";
	public static final String GET_ALL_GAME_PLAY_CARD =
			"select * from gamePlayCard";
	public static final String REPLACE_INTO_GAME_PLAY_CARD =
			"Replace into gamePlayCard(gamePlayCardUUID,title,type,passcode,lore," +
			"attribute,race,linkValue,level,pendScale,atk,def,archetype, " +
			"modificationDate) " +
			"values(?,?,?,?,?,?,?,?,?,?,?,?,?,datetime('now','localtime'))";
	public static final String UPDATE_OWNED_CARD_BY_UUID =
			"update ownedCards set gamePlayCardUUID = ?,folderName = ?,cardName = ?,quantity = ?,"
			+ "setCode = ?, setNumber = ?,setName = ?,setRarity = ?,setRarityColorVariant = ?,"
			+ "condition = ?,editionPrinting = ?,dateBought = ?,priceBought = ?,rarityUnsure = ?, "
			+ "modificationDate = datetime('now','localtime'), passcode = ? "
			+ "where UUID = ?";

	public static final String DELETE_FROM_OWNED_CARDS_WHERE_UUID =
			"DELETE FROM ownedCards WHERE UUID = ?";
	public static final String UPDATE_OWNED_CARDS_SET_QUANTITY_WHERE_UUID =
			"UPDATE ownedCards SET quantity = ?, modificationDate = datetime('now','localtime') WHERE UUID = ?";
	public static final String INSERT_INTO_SOLD_CARDS =
			"INSERT INTO soldCards (gamePlayCardUUID, cardName, quantity, setCode, setNumber, " +
			"setName, setRarity, setRarityColorVariant, condition, editionPrinting, dateBought, " +
			"priceBought, dateSold, priceSold, UUID, creationDate, modificationDate, passcode) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now','localtime'), ?)";
	public static final String UPSERT_OWNED_CARD_BATCH =
			"insert into ownedCards(gamePlayCardUUID,folderName,cardName,quantity,setCode,"
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
	public static final String REPLACE_INTO_CARD_SET_WITH_SOFT_PRICE_UPDATE =
			"INSERT OR IGNORE into cardSets(gamePlayCardUUID,setNumber,setName,setRarity,cardName) values(?,?,?,?,?)";
	public static final String UPDATE_CARD_SETS_SET_NAME =
			"update cardSets set setName = ? where setName = ?";
	public static final String UPDATE_OWNED_CARDS_SET_NAME =
			"update ownedCards set setName = ? where setName = ?";
	public static final String UPDATE_SET_DATA_SET_NAME =
			"update setData set setName = ? where setName = ?";
	public static final String UPDATE_CARD_SET_PRICE_WITH_RARITY =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
			+ " where setNumber = ? and setRarity = ?";
	public static final String GET_UPDATED_ROW_COUNT =
			"select changes()";
	public static final String UPDATE_CARD_SET_PRICE_WITH_SET_NAME =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
			+ " where setNumber = ? and setRarity = ? and setName = ?";

	public static final String UPDATE_CARD_SET_PRICE =
			"update cardSets set setPrice = ?, setPriceUpdateTime = datetime('now','localtime')"
			+ " where setNumber = ?";
}
