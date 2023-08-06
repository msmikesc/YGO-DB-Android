package ygodb.commonlibrary.bean;

public class SoldCard extends OwnedCard {

	private int sellQuantity;
	private String priceSold;
	private String dateSold;

	public SoldCard() {
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

}
