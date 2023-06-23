package ygodb.commonlibrary.bean;

public class SetMetaData {
	private String setName;
	private String setCode;
	private int numOfCards;
	private String tcgDate;

	public String getSetName() {
		return setName;
	}

	public void setSetName(String setName) {
		this.setName = setName;
	}

	public String getSetCode() {
		return setCode;
	}

	public void setSetCode(String setCode) {
		this.setCode = setCode;
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
