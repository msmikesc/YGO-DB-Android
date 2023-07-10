package ygodb.commonlibrary.bean;

import org.junit.jupiter.api.Test;
import ygodb.commonlibrary.constant.Const;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CardSetTest {

	@Test
	void testGetBestExistingPrice_PreferFirstEdition_PreferredPriceNotNull_ReturnsPreferredPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("10.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(true);

		assertEquals("10.00", result);
	}

	@Test
	void testGetBestExistingPrice_PreferFirstEdition_PreferredPriceNotNull_ReturnsSecondaryPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("0.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(true);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_PreferFirstEdition_PreferredPriceIsNull_ReturnsSecondaryPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst(null);
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(true);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_PreferFirstEdition_PreferredPriceIsZero_ReturnsSecondaryPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("0.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(true);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_PreferFirstEdition_PreferredPriceIsNullAndSecondaryPriceIsNull_ReturnsZeroPriceString() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst(null);
		cardSet.setSetPrice(null);

		String result = cardSet.getBestExistingPrice(true);

		assertEquals(Const.ZERO_PRICE_STRING, result);
	}

	@Test
	void testGetBestExistingPrice_NotPreferFirstEdition_PreferredPriceNotNull_ReturnsSecondaryPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("10.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(false);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_NotPreferFirstEdition_PreferredPriceNotNull_ReturnsPreferredPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("0.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(false);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_NotPreferFirstEdition_PreferredPriceIsNull_ReturnsPreferredPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst(null);
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(false);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_NotPreferFirstEdition_PreferredPriceIsZero_ReturnsPreferredPrice() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst("0.00");
		cardSet.setSetPrice("5.00");

		String result = cardSet.getBestExistingPrice(false);

		assertEquals("5.00", result);
	}

	@Test
	void testGetBestExistingPrice_NotPreferFirstEdition_PreferredPriceIsNullAndSecondaryPriceIsNull_ReturnsZeroPriceString() {
		CardSet cardSet = new CardSet();
		cardSet.setSetPriceFirst(null);
		cardSet.setSetPrice(null);

		String result = cardSet.getBestExistingPrice(false);

		assertEquals(Const.ZERO_PRICE_STRING, result);
	}
}
