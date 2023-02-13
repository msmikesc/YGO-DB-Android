package com.example.ygodb.backend.bean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnalyzeData implements Comparable<AnalyzeData> {

	public int quantity;
	public String cardName;
	public Set<String> setNumber;
	public Set<String> setName;
	public Set<String> setRarities;
	public String cardType;
	public Set<String> mainSetNumber;
	public Set<String> mainSetRarities;
	public BigDecimal cardPriceAverage;

	public String mainSetName;
	public String mainSetCode;

	public int id;


	public AnalyzeData() {
		setNumber = new HashSet<String>();
		setName = new HashSet<String>();
		setRarities = new HashSet<String>();

		mainSetRarities = new HashSet<String>();
		mainSetNumber = new HashSet<String>();
		cardPriceAverage = new BigDecimal(0);
		cardPriceAverage = cardPriceAverage.setScale(2, RoundingMode.HALF_UP);

	}

	public String getAveragePrice(){
		if(quantity == 0){
			return "0.00";
		}

		return cardPriceAverage.toString();
	}

	@Override
	public int compareTo(AnalyzeData o) {

		int compare = 0;
		
		compare = Integer.valueOf(this.quantity).compareTo(Integer.valueOf(o.quantity));

		if (compare != 0) {
			return compare;
		}

		compare = this.getStringOfRarities().compareTo(o.getStringOfRarities());

		if (compare != 0) {
			return compare;
		}

		compare = this.getStringOfSetNames().compareTo(o.getStringOfSetNames());

		if (compare != 0) {
			return compare;
		}

		return cardName.compareTo(o.cardName);
	}

	public String getStringOfSetNames() {
		
		if(setName.size() == 0) {
			return "";
		}

		ArrayList<String> results = new ArrayList<String>(setName);

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfSetNumbers() {
		
		if(setNumber.size() == 0) {
			return "";
		}
		
		ArrayList<String> results = new ArrayList<String>(setNumber);

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.size() == 0){
			return "None Found";
		}

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfMainSetNumbers() {

		if(mainSetNumber.size() == 0) {
			return "";
		}

		ArrayList<String> results = new ArrayList<String>(mainSetNumber);

		if(results.size() == 1){
			return results.get(0);
		}

		if(results.size() == 0){
			return "None Found";
		}

		Collections.sort(results);

		String output = results.get(0);

		for (int i = 1; i < results.size(); i++) {
			output += ", " + results.get(i);
		}

		return output;
	}

	public String getStringOfRarities() {
		
		if(setRarities.size() == 0) {
			return "";
		}
		
		HashSet<Rarity> enumList = new HashSet<Rarity>();

		for (String s : setRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<Rarity>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.size() == 0){
			return "None Found";
		}

		Collections.sort(enumList2);

		String output = enumList2.get(0).toString();

		for (int i = 1; i < enumList.size(); i++) {
			output += ", " + enumList2.get(i).toString();
		}

		return output;

	}

	public String getStringOfMainRarities() {

		if(setRarities.size() == 0) {
			return "";
		}

		HashSet<Rarity> enumList = new HashSet<Rarity>();

		for (String s : mainSetRarities) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		ArrayList<Rarity> enumList2 = new ArrayList<Rarity>(enumList);

		if(enumList2.size() == 1){
			return enumList2.get(0).toString();
		}

		if(enumList2.size() == 0){
			return "None Found";
		}

		Collections.sort(enumList2);

		String output = enumList2.get(0).toString();

		for (int i = 1; i < enumList.size(); i++) {
			output += ", " + enumList2.get(i).toString();
		}

		return output;

	}

}
