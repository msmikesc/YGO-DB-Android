package connection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import bean.AnalyzePrintedOnceData;
import bean.CardSet;
import bean.GamePlayCard;
import bean.OwnedCard;
import bean.SetMetaData;

public interface SQLiteConnection {

    void closeInstance() throws SQLException;

    HashMap<String, ArrayList<CardSet>> getAllCardRarities() throws SQLException;

    ArrayList<CardSet> getAllRaritiesOfCardByID(int id) throws SQLException;

    ArrayList<CardSet> getAllCardSetsOfCardByIDAndSet(int id, String setName) throws SQLException;

    ArrayList<CardSet> getAllCardSetsOfCardBySetNumber(String setNumber) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardInSetByID(int id, String setName) throws SQLException;

    ArrayList<CardSet> getRaritiesOfCardByID(int id);

    ArrayList<CardSet> getRaritiesOfCardInSetByIDAndName(int id, String setName, String cardName) throws SQLException;

    ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy);

    String getCardTitleFromID(int wikiID) throws SQLException;

    ArrayList<String> getMultiCardTitlesFromID(int wikiID) throws SQLException;

    int getCardIdFromTitle(String title) throws SQLException;

    ArrayList<OwnedCard> getNumberOfOwnedCardsById(int id) throws SQLException;

    ArrayList<OwnedCard> getNumberOfOwnedCardsByName(String name);

    ArrayList<OwnedCard> getAllOwnedCards() throws SQLException;

    OwnedCard getExistingOwnedCardByObject(OwnedCard query);

    ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> getAllOwnedCardsWithoutSetCode() throws SQLException;

    HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap() throws SQLException;

    ArrayList<OwnedCard> getRarityUnsureOwnedCards() throws SQLException;

    ArrayList<Integer> getDistinctCardIDsInSetByName(String setName) throws SQLException;

    ArrayList<String> getDistinctCardNamesInSetByName(String setName);

    ArrayList<CardSet> getDistinctCardNamesAndIdsInSetByName(String setName);

    ArrayList<Integer> getDistinctCardIDsByArchetype(String archetype) throws SQLException;

    ArrayList<String> getDistinctCardNamesByArchetype(String archetype);

    ArrayList<CardSet> getDistinctCardNamesAndIdsByArchetype(String archetype);

    ArrayList<String> getSortedCardsInSetByName(String setName) throws SQLException;

    ArrayList<String> getDistinctSetNames() throws SQLException;

    ArrayList<String> getDistinctSetAndArchetypeNames();

    int getCountDistinctCardsInSet(String setName) throws SQLException;

    int getCountQuantity() throws SQLException;

    int getCountQuantityManual() throws SQLException;

    CardSet getFirstCardSetForCardInSet(String cardName, String setName) throws SQLException;

    ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName) throws SQLException;

    ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode) throws SQLException;

    ArrayList<SetMetaData> getAllSetMetaDataFromSetData() throws SQLException;

    HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce() throws SQLException;

    void replaceIntoCardSetMetaData(String set_name, String set_code, int num_of_cards, String tcg_date) throws SQLException;

    GamePlayCard getGamePlayCardByNameAndID(Integer wikiID, String name) throws SQLException;

    void replaceIntoGamePlayCard(GamePlayCard input) throws SQLException;

    void UpdateOwnedCardByUUID(OwnedCard card) throws SQLException;

    void sellCards(OwnedCard card, int quantity, String priceSold);

    void upsertOwnedCardBatch(OwnedCard card) throws SQLException;

    void replaceIntoCardSet(String setNumber, String rarity, String setName, int wikiID, String price,
                            String cardName) throws SQLException;

    void updateSetName(String original, String newName) throws SQLException;

    int updateCardSetPrice(String setNumber, String rarity, String price) throws SQLException;

    int updateCardSetPriceWithSetName(String setNumber, String rarity, String price, String setName)
            throws SQLException;

    int updateCardSetPrice(String setNumber, String price) throws SQLException;
}
