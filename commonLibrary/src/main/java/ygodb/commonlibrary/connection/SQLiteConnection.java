package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.bean.SetMetaData;
import ygodb.commonlibrary.bean.SoldCard;

public interface SQLiteConnection {

	void closeInstance() throws SQLException;

	Map<String, List<CardSet>> getAllCardRaritiesForHashMap() throws SQLException;

	Map<String, List<GamePlayCard>> getAllGamePlayCardsForHashMap() throws SQLException;

	List<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

	List<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) throws SQLException;

	List<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy);

	List<OwnedCard> getAllPossibleCardsBySetName(String setName, String orderBy);

	List<OwnedCard> getAllPossibleCardsByArchetype(String archetype, String orderBy);

	CardSet getRarityOfExactCardInSet(String gamePlayCardUUID, String setNumber, String rarity, String colorVariant, String setName) throws SQLException;

	String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

	List<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

	String getGamePlayCardUUIDFromTitle(String title) throws SQLException;

	String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException;

	List<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String name) throws SQLException;

	List<OwnedCard> getAllOwnedCards() throws SQLException;

	OwnedCard getExistingOwnedCardByObject(OwnedCard query);

	List<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch);

	List<SoldCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch);

	List<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch);

	List<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException;

	List<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException;

	Map<String, List<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException;

	List<OwnedCard> getRarityUnsureOwnedCards() throws SQLException;

	List<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException;

	List<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName) throws SQLException;

	List<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype) throws SQLException;

	List<String> getSortedSetNumbersInSetByName(String setName) throws SQLException;

	List<String> getDistinctSetNames() throws SQLException;

	List<String> getDistinctSetAndArchetypeNames() throws SQLException;

	int getCountDistinctCardsInSet(String setName) throws SQLException;

	int getCountQuantity() throws SQLException;

	int getCountQuantityManual() throws SQLException;

	CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException;

	List<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException;

	List<SetMetaData> getSetMetaDataFromSetPrefix(String setPrefix) throws SQLException;

	List<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException;

	Map<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException;

	int replaceIntoCardSetMetaData(String setName, String setPrefix, int numOfCards, String tcgDate) throws SQLException;

	GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) throws SQLException;

	List<SetBox> getNewSetBoxDataForValidSetPrefix(String setPrefix) throws SQLException;

	int replaceIntoGamePlayCard(GamePlayCard input) throws SQLException;

	int insertOrUpdateOwnedCardByUUID(OwnedCard card) throws SQLException;

	int updateOwnedCardByUUID(OwnedCard card) throws SQLException;

	void sellCards(OwnedCard card, int quantity, String priceSold);

	int insertIntoOwnedCards(OwnedCard card) throws SQLException;

	int insertOrUpdateSetBoxByUUID(SetBox setBox) throws SQLException;

	int insertIntoSetBoxes(SetBox setBox) throws SQLException;

	int insertOrIgnoreIntoCardSet(String setNumber, String rarity, String setName, String gamePlayCardUUID, String cardName,
			String colorVariant, String url) throws SQLException;

	void updateSetName(String original, String newName) throws SQLException;

	int updateCardSetPrice(String setNumber, String rarity, String price, String edition) throws SQLException;

	int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName, String edition)
			throws SQLException;

	int updateCardSetPriceWithCardAndSetName(String setNumber, String rarity, String price, String setName, String cardName,
			String edition) throws SQLException;

	int updateCardSetPriceWithCardName(String setNumber, String rarity, String price, String cardName, String edition)
			throws SQLException;

	int updateCardSetPrice(String setNumber, String price, String edition) throws SQLException;

	int getNewLowestPasscode() throws SQLException;

	List<Integer> getAllArtPasscodesByName(String cardName) throws SQLException;

	List<SetBox> getAllSetBoxes() throws SQLException;

	List<SetBox> getSetBoxesByNameOrCodeOrLabel(String searchText) throws SQLException;

	int updateCardSetUrl(String setNumber, String rarity, String setName, String cardName, String setURL, String colorVariant)
			throws SQLException;

	int updateCardSetUrlAndColor(String setNumber, String rarity, String setName, String cardName, String setURL,
			String currentColorVariant, String newColorVariant) throws SQLException;

	PreparedStatementBatchWrapper getBatchedPreparedStatementUrlFirst() throws SQLException;

	PreparedStatementBatchWrapper getBatchedPreparedStatementUrlUnlimited() throws SQLException;

	PreparedStatementBatchWrapper getBatchedPreparedStatementUrlLimited() throws SQLException;
}
