package ygodb.commonlibrary.connection;

import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CSVConnectionTest {

	@Test
	void testGetCondition_NoReplacements() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Mint");
		assertEquals("Mint", result);
	}

	@Test
	void testGetCondition_UnlimitedReplacement() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Condition: Unlimited");
		assertEquals("", result);
	}

	@Test
	void testGetCondition_LimitedReplacement() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Condition: Limited");
		assertEquals("", result);
	}

	@Test
	void testGetCondition_FirstEditionReplacement() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Condition: 1st Edition");
		assertEquals("", result);
	}

	@Test
	void testGetCondition_ConditionLabelReplacement() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Condition: Lightly Played");
		assertEquals("LightPlayed", result);
	}

	@Test
	void testGetCondition_WhitespaceRemoval() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("  Moderately Played  ");
		assertEquals("Played", result);
	}

	@Test
	void testGetCondition_MultipleReplacements() {
		CsvConnection obj = new CsvConnection();
		String result = obj.getCondition("Condition: Heavily Played Damaged");
		assertEquals("PoorPoor", result);
	}

	@Test
	void testGetPrinting_FirstEdition() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name (1st Edition)");
		assertEquals(Const.CARD_PRINTING_FIRST_EDITION, result);
	}

	@Test
	void testGetPrinting_Unlimited() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name (Unlimited)");
		assertEquals(Const.CARD_PRINTING_UNLIMITED, result);
	}

	@Test
	void testGetPrinting_Limited() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name");
		assertEquals(Const.CARD_PRINTING_LIMITED, result);
	}

	@Test
	void testGetPrinting_MultiplePrintings_FirstEdition() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name (1st Edition) (Unlimited)");
		assertEquals(Const.CARD_PRINTING_FIRST_EDITION, result);
	}

	@Test
	void testGetPrinting_MultiplePrintings_Unlimited() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name (Unlimited) (1st Edition)");
		assertEquals(Const.CARD_PRINTING_FIRST_EDITION, result);
	}

	@Test
	void testGetPrinting_NoPrinting() {
		CsvConnection obj = new CsvConnection();
		String result = Util.identifyEditionPrinting("Card Name");
		assertEquals(Const.CARD_PRINTING_LIMITED, result);
	}

	@Test
	void testGetPasscodeOrNegativeOne_ExistingGamePlayCard() throws SQLException {
		// Create a mock SQLiteConnection
		SQLiteConnection db = mock(SQLiteConnection.class);

		// Create a mock GamePlayCard
		GamePlayCard mockGamePlayCard = mock(GamePlayCard.class);

		// Set up the mock behavior to return the passcode
		when(db.getGamePlayCardByUUID(anyString())).thenReturn(mockGamePlayCard);
		when(mockGamePlayCard.getPasscode()).thenReturn(1234);

		CsvConnection obj = new CsvConnection();
		int result = obj.getPasscodeOrNegativeOne(db, "Card Name", "12345");

		// Verify that the method returned the expected passcode
		assertEquals(1234, result);

		// Verify that the getGamePlayCardByUUID method was called with the correct UUID
		verify(db).getGamePlayCardByUUID("12345");
	}

	@Test
	void testGetPasscodeOrNegativeOne_NonExistingGamePlayCard() throws SQLException {
		// Create a mock SQLiteConnection
		SQLiteConnection db = mock(SQLiteConnection.class);

		// Set up the mock behavior to return null for the GamePlayCard
		when(db.getGamePlayCardByUUID(anyString())).thenReturn(null);

		CsvConnection obj = new CsvConnection();
		int result = obj.getPasscodeOrNegativeOne(db, "Card Name", "12345");

		// Verify that the method returned -1
		assertEquals(-1, result);
	}

	@Test
	void testGetOwnedCardFromTCGPlayerCSV() throws SQLException {

		CsvConnection csvConnectionSpy = Mockito.spy(CsvConnection.class);

		// Test case 1: All required fields are present
		CSVRecord current = mock(CSVRecord.class);

		when(current.get(Const.TCGPLAYER_ITEMS_CSV)).thenReturn("Armed Neos (Secret Rare)\n" +
				"Battles of Legend: Monstrous Revenge\n");
		when(current.get(Const.TCGPLAYER_DETAILS_CSV)).thenReturn("Rarity: Secret Rare\n" +
				"Condition: Near Mint 1st Edition\n");
		when(current.get(Const.TCGPLAYER_PRICE_CSV)).thenReturn("$1.95");
		when(current.get(Const.TCGPLAYER_QUANTITY_CSV)).thenReturn("1");
		when(current.get(Const.TCGPLAYER_IMPORT_TIME)).thenReturn(null);

		CardSet setIdentified = new CardSet();
		setIdentified.setRarityUnsure(1);
		setIdentified.setColorVariant(Const.DEFAULT_COLOR_VARIANT);
		setIdentified.setSetName("setName");
		setIdentified.setSetNumber(null);
		setIdentified.setSetCode(null);
		setIdentified.setGamePlayCardUUID("1");
		setIdentified.setSetRarity("Secret Rare");

		// Mock specific methods of the CsvConnection class
		doReturn(setIdentified).when(csvConnectionSpy).getCardSetMatchingDetails(any(), any(), any(), any(), any());

		doReturn(-1).when(csvConnectionSpy).getPasscodeOrNegativeOne(any(), any(), any());

		OwnedCard result = csvConnectionSpy.getOwnedCardFromTCGPlayerCSV(current, null);
		assertNotNull(result);
		assertEquals("Armed Neos", result.getCardName());
		assertEquals("Secret Rare", result.getSetRarity());
		assertEquals("NearMint", result.getCondition());
		assertEquals("1st Edition", result.getEditionPrinting());
		assertEquals("1.95", result.getPriceBought());
		assertEquals(1, result.getQuantity());
	}

	@Test
	void testGetOwnedCardFromTCGPlayerCSV_NullFields() throws SQLException {
		CsvConnection csvConnectionSpy = Mockito.spy(CsvConnection.class);

		// Test case: Null fields
		CSVRecord current = mock(CSVRecord.class);

		when(current.get(Const.TCGPLAYER_ITEMS_CSV)).thenReturn(null);
		when(current.get(Const.TCGPLAYER_DETAILS_CSV)).thenReturn(null);
		when(current.get(Const.TCGPLAYER_PRICE_CSV)).thenReturn(null);
		when(current.get(Const.TCGPLAYER_QUANTITY_CSV)).thenReturn(null);
		when(current.get(Const.TCGPLAYER_IMPORT_TIME)).thenReturn("2023-07-09");

		OwnedCard result = csvConnectionSpy.getOwnedCardFromTCGPlayerCSV(current, null);

		// Verify that the method returned null
		assertNull(result);
	}

	@Test
	void testGetOwnedCardFromTCGPlayerCSV_UnknownFormatItems() throws SQLException {
		CsvConnection csvConnectionSpy = Mockito.spy(CsvConnection.class);

		// Test case: Unknown format for items
		CSVRecord current = mock(CSVRecord.class);

		when(current.get(Const.TCGPLAYER_ITEMS_CSV)).thenReturn("Armed Neos (Secret Rare)");
		when(current.get(Const.TCGPLAYER_DETAILS_CSV)).thenReturn("Rarity: Secret Rare\n" +
				"Condition: Near Mint 1st Edition\n");
		when(current.get(Const.TCGPLAYER_PRICE_CSV)).thenReturn("$1.95");
		when(current.get(Const.TCGPLAYER_QUANTITY_CSV)).thenReturn("1");
		when(current.get(Const.TCGPLAYER_IMPORT_TIME)).thenReturn(null);

		OwnedCard result = csvConnectionSpy.getOwnedCardFromTCGPlayerCSV(current, null);

		// Verify that the method returned null
		assertNull(result);
	}

	@Test
	void testGetOwnedCardFromTCGPlayerCSV_UnknownFormatDetails() throws SQLException {
		CsvConnection csvConnectionSpy = Mockito.spy(CsvConnection.class);

		// Test case: Unknown format for details
		CSVRecord current = mock(CSVRecord.class);

		when(current.get(Const.TCGPLAYER_ITEMS_CSV)).thenReturn("Armed Neos (Secret Rare)\n" +
				"Battles of Legend: Monstrous Revenge\n");
		when(current.get(Const.TCGPLAYER_DETAILS_CSV)).thenReturn("Rarity: Secret Rare");
		when(current.get(Const.TCGPLAYER_PRICE_CSV)).thenReturn("$1.95");
		when(current.get(Const.TCGPLAYER_QUANTITY_CSV)).thenReturn("1");
		when(current.get(Const.TCGPLAYER_IMPORT_TIME)).thenReturn(null);

		OwnedCard result = csvConnectionSpy.getOwnedCardFromTCGPlayerCSV(current, null);

		// Verify that the method returned null
		assertNull(result);
	}

}

