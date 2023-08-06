package ygodb.commonlibrary.bean;

import ygodb.commonlibrary.utility.Util;
import ygodb.commonlibrary.utility.YGOLogger;

import java.util.List;
import java.util.Objects;

public class OwnedCard {

	private String gamePlayCardUUID;
	private String folderName;
	private String cardName;
	private int quantity;
	private String setPrefix;
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

	private List<CardSet> analyzeResultsCardSets;

	public OwnedCard() {
	}

	public OwnedCard(String folder, String cardName, String quantity, String condition, String printing, String priceBought,
			String dateBought, CardSet setIdentified, int passcode) {
		this.setFolderName(folder);
		this.setCardName(cardName);
		this.setQuantity(Integer.parseInt(quantity));
		this.setSetPrefix(setIdentified.getSetPrefix());
		this.setCondition(condition);
		this.setEditionPrinting(printing);
		this.setPriceBought(Util.normalizePrice(priceBought));
		this.setDateBought(dateBought);
		this.setSetRarity(setIdentified.getSetRarity());
		this.setGamePlayCardUUID(setIdentified.getGamePlayCardUUID());
		this.setColorVariant(setIdentified.getColorVariant());
		this.setSetName(setIdentified.getSetName());
		this.setSetNumber(setIdentified.getSetNumber());
		this.setRarityUnsure(setIdentified.getRarityUnsure());
		this.setPasscode(passcode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gamePlayCardUUID, folderName, cardName, setPrefix, setNumber, setName, setRarity, colorVariant, condition,
							editionPrinting, dateBought, priceBought, passcode);
	}

	@Override
	public boolean equals(Object other) {
		//Quantity and card UUId are ignored since they may change or be unavailable

		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof OwnedCard)) {
			return false;
		}
		OwnedCard otherOwnedCard = (OwnedCard) other;

		try {
			return setNumber.equals(otherOwnedCard.getSetNumber()) && priceBought.equals(otherOwnedCard.getPriceBought()) &&
					dateBought.equals(otherOwnedCard.getDateBought()) && folderName.equals(otherOwnedCard.getFolderName()) &&
					condition.equals(otherOwnedCard.getCondition()) && editionPrinting.equals(otherOwnedCard.getEditionPrinting()) &&
					cardName.equals(otherOwnedCard.getCardName()) && setPrefix.equals(otherOwnedCard.getSetPrefix()) &&
					colorVariant.equals(otherOwnedCard.getColorVariant()) && setRarity.equals(otherOwnedCard.getSetRarity()) &&
					setName.equals(otherOwnedCard.getSetName()) && passcode == otherOwnedCard.getPasscode() &&
					gamePlayCardUUID.equals(otherOwnedCard.getGamePlayCardUUID());
		} catch (Exception e) {
			YGOLogger.logException(e);
			throw e;
		}
	}

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

	public String getSetPrefix() {
		return setPrefix;
	}

	public void setSetPrefix(String setPrefix) {
		this.setPrefix = setPrefix;
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

	public List<CardSet> getAnalyzeResultsCardSets() {
		return analyzeResultsCardSets;
	}

	public void setAnalyzeResultsCardSets(List<CardSet> analyzeResultsCardSets) {
		this.analyzeResultsCardSets = analyzeResultsCardSets;
	}

	public int getPasscode() {
		return passcode;
	}

	public void setPasscode(int passcode) {
		this.passcode = passcode;
	}
}
