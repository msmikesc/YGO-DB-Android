package ygodb.commonlibrary.connection;

import ygodb.commonlibrary.bean.CardSet;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.constant.Const;
import ygodb.commonlibrary.constant.SQLConst;

import java.sql.SQLException;
import java.util.List;

public class CommonDatabaseQueries {

	private CommonDatabaseQueries(){}

	public static int updateCardSetUrl(DatabaseUpdateQuery query, String setNumber, String rarity, String setName,
								String cardName, String setURL, String colorVariant)
			throws SQLException {

		if(colorVariant == null || colorVariant.isBlank()){
			colorVariant = Const.DEFAULT_COLOR_VARIANT;
		}

		query.prepareStatement(SQLConst.UPDATE_CARD_SET_URL);

		query.bindString(1, setURL);
		query.bindString(2, setNumber);
		query.bindString(3, rarity);
		query.bindString(4, setName);
		query.bindString(5, cardName);
		query.bindString(6, colorVariant);

		return query.executeUpdate();
	}

	public static int replaceIntoGamePlayCard(DatabaseUpdateQuery query, GamePlayCard input) throws SQLException {

		String gamePlayCard = SQLConst.REPLACE_INTO_GAME_PLAY_CARD;

		query.prepareStatement(gamePlayCard);

		query.bindString(1, input.getGamePlayCardUUID());
		query.bindString(2, input.getCardName());
		query.bindString(3, input.getCardType());
		query.bindInteger(4, input.getPasscode());
		query.bindString(5, input.getDesc());
		query.bindString(6, input.getAttribute());
		query.bindString(7, input.getRace());
		query.bindString(8, input.getLinkVal());
		query.bindString(9, input.getLevel());
		query.bindString(10, input.getScale());
		query.bindString(11, input.getAtk());
		query.bindString(12, input.getDef());
		query.bindString(13, input.getArchetype());

		return query.executeUpdate();
	}

	public static <R> List<CardSet> getRaritiesOfCardInSetByGamePlayCardUUID(String gamePlayCardUUID, String setName,
			DatabaseSelectQuery <CardSet, R> query, SelectQueryResultMapper<CardSet, R> cardSetMapperSelectQuery) throws SQLException {
		query.prepareStatement(SQLConst.GET_RARITIES_OF_CARD_IN_SET_BY_GAME_PLAY_CARD_UUID);

		query.bindString(1, gamePlayCardUUID);
		query.bindString(2, setName);

		return query.executeQuery(cardSetMapperSelectQuery);
	}
}
