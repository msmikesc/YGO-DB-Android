package ygodb.commonlibrary.utility;

import org.junit.jupiter.api.Test;
import ygodb.commonlibrary.bean.NameAndColor;
import ygodb.commonlibrary.connection.CsvConnection;
import ygodb.commonlibrary.constant.Const;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

	@Test
	void testGetNameAndColor_NoVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name");
		assertEquals("Card Name", result.name);
		assertEquals(Const.DEFAULT_COLOR_VARIANT , result.colorVariant);
	}

	@Test
	void testGetNameAndColor_RedVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name (Red)");
		assertEquals("Card Name", result.name);
		assertEquals("r", result.colorVariant);
	}

	@Test
	void testGetNameAndColor_BlueVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name (Blue)");
		assertEquals("Card Name", result.name);
		assertEquals("b", result.colorVariant);
	}

	@Test
	void testGetNameAndColor_GreenVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name (Green)");
		assertEquals("Card Name", result.name);
		assertEquals("g", result.colorVariant);
	}

	@Test
	void testGetNameAndColor_PurpleVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name (Purple)");
		assertEquals("Card Name", result.name);
		assertEquals("p", result.colorVariant);
	}

	@Test
	void testGetNameAndColor_AlternateArtVariant() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("Card Name (Alternate Art)");
		assertEquals("Card Name", result.name);
		assertEquals("a", result.colorVariant);
	}

	@Test
	void testGetNameAndColor_VariantInName() {
		CsvConnection obj = new CsvConnection();
		NameAndColor result = Util.getNameAndColor("(Red) Card Name");
		assertEquals("Card Name", result.name);
		assertEquals("r", result.colorVariant);
	}

	@Test
	void testBothInputsNull() {
		String result = Util.getLowestPriceString(null, null);
		assertEquals(Const.ZERO_PRICE_STRING, result);
	}

	@Test
	void testInput1Null() {
		String result = Util.getLowestPriceString(null, "10.99");
		assertEquals("10.99", result);
	}

	@Test
	void testInput2Null() {
		String result = Util.getLowestPriceString("5.99", null);
		assertEquals("5.99", result);
	}

	@Test
	void testBothInputsZeroPrice() {
		String result = Util.getLowestPriceString("0", "0");
		assertEquals("0", result);
	}

	@Test
	void testInput1LowerPrice() {
		String result = Util.getLowestPriceString("5.99", "10.99");
		assertEquals("5.99", result);
	}

	@Test
	void testInput2LowerPrice() {
		String result = Util.getLowestPriceString("15.99", "10.99");
		assertEquals("10.99", result);
	}

}