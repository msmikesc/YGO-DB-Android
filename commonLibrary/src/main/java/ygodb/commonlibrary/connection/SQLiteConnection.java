package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetBox;
import ygodb.commonlibrary.bean.SetMetaData;

public interface SQLiteConnection {

    void closeInstance() throws SQLException;

    HashMap<String, List<CardSet>> getAllCardRaritiesForHashMap() throws SQLException;

    HashMap<String, List<GamePlayCard>> getAllGamePlayCardsForHashMap() throws SQLException;

    ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) throws SQLException;

    ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy);

    String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    String getGamePlayCardUUIDFromTitle(String title) throws SQLException;

    String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException;

    ArrayList<OwnedCard> getAnalyzeDataOwnedCardSummaryByGamePlayCardUUID(String name) throws SQLException;

    ArrayList<OwnedCard> getAllOwnedCards() throws SQLException;

    OwnedCard getExistingOwnedCardByObject(OwnedCard query);

    ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch);

	ArrayList<OwnedCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch);

	ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException;

    ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException;

    HashMap<String, List<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException;

    ArrayList<OwnedCard> getRarityUnsureOwnedCards() throws SQLException;

    ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException;

    ArrayList<GamePlayCard> getDistinctGamePlayCardsInSetByName(String setName) throws SQLException;

    ArrayList<GamePlayCard> getDistinctGamePlayCardsByArchetype(String archetype) throws SQLException;

    ArrayList<String> getSortedCardsInSetByName(String setName) throws SQLException;

    ArrayList<String> getDistinctSetNames() throws SQLException;

    ArrayList<String> getDistinctSetAndArchetypeNames();

    int getCountDistinctCardsInSet(String setName) throws SQLException;

    int getCountQuantity() throws SQLException;

    int getCountQuantityManual() throws SQLException;

    CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException;

    List<CardSet> getCardSetsForValues(String setNumber, String rarity, String setName)
            throws SQLException;

    ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException;

    ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) throws SQLException;

    ArrayList<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException;

    HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException;

    void replaceIntoCardSetMetaData(String setName, String setCode, int numOfCards, String tcgDate) throws SQLException;

    GamePlayCard getGamePlayCardByUUID(String gamePlayCardUUID) throws SQLException;

    List<GamePlayCard> getAllGamePlayCard() throws SQLException;

    void replaceIntoGamePlayCard(GamePlayCard input) throws SQLException;

    void insertOrUpdateOwnedCardByUUID(OwnedCard card) throws SQLException;

    int updateOwnedCardByUUID(OwnedCard card) throws SQLException;

    void sellCards(OwnedCard card, int quantity, String priceSold);

    int insertIntoOwnedCards(OwnedCard card) throws SQLException;

    void insertOrIgnoreIntoCardSet(String setNumber, String rarity, String setName, String gamePlayCardUUID,
                                   String cardName, String colorVariant, String url) throws SQLException;

    void updateSetName(String original, String newName) throws SQLException;

    int updateCardSetPrice(String setNumber, String rarity, String price, boolean isFirstEdition) throws SQLException;

    int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName, boolean isFirstEdition)
            throws SQLException;

    int getUpdatedRowCount() throws SQLException;

    int updateCardSetPriceWithCardAndSetName(String setNumber, String rarity, String price, String setName,
                                             String cardName, boolean isFirstEdition)
            throws SQLException;

    int updateCardSetPriceWithCardName(String setNumber, String rarity, String price, String cardName, boolean isFirstEdition)
            throws SQLException;

    int updateCardSetPrice(String setNumber, String price, boolean isFirstEdition) throws SQLException;

    int getNewLowestPasscode() throws SQLException;

	List<SetBox> getAllSetBoxes();

	List<SetBox> getSetBoxesByNameOrCode(String searchText);

    void updateCardSetPriceBatchedWithCardAndSetName(String setNumber, String rarity, String price, String setName,
                                                     String cardName, boolean isFirstEdition)
            throws SQLException;

    void updateCardSetPriceBatchedWithCardName(String setNumber, String rarity, String price, String cardName, boolean isFirstEdition)
            throws SQLException;

	int updateCardSetUrl(String setNumber, String rarity, String setName,
						 String cardName, String setURL, String colorVariant)
			throws SQLException;

    int updateCardSetUrlAndColor(String setNumber, String rarity, String setName,
                                 String cardName, String setURL, String currentColorVariant, String newColorVariant)
            throws SQLException;

    PreparedStatementBatchWrapper getBatchedPreparedStatement(String input, BatchSetter setter)
            throws SQLException;

    PreparedStatementBatchWrapper getBatchedPreparedStatementUrlFirst()
            throws SQLException;

    PreparedStatementBatchWrapper getBatchedPreparedStatementUrlUnlimited()
            throws SQLException;
}
