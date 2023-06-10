package ygodb.windows.utility;

import ygodb.commonLibrary.utility.Util;
import ygodb.windows.connection.SQLiteConnectionWindows;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WindowsUtil {

    private static SQLiteConnectionWindows dbInstance = null;

    public static SQLiteConnectionWindows getDBInstance(){
        if (dbInstance == null){
            dbInstance = new SQLiteConnectionWindows();
        }

        return dbInstance;
    }

    private static KeyUpdateMap setNameMap = null;
    private static HashMap<String, String> rarityMap = null;
    private static HashMap<String, String> setNumberMap = null;
    private static HashMap<String, String> cardNameMap = null;
    private static HashMap<Integer, Integer> passcodeMap = null;

    private static QuadKeyUpdateMap quadKeyUpdateMap = null;

    public static QuadKeyUpdateMap getQuadKeyUpdateMapInstance() {
        if (quadKeyUpdateMap == null) {

            try {
                String filename = "quadUpdateMapping.csv";

                InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

                quadKeyUpdateMap = new QuadKeyUpdateMap(inputStream, "|");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        return quadKeyUpdateMap;
    }

    public static List<String> checkForTranslatedQuadKey(String cardName, String setNumber, String rarity, String setName) {
        QuadKeyUpdateMap instance = getQuadKeyUpdateMapInstance();

        return instance.getValues(cardName, setNumber, rarity, setName);
    }

    public static KeyUpdateMap getSetNameMapInstance() {
        if (setNameMap == null) {

            try {
                String filename = "setNameUpdateMapping.csv";

                InputStream inputStream = Util.class.getResourceAsStream("/" + filename);

                setNameMap = new KeyUpdateMap(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        return setNameMap;
    }

    public static Map<String, String> getRarityMapInstance() {
        if (rarityMap == null) {
            rarityMap = new HashMap<>();

            rarityMap.put("Collectors Rare", "Collector's Rare");
            rarityMap.put("URPR", "Ultra Rare (Pharaoh's Rare)");
            rarityMap.put("Super Short Print", "Short Print");
            rarityMap.put("SSP", "Short Print");
            rarityMap.put("Duel Terminal Technology Common", "Duel Terminal Normal Parallel Rare");
            rarityMap.put("Secret Pharaohâ€™s Rare", "Secret Rare (Pharaoh's Rare)");
            rarityMap.put("Ultra Pharaohâ€™s Rare", "Ultra Rare (Pharaoh's Rare)");
            rarityMap.put("Duel Terminal Technology Ultra Rare", "Duel Terminal Ultra Parallel Rare");
            rarityMap.put("Ultra Pharaoh’s Rare", "Ultra Rare (Pharaoh's Rare)");
            rarityMap.put("Secret Pharaoh’s Rare", "Secret Rare (Pharaoh's Rare)");

            //rarityMap.put("", "");

        }

        return rarityMap;
    }

    public static Map<String, String> getSetNumberMapInstance() {
        if (setNumberMap == null) {
            setNumberMap = new HashMap<>();

            setNumberMap.put("GTP2-EN176", "GFP2-EN176");
            setNumberMap.put("SSD-E001", "SDD-E001");
            setNumberMap.put("SSD-E002", "SDD-E002");
            setNumberMap.put("SSD-E003", "SDD-E003");
            setNumberMap.put("OTPT-EN001", "OPTP-EN001");

            //setNumberMap.put("", "");

        }

        return setNumberMap;
    }

    public static Map<Integer, Integer> getPasscodeMapInstance() {
        if (passcodeMap == null) {
            passcodeMap = new HashMap<>();

            passcodeMap.put(74677427, 74677422);
            passcodeMap.put(89943724, 89943723);


            //passcodeMap.put("", "");

        }

        return passcodeMap;
    }

    public static Map<String, String> getCardNameMapInstance() {
        if (cardNameMap == null) {
            cardNameMap = new HashMap<>();

            cardNameMap.put("after genocide","After the Struggle");
            cardNameMap.put("amazon archer" ,"Amazoness Archer");
            cardNameMap.put("armityle the chaos phantom","Armityle the Chaos Phantasm");
            cardNameMap.put("big core" ,"B.E.S. Big Core");
            cardNameMap.put("cliff the trap remover","Dark Scorpion - Cliff the Trap Remover");
            cardNameMap.put("dark assassin","Dark Assailant");
            cardNameMap.put("dark trap hole","Darkfall");
            cardNameMap.put("forbidden graveyard","Silent Graveyard");
            cardNameMap.put("frog the jam","Slime Toad");
            cardNameMap.put("harpie's brother","Sky Scout");
            cardNameMap.put("hidden book of spell","Hidden Spellbook");
            cardNameMap.put("judgment of the pharaoh","Judgment of Pharaoh");
            cardNameMap.put("kinetic soldier","Cipher Soldier");
            cardNameMap.put("marie the fallen one","Darklord Marie");
            cardNameMap.put("metaphysical regeneration","Supernatural Regeneration");
            cardNameMap.put("null and void","Muko");
            cardNameMap.put("nurse reficule the fallen one","Darklord Nurse Reficule");
            cardNameMap.put("oscillo hero #2","Wattkid");
            cardNameMap.put("pigeonholing books of spell","Spellbook Organization");
            cardNameMap.put("red-eyes b. chick","Black Dragon's Chick");
            cardNameMap.put("red-eyes b. dragon","Red-Eyes Black Dragon");
            cardNameMap.put("red-moon baby","Vampire Baby");
            cardNameMap.put("trial of hell","Trial of Nightmare");
            cardNameMap.put("d. d. assailant","D.D. Assailant");
            cardNameMap.put("d. d. borderline","D.D. Borderline");
            cardNameMap.put("d. d. designator","D.D. Designator");
            cardNameMap.put("d. d. scout plane","D.D. Scout Plane");
            cardNameMap.put("d. d. trainer","D.D. Trainer");
            cardNameMap.put("d. d. warrior lady","D.D. Warrior Lady");
            cardNameMap.put("gradius's option","Gradius' Option");
            cardNameMap.put("hundred-eyes dragon","Hundred Eyes Dragon");
            cardNameMap.put("necrolancer the timelord","Necrolancer the Time-lord");
            cardNameMap.put("sephylon,the Ultimate Time Lord","Sephylon, the Ultimate Timelord");
            cardNameMap.put("winged dragon,Guardian of the Fortress #1","Winged Dragon, Guardian of the Fortress #1");
            cardNameMap.put("blackwing  armed wing","Blackwing Armed Wing");
            cardNameMap.put("b. skull dragon","Black Skull Dragon");

            //cardNameMap.put("", "");

        }

        return cardNameMap;
    }

    public static String flipStructureEnding(String input, String match) {

        input = input.trim();

        if(input.endsWith(match)) {
            input = match + ": " + input.replace(match, "").trim();
        }
        return input;

    }


    public static String checkForTranslatedSetName(String setName) {

        if(setName.contains("The Lost Art Promotion")) {
            setName = "The Lost Art Promotion";
        }

        if(setName.contains("(Worldwide English)")) {
            setName = setName.replace("(Worldwide English)", "");
            setName = setName.trim();
        }

        if(setName.contains("Sneak Peek Participation Card")) {
            setName = setName.replace("Sneak Peek Participation Card", "");
            setName = setName.trim();
        }

        if(setName.contains(": Special Edition")) {
            setName = setName.replace(": Special Edition", "");
            setName = setName.trim();
        }

        if(setName.contains("Special Edition")) {
            setName = setName.replace("Special Edition", "");
            setName = setName.trim();
        }

        if(setName.contains(": Super Edition")) {
            setName = setName.replace(": Super Edition", "");
            setName = setName.trim();
        }

        if(setName.contains("Super Edition")) {
            setName = setName.replace("Super Edition", "");
            setName = setName.trim();
        }

        if(!setName.equals("Structure Deck: Deluxe Edition") && setName.contains(": Deluxe Edition")) {
            setName = setName.replace(": Deluxe Edition", "");
            setName = setName.trim();
        }

        if(!setName.equals("Structure Deck: Deluxe Edition") && setName.contains("Deluxe Edition")) {
            setName = setName.replace("Deluxe Edition", "");
            setName = setName.trim();
        }

        if(setName.contains("Premiere! promotional card")) {
            setName = setName.replace("Premiere! promotional card", "");
            setName = setName.trim();
        }

        if(setName.contains("Launch Event participation card")) {
            setName = setName.replace("Launch Event participation card", "");
            setName = setName.trim();
        }

        if(setName.endsWith(" SE")) {
            setName = setName.substring(0, setName.length()-3);
            setName = setName.trim();
        }

        setName = flipStructureEnding(setName, "Starter Deck");
        setName = flipStructureEnding(setName, "Structure Deck");

        return getSetNameMapInstance().getValue(setName);
    }

    public static String checkForTranslatedRarity(String rarity) {
        Map<String, String> instance = getRarityMapInstance();

        String newRarity = instance.get(rarity);

        if(newRarity == null) {
            return rarity;
        }

        return newRarity;
    }

    public static String checkForTranslatedSetNumber(String setNumber) {
        Map<String, String> instance = getSetNumberMapInstance();

        String newSetNumber = instance.get(setNumber);

        if(newSetNumber == null) {
            return setNumber;
        }

        return newSetNumber;
    }



    public static String checkForTranslatedCardName(String cardName) {
        Map<String, String> instance = getCardNameMapInstance();

        String newName = instance.get(cardName.toLowerCase(Locale.ROOT));

        if(newName == null) {
            return cardName;
        }

        return newName;
    }

    public static int checkForTranslatedPasscode(int passcode) {
        Map<Integer, Integer> instance = getPasscodeMapInstance();

        Integer newPasscode = instance.get(passcode);

        if(newPasscode == null) {
            return passcode;
        }

        return newPasscode;
    }
}
