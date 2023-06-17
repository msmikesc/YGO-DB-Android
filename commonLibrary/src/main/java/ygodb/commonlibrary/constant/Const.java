package ygodb.commonlibrary.constant;

import java.util.List;

public class Const {

    private Const (){}

    public static final String ZERO_PRICE_STRING= "0.00";
    public static final String CARD_PRINTING_FIRST_EDITION = "1st Edition";
    public static final String CARD_PRINTING_FOIL= "Foil";
    public static final String CARD_PRINTING_UNLIMITED= "Unlimited";
    public static final String CARD_PRINTING_LIMITED= "Limited";
    public static final String CARD_TYPE_SKILL= "Skill Card";
    public static final String SKILL_CARD_NAME_APPEND= " (Skill Card)";
    public static final String FOLDER_MANUAL = "Manual Folder";
    public static final String FOLDER_UNSYNCED= "UnSynced Folder";
    public static final String FOLDER_SYNC= "Sync Folder";
    public static final String ARCHETYPE_AUTOGENERATE = "autogenerated";



    //DB keys
    public static final String GAME_PLAY_CARD_UUID = "gamePlayCardUUID";
    public static final String RARITY_UNSURE = "rarityUnsure";
    public static final String QUANTITY = "quantity";
    public static final String CARD_NAME = "cardName";
    public static final String SET_CODE = "setCode";
    public static final String SET_NUMBER = "setNumber";
    public static final String SET_NAME = "setName";
    public static final String SET_RARITY = "setRarity";
    public static final String SET_RARITY_COLOR_VARIANT = "setRarityColorVariant";
    public static final String FOLDER_NAME = "folderName";
    public static final String CONDITION = "condition";
    public static final String EDITION_PRINTING = "editionPrinting";
    public static final String DATE_BOUGHT = "dateBought";
    public static final String PRICE_BOUGHT = "priceBought";
    public static final String CREATION_DATE = "creationDate";
    public static final String MODIFICATION_DATE = "modificationDate";
    public static final String UUID = "UUID";
    public static final String PASSCODE = "passcode";
    public static final String GAME_PLAY_CARD_NAME = "title";
    public static final String TYPE = "type";
    public static final String GAME_PLAY_CARD_TEXT = "lore";
    public static final String ATTRIBUTE = "attribute";
    public static final String RACE = "race";
    public static final String LINK_VALUE = "linkValue";
    public static final String LEVEL_RANK = "level";
    public static final String PENDULUM_SCALE = "pendScale";
    public static final String ATTACK = "atk";
    public static final String DEFENSE = "def";
    public static final String ARCHETYPE = "archetype";

    public static final String SET_PRICE = "setPrice";
    public static final String SET_PRICE_UPDATE_TIME = "setPriceUpdateTime";

    public static final String RELEASE_DATE = "releaseDate";


    //CSV keys
    public static final String CSV_IMPORT_FOLDER = "csv/import/";
    public static final String CSV_EXPORT_FOLDER = "csv/export/";
    public static final String CSV_ANALYZE_FOLDER = "csv/analyze/";
    public static final String FOLDER_NAME_CSV = "Folder Name";
    public static final String QUANTITY_CSV = "Quantity";
    public static final String CARD_NAME_CSV = "Card Name";
    public static final String SET_CODE_CSV = "Set Code";
    public static final String SET_NAME_CSV = "Set Name";
    public static final String CARD_NUMBER_CSV = "Card Number";
    public static final String CONDITION_CSV = "Condition";
    public static final String PRINTING_CSV = "Printing";
    public static final String PRICE_BOUGHT_CSV = "Price Bought";
    public static final String DATE_BOUGHT_CSV = "Date Bought";
    public static final String RARITY_CSV = "Rarity";
    public static final String RARITY_COLOR_VARIANT_CSV = "Rarity Color Variant";
    public static final String RARITY_UNSURE_CSV = "Rarity Unsure";
    public static final String GAME_PLAY_CARD_UUID_CSV = "GamePlay Card UUID";
    public static final String UUID_CSV = "UUID";
    public static final String PASSCODE_CSV = "passcode";
    public static final String TRADE_QUANTITY_CSV = "Trade Quantity";
    public static final String LANGUAGE_CSV = "Language";
    public static final String LOW_CSV = "LOW";
    public static final String MID_CSV = "MID";
    public static final String MARKET_CSV = "MARKET";
    public static final String CARD_TYPE_CSV = "Card Type";
    public static final String RARITIES_CSV = "Rarities";
    public static final String TCGPLAYER_MASS_BUY_3_CSV = "TCGPlayer Mass Buy 3";
    public static final String TCGPLAYER_MASS_BUY_1_CSV = "TCGPlayer Mass Buy 1";
    public static final String SET_NAMES_CSV = "Set Names";
    public static final String SET_CODES_CSV = "Set Codes";
    public static final String RELEASE_DATE_CSV = "Release Date";
    public static final String ARCHETYPE_CSV = "Archetype";
    public static final String TCGPLAYER_ITEMS_CSV = "ITEMS";
    public static final String TCGPLAYER_DETAILS_CSV = "DETAILS";
    public static final String TCGPLAYER_PRICE_CSV = "PRICE";
    public static final String TCGPLAYER_QUANTITY_CSV = "QUANTITY";
    public static final String TCGPLAYER_IMPORT_TIME = "Import Time";
    public static final String CARD_TEXT_CSV = "Card Text";
    public static final String ATTRIBUTE_CSV = "Attribute";
    public static final String RACE_CSV = "Race";
    public static final String LINK_VALUE_CSV = "Link Value";
    public static final String PENDULUM_SCALE_CSV = "Pendulum Scale";
    public static final String LEVEL_RANK_CSV = "Level/Rank";
    public static final String ATTACK_CSV = "Attack";
    public static final String DEFENSE_CSV = "Defense";


    //YGOPRO API

    public static final String YGOPRO_TOP_LEVEL_DATA = "data";
    public static final String YGOPRO_CARD_SETS = "card_sets";
    public static final String YGOPRO_CARD_NAME = "name";
    public static final String YGOPRO_CARD_TYPE = "type";
    public static final String YGOPRO_CARD_PASSCODE = "id";
    public static final String YGOPRO_CARD_TEXT = "desc";
    public static final String YGOPRO_ATTRIBUTE = "attribute";
    public static final String YGOPRO_RACE = "race";
    public static final String YGOPRO_LINK_VALUE = "linkval";
    public static final String YGOPRO_LEVEL_RANK = "level";
    public static final String YGOPRO_PENDULUM_SCALE = "scale";
    public static final String YGOPRO_ATTACK = "atk";
    public static final String YGOPRO_DEFENSE = "def";
    public static final String YGOPRO_ARCHETYPE = "archetype";
    public static final String YGOPRO_SET_CODE = "set_code";
    public static final String YGOPRO_SET_NAME = "set_name";
    public static final String YGOPRO_SET_RARITY = "set_rarity";
    public static final String YGOPRO_SET_PRICE = "set_price";
    public static final String YGOPRO_TOTAL_CARDS_IN_SET = "num_of_cards";
    public static final String YGOPRO_TCG_RELEASE_DATE = "tcg_date";

    public static final String DEFAULT_COLOR_VARIANT = "-1";
	public static final List<String> setColorVariantUnsupportedDragonShield = List.of("Legendary Duelists: Season 2");
}
