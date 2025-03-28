package ygodb.commonlibrary.bean;

public class SetMetaData {
	private String setName;
	private String setPrefix;
	private int numOfCards;
	private String tcgDate;

	public SetMetaData() {}

	public SetMetaData(String setName, String setPrefix, int numOfCards, String tcgDate) {
		this.setName = setName;
		this.setPrefix = setPrefix;
		this.numOfCards = numOfCards;
		this.tcgDate = tcgDate;
	}

	public String getSetName() {
		return setName;
	}

	public void setSetName(String setName) {
		this.setName = setName;
	}

	public String getSetPrefix() {
		return setPrefix;
	}

	public void setSetPrefix(String setPrefix) {
		this.setPrefix = setPrefix;
	}

	public int getNumOfCards() {
		return numOfCards;
	}

	public void setNumOfCards(int numOfCards) {
		this.numOfCards = numOfCards;
	}

	public String getTcgDate() {
		return tcgDate;
	}

	public void setTcgDate(String tcgDate) {
		this.tcgDate = tcgDate;
	}
}
