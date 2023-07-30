package ygodb.commonlibrary.bean;

import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.utility.Util;

import java.math.BigDecimal;

public class CardSet {

	private String gamePlayCardUUID;
	private String cardName;
	private String setNumber;
	private String setName;
	private String setRarity;
	private String setPrice;
	private String setPriceUpdateTime;
	private String setPriceFirst;
	private String setPriceFirstUpdateTime;
	private String colorVariant;
	private String setUrl;
	private int rarityUnsure;
	private String setCode;
	private String editionPrinting;

	private static final String OPEN = "\"";
	private static final String CLOSE = "\",";
	private static final String SEP = "\",\"";

	public CardSet() {
	}

	public CardSet(String gamePlayCardUUID, String setNumber, String cardName, String setRarity, String setName,
				   String colorVariant, String setCode) {
		this.gamePlayCardUUID = gamePlayCardUUID;
		this.setNumber = setNumber;
		this.cardName = cardName;
		this.setRarity = setRarity;
		this.setName = setName;
		this.colorVariant = colorVariant;
		this.setCode = setCode;
		this.rarityUnsure = Const.RARITY_UNSURE_FALSE;
	}

	public String getCardLogIdentifier(){
		return OPEN + getSetNumber() + SEP + getCardName() + SEP + getSetRarity() + SEP + getSetName() + CLOSE + getSetPrice();
	}

	public String getLowestExistingPrice(){
		return Util.getLowestPriceString(getSetPrice(), getSetPriceFirst());
	}

	public String getBestExistingPrice(boolean preferFirstEdition){
		BigDecimal zero = new BigDecimal(0);

		String preferredPrice;
		String secondaryPrice;

		if(preferFirstEdition){
			preferredPrice = getSetPriceFirst();
			secondaryPrice = getSetPrice();
		}
		else{
			preferredPrice = getSetPrice();
			secondaryPrice = getSetPriceFirst();
		}


		if (preferredPrice != null && new BigDecimal(preferredPrice).compareTo(zero) != 0) {
			return preferredPrice;
		}

		if (secondaryPrice != null && new BigDecimal(secondaryPrice).compareTo(zero) != 0) {
			return secondaryPrice;
		}

		return Const.ZERO_PRICE_STRING;

	}

	public String getGamePlayCardUUID() {
		return gamePlayCardUUID;
	}

	public void setGamePlayCardUUID(String gamePlayCardUUID) {
		this.gamePlayCardUUID = gamePlayCardUUID;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public String getSetNumber() {
		return setNumber;
	}

	public void setSetNumber(String setNumber) {
		this.setNumber = setNumber;
	}

	public String getSetName() {
		return setName;
	}

	public void setSetName(String setName) {
		this.setName = setName;
	}

	public String getSetRarity() {
		return setRarity;
	}

	public void setSetRarity(String setRarity) {
		this.setRarity = setRarity;
	}

	public String getSetPrice() {
		return setPrice;
	}

	public void setSetPrice(String setPrice) {
		this.setPrice = setPrice;
	}

	public String getSetPriceUpdateTime() {
		return setPriceUpdateTime;
	}

	public void setSetPriceUpdateTime(String setPriceUpdateTime) {
		this.setPriceUpdateTime = setPriceUpdateTime;
	}

	public String getColorVariant() {
		return colorVariant;
	}

	public void setColorVariant(String colorVariant) {
		this.colorVariant = colorVariant;
	}

	public int getRarityUnsure() {
		return rarityUnsure;
	}

	public void setRarityUnsure(int rarityUnsure) {
		this.rarityUnsure = rarityUnsure;
	}

	public String getSetCode() {
		return setCode;
	}

	public void setSetCode(String setCode) {
		this.setCode = setCode;
	}

	public String getSetPriceFirst() {
		return setPriceFirst;
	}

	public void setSetPriceFirst(String setPriceFirst) {
		this.setPriceFirst = setPriceFirst;
	}

	public String getSetPriceFirstUpdateTime() {
		return setPriceFirstUpdateTime;
	}

	public void setSetPriceFirstUpdateTime(String setPriceFirstUpdateTime) {
		this.setPriceFirstUpdateTime = setPriceFirstUpdateTime;
	}

	public String getSetUrl() {
		return setUrl;
	}

	public void setSetUrl(String setUrl) {
		this.setUrl = setUrl;
	}

	public String getEditionPrinting() {
		return editionPrinting;
	}

	public void setEditionPrinting(String editionPrinting) {
		this.editionPrinting = editionPrinting;
	}
}
