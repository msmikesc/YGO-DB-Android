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

    void closeInstance();

    HashMap<String, ArrayList<CardSet>> getAllCardRarities();

    ArrayList<CardSet> getAllRaritiesOfCardByID(int id);

    ArrayList<CardSet> getRaritiesOfCardInSetByID(int id, String setName);

    ArrayList<CardSet> getRaritiesOfCardByID(int id);

    ArrayList<CardSet> getRaritiesOfCardInSetByIDAndName(int id, String setName, String cardName);

    ArrayList<OwnedCard> getAllPossibleCardsByNameSearch(String cardName, String orderBy);

    String getCardTitleFromID(int wikiID);

    int getCardIdFromTitle(String title);

    ArrayList<OwnedCard> getNumberOfOwnedCardsById(int id);

    ArrayList<OwnedCard> getNumberOfOwnedCardsByName(String name);

    ArrayList<OwnedCard> getAllOwnedCards() throws SQLException;

    OwnedCard getExistingOwnedCardByObject(OwnedCard query);

    ArrayList<OwnedCard> queryOwnedCards(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> queryOwnedCardsGrouped(String orderBy, int limit, int offset, String cardNameSearch);

    ArrayList<OwnedCard> getAllOwnedCardsWithoutSetCode();

    HashMap<String, ArrayList<OwnedCard>> getAllOwnedCardsForHashMap();

    ArrayList<OwnedCard> getRarityUnsureOwnedCards();

    ArrayList<Integer> getDistinctCardIDsInSetByName(String setName);

    ArrayList<String> getDistinctCardNamesInSetByName(String setName);

    ArrayList<CardSet> getDistinctCardNamesAndIdsInSetByName(String setName);

    ArrayList<Integer> getDistinctCardIDsByArchetype(String archetype);

    ArrayList<String> getDistinctCardNamesByArchetype(String archetype);

    ArrayList<CardSet> getDistinctCardNamesAndIdsByArchetype(String archetype);

    ArrayList<String> getSortedCardsInSetByName(String setName);

    ArrayList<String> getDistinctSetNames();

    ArrayList<String> getDistinctSetAndArchetypeNames();

    int getCountDistinctCardsInSet(String setName);

    int getCountQuantity();

    int getCountQuantityManual();

    CardSet getFirstCardSetForCardInSet(String cardName, String setName);

    ArrayList<SetMetaData> getSetMetaDataFromSetName(String setName);

    ArrayList<SetMetaData> getSetMetaDataFromSetCode(String setCode);

    ArrayList<SetMetaData> getAllSetMetaDataFromSetData();

    HashMap<String, AnalyzePrintedOnceData> getCardsOnlyPrintedOnce();

    void replaceIntoCardSetMetaData(String set_name, String set_code, int num_of_cards, String tcg_date);

    GamePlayCard getGamePlayCardByNameAndID(Integer wikiID, String name);

    void replaceIntoGamePlayCard(Integer wikiID, String name, String type, Integer passcode, String desc,
                                 String attribute, String race, Integer linkval, Integer level, Integer scale, Integer atk, Integer def,
                                 String archetype);

    void UpdateOwnedCardByUUID(OwnedCard card);

    void sellCards(OwnedCard card, int quantity, String priceSold);

    void upsertOwnedCardBatch(OwnedCard card);

    void replaceIntoCardSet(String setNumber, String rarity, String setName, int wikiID, String price,
                            String cardName);

    void updateSetName(String original, String newName);
}
