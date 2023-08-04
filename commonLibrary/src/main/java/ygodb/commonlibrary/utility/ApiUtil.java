package ygodb.commonlibrary.utility;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.util.Pair;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.bean.OwnedCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.constant.Const;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class ApiUtil {

	private ApiUtil() {
	}

	public static String getStringOrNull(JsonNode current, String id) {
		try {
			return current.get(id).asText().trim();
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getIntOrNegativeOne(JsonNode current, String id) {
		try {
			return current.get(id).asInt();
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getApiResponseFromURL(URL url) throws IOException {
		String inline = "";
		InputStream inputStreamFromURL = null;
		try {
			inputStreamFromURL = url.openStream();

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = inputStreamFromURL.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			inline = result.toString(StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			YGOLogger.logException(e);
			throw e;
		} finally {
			if (inputStreamFromURL != null) {
				try {
					inputStreamFromURL.close();
				} catch (IOException e) {
					YGOLogger.logException(e);
				}
			}
		}

		return inline;
	}

	public static GamePlayCard replaceIntoGameplayCardFromYGOPRO(JsonNode current, List<OwnedCard> ownedCardsToCheck,
			SQLiteConnection db) throws SQLException {

		String name = getStringOrNull(current, Const.YGOPRO_CARD_NAME);
		String type = getStringOrNull(current, Const.YGOPRO_CARD_TYPE);
		Integer passcode = getIntOrNegativeOne(current, Const.YGOPRO_CARD_PASSCODE);
		String desc = getStringOrNull(current, Const.YGOPRO_CARD_TEXT);
		String attribute = getStringOrNull(current, Const.YGOPRO_ATTRIBUTE);
		String race = getStringOrNull(current, Const.YGOPRO_RACE);
		String linkValue = getStringOrNull(current, Const.YGOPRO_LINK_VALUE);
		String level = getStringOrNull(current, Const.YGOPRO_LEVEL_RANK);
		String scale = getStringOrNull(current, Const.YGOPRO_PENDULUM_SCALE);
		String atk = getStringOrNull(current, Const.YGOPRO_ATTACK);
		String def = getStringOrNull(current, Const.YGOPRO_DEFENSE);
		String archetype = getStringOrNull(current, Const.YGOPRO_ARCHETYPE);

		GamePlayCard gamePlayCard = new GamePlayCard();

		name = Util.checkForTranslatedCardName(name);
		passcode = Util.checkForTranslatedPasscode(passcode);

		gamePlayCard.setCardName(name);
		gamePlayCard.setCardType(type);
		gamePlayCard.setArchetype(archetype);
		gamePlayCard.setPasscode(passcode);

		gamePlayCard.setGamePlayCardUUID(db.getGamePlayCardUUIDFromPasscode(passcode));

		if (gamePlayCard.getGamePlayCardUUID() == null) {
			Pair<String, String> uuidAndName = Util.getGamePlayCardUUIDFromTitleOrGenerateNewWithSkillCheck(name, db);

			gamePlayCard.setGamePlayCardUUID(uuidAndName.getKey());
			gamePlayCard.setCardName(uuidAndName.getValue());
		}

		gamePlayCard.setDesc(desc);
		gamePlayCard.setAttribute(attribute);
		gamePlayCard.setRace(race);
		gamePlayCard.setLinkVal(linkValue);
		gamePlayCard.setScale(scale);
		gamePlayCard.setLevel(level);
		gamePlayCard.setAtk(atk);
		gamePlayCard.setDef(def);

		db.replaceIntoGamePlayCard(gamePlayCard);

		for (OwnedCard currentOwnedCard : ownedCardsToCheck) {
			if (currentOwnedCard.getGamePlayCardUUID().equals(gamePlayCard.getGamePlayCardUUID())) {
				currentOwnedCard.setPasscode(passcode);
				db.updateOwnedCardByUUID(currentOwnedCard);
			}
		}

		return gamePlayCard;
	}

	public static void insertOrIgnoreCardSetsForOneCard(JsonNode setListNode, String cardName, String gamePlayCardUUID,
			SQLiteConnection db) throws SQLException {

		for (JsonNode currentSetNode : setListNode) {

			String setCode = null;
			String setName = null;
			String setRarity = null;

			try {
				setCode = getStringOrNull(currentSetNode, Const.YGOPRO_SET_CODE);
				setName = getStringOrNull(currentSetNode, Const.YGOPRO_SET_NAME);
				setRarity = getStringOrNull(currentSetNode, Const.YGOPRO_SET_RARITY);
			} catch (Exception e) {
				YGOLogger.error("issue found on " + cardName);
				continue;
			}

			cardName = Util.checkForTranslatedCardName(cardName);
			setRarity = Util.checkForTranslatedRarity(setRarity);
			setName = Util.checkForTranslatedSetName(setName);
			setCode = Util.checkForTranslatedSetNumber(setCode);

			List<String> translatedList = Util.checkForTranslatedQuadKey(cardName, setCode, setRarity, setName);
			cardName = translatedList.get(0);
			setCode = translatedList.get(1);
			setRarity = translatedList.get(2);
			setName = translatedList.get(3);

			db.insertOrIgnoreIntoCardSet(setCode, setRarity, setName, gamePlayCardUUID, cardName, null, null);
		}
	}
}
