package ygodb.commonlibrary.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Rarity {

	COMMON("Common"),
	SHORT_PRINT("Short Print"),
	SUPER_SHORT_PRINT("Super Short Print"),
	RARE("Rare"),
	SUPER_RARE("Super Rare"),
	ULTRA_RARE("Ultra Rare"),
	SECRET_RARE("Secret Rare"),
	PRISMATIC_SECRET_RARE("Prismatic Secret Rare"),
	EMBLAZONED_ULTRA_RARE("Emblazoned Ultra Rare"),
	EMBLAZONED_SECRET_RARE("Emblazoned Secret Rare"),
	GOLD_RARE("Gold Rare"),
	PREMIUM_GOLD_RARE("Premium Gold Rare"),
	GOLD_SECRET_RARE("Gold Secret Rare"),
	STARFOIL_RARE("Starfoil Rare"),
	SHATTERFOIL_RARE("Shatterfoil Rare"),
	MOSAIC_RARE("Mosaic Rare"),
	NORMAL_PARALLEL_RARE("Normal Parallel Rare"),
	SUPER_PARALLEL_RARE("Super Parallel Rare"),
	ULTRA_PARALLEL_RARE("Ultra Parallel Rare"),
	DUEL_TERMINAL_NORMAL_PARALLEL_RARE("Duel Terminal Normal Parallel Rare"),
	DUEL_TERMINAL_RARE_PARALLEL_RARE("Duel Terminal Rare Parallel Rare"),
	DUEL_TERMINAL_SUPER_PARALLEL_RARE("Duel Terminal Super Parallel Rare"),
	DUEL_TERMINAL_ULTRA_PARALLEL_RARE("Duel Terminal Ultra Parallel Rare"),
	ULTIMATE_RARE("Ultimate Rare"),
	PRISMATIC_ULTIMATE_RARE("Prismatic Ultimate Rare"),
	COLLECTORS_RARE("Collector's Rare"),
	PRISMATIC_COLLECTORS_RARE("Prismatic Collector's Rare"),
	PLATINUM_SECRET_RARE("Platinum Secret Rare"),
	QUARTER_CENTURY_SECRET_RARE("Quarter Century Secret Rare"),
	STARLIGHT_RARE("Starlight Rare"),
	GHOST_RARE("Ghost Rare"),
	GHOST_GOLD_RARE("Ghost/Gold Rare"),
	PLATINUM_RARE("Platinum Rare"),
	ULTRA_SECRET_RARE("Ultra Secret Rare"),
	EXTRA_SECRET_RARE("Extra Secret Rare"),
	ULTRA_RARE_PHAROHS_RARE("Ultra Rare (Pharaoh's Rare)"),
	SECRET_RARE_PHAROHS_RARE("Secret Rare (Pharaoh's Rare)"),
	SECRET_RARE_TEN_THOUSAND("10000 Secret Rare"),
	MILLENNIUM_SECRET_RARE("Millennium Secret Rare"),
	OVERSIZED("Oversized"),
	NULL_RARITY("nullRarity");

	private final String name;

	Rarity(String s) {
		name = s;
	}

	@Override
	public String toString() {
		return name;
	}

	public static Rarity fromString(String text) {
		for (Rarity b : Rarity.values()) {
			if (b.name.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return NULL_RARITY;
	}

	public static final Set<String> androidShinyRarities =
			Set.of(QUARTER_CENTURY_SECRET_RARE.toString(), MILLENNIUM_SECRET_RARE.toString(), ULTIMATE_RARE.toString(), STARLIGHT_RARE.toString(),
				   SECRET_RARE_TEN_THOUSAND.toString(), SECRET_RARE_PHAROHS_RARE.toString(), ULTRA_RARE_PHAROHS_RARE.toString(),
				   COLLECTORS_RARE.toString(), PRISMATIC_COLLECTORS_RARE.toString(), PRISMATIC_ULTIMATE_RARE.toString(), PLATINUM_SECRET_RARE.toString());

	public static List<String> getSortedListFromCollection(Collection<String> raritySet){
		ArrayList<Rarity> enumList = new ArrayList<>();

		for (String s : raritySet) {
			Rarity rarityValue = Rarity.fromString(s);
			enumList.add(rarityValue);
		}

		Collections.sort(enumList);

		return enumList.stream()
				.map(Object::toString)
				.collect(Collectors.toList());
	}

}
