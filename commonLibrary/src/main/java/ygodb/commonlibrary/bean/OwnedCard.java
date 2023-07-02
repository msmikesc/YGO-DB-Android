package ygodb.commonlibrary.bean;

import java.util.List;

public class OwnedCard {

	private String gamePlayCardUUID;
	private String folderName;
	private String cardName;
	private int quantity;
	private String setCode;
	private String setNumber;
	private String setName;
	private String setRarity;
	private String colorVariant;
	private int rarityUnsure;
	private String condition;
	private String editionPrinting;
	private String dateBought;
	private String priceBought;
	private String creationDate;
	private String modificationDate;
	private String uuid;
	private int passcode;

	private List<String> setNamesOptions;
	private String dropdownSelectedSetNumber;
	private String dropdownSelectedRarity;
	private String dropdownSelectedSetName;
	private List<CardSet> analyzeResultsCardSets;
	private int sellQuantity;
	private String priceSold;
	private String dateSold;

	public String getGamePlayCardUUID() {
		return gamePlayCardUUID;
	}

	public void setGamePlayCardUUID(String gamePlayCardUUID) {
		this.gamePlayCardUUID = gamePlayCardUUID;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getSetCode() {
		return setCode;
	}

	public void setSetCode(String setCode) {
		this.setCode = setCode;
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

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getEditionPrinting() {
		return editionPrinting;
	}

	public void setEditionPrinting(String editionPrinting) {
		this.editionPrinting = editionPrinting;
	}

	public String getDateBought() {
		return dateBought;
	}

	public void setDateBought(String dateBought) {
		this.dateBought = dateBought;
	}

	public String getPriceBought() {
		return priceBought;
	}

	public void setPriceBought(String priceBought) {
		this.priceBought = priceBought;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(String modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	public List<String> getSetNamesOptions() {
		return setNamesOptions;
	}

	public void setSetNamesOptions(List<String> setNamesOptions) {
		this.setNamesOptions = setNamesOptions;
	}

	public String getDropdownSelectedSetNumber() {
		return dropdownSelectedSetNumber;
	}

	public void setDropdownSelectedSetNumber(String dropdownSelectedSetNumber) {
		this.dropdownSelectedSetNumber = dropdownSelectedSetNumber;
	}

	public String getDropdownSelectedRarity() {
		return dropdownSelectedRarity;
	}

	public void setDropdownSelectedRarity(String dropdownSelectedRarity) {
		this.dropdownSelectedRarity = dropdownSelectedRarity;
	}

	public String getDropdownSelectedSetName() {
		return dropdownSelectedSetName;
	}

	public void setDropdownSelectedSetName(String dropdownSelectedSetName) {
		this.dropdownSelectedSetName = dropdownSelectedSetName;
	}

	public List<CardSet> getAnalyzeResultsCardSets() {
		return analyzeResultsCardSets;
	}

	public void setAnalyzeResultsCardSets(List<CardSet> analyzeResultsCardSets) {
		this.analyzeResultsCardSets = analyzeResultsCardSets;
	}

	public int getSellQuantity() {
		return sellQuantity;
	}

	public void setSellQuantity(int sellQuantity) {
		this.sellQuantity = sellQuantity;
	}

	public String getPriceSold() {
		return priceSold;
	}

	public void setPriceSold(String priceSold) {
		this.priceSold = priceSold;
	}

	public String getDateSold() {
		return dateSold;
	}

	public void setDateSold(String dateSold) {
		this.dateSold = dateSold;
	}

	public int getPasscode() {
		return passcode;
	}

	public void setPasscode(int passcode) {
		this.passcode = passcode;
	}
}
