package ygodb.commonlibrary.connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ygodb.commonlibrary.bean.AnalyzePrintedOnceData;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.bean.SetMetaData;

public interface SQLiteConnection {

    void closeInstance() throws SQLException;

    HashMap<String, ArrayList<CardSet>> getAllCardRarities() throws SQLException;

    ArrayList<CardSet> getAllCardSetsOfCardByGamePlayCardUUIDAndSet(String gamePlayCardUUID, String setName) throws SQLException;

    ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardByGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName) throws SQLException;

    ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy);

    String getCardTitleFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    ArrayList<String> getMultipleCardNamesFromGamePlayCardUUID(String gamePlayCardUUID) throws SQLException;

    String getGamePlayCardUUIDFromTitle(String title) throws SQLException;

    String getGamePlayCardUUIDFromPasscode(int passcode) throws SQLException;

    ArrayList<OwnedCard> getNumberOfOwnedCardsByGamePlayCardUUID(String name) throws SQLException;

    ArrayList<OwnedCard> getAllOwnedCards() throws SQLException;

    OwnedCard getExistingOwnedCardByObject(OwnedCard query);

    ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch);

	ArrayList<OwnedCard> querySoldCards(String orderBy, int limit, int offset, String cardNameSearch);

	ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> getAllOwnedCardsWithoutSetNumber() throws SQLException;

    ArrayList<OwnedCard> getAllOwnedCardsWithoutPasscode() throws SQLException;

    HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException;

    ArrayList<OwnedCard> getRarityUnsureOwnedCards() throws SQLException;

    ArrayList<String> getDistinctGamePlayCardUUIDsInSetByName(String setName) throws SQLException;

    ArrayList<GamePlayCard> getDistinctCardNamesAndGamePlayCardUUIDsInSetByName(String setName) throws SQLException;

    ArrayList<GamePlayCard> getDistinctCardNamesAndIdsByArchetype(String archetype) throws SQLException;

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

    void updateOwnedCardByUUID(OwnedCard card) throws SQLException;

    void sellCards(OwnedCard card, int quantity, String priceSold);

    void upsertOwnedCardBatch(OwnedCard card) throws SQLException;

    void replaceIntoCardSetWithSoftPriceUpdate(String setNumber, String rarity, String setName, String gamePlayCardUUID, String price,
                                               String cardName) throws SQLException;

    void updateSetName(String original, String newName) throws SQLException;

    int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException;

    int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName)
            throws SQLException;

    int getUpdatedRowCount() throws SQLException;

    int updateCardSetPrice(String setNumber, String price) throws SQLException;

    int getNewLowestPasscode() throws SQLException;
}
