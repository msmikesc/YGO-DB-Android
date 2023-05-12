package ygodb.commonLibrary.bean;

public enum Rarity {

	Common("Common"),
	ShortPrint("Short Print"),
	SuperShortPrint("Super Short Print"),
	Rare("Rare"),
	SuperRare("Super Rare"),
	UltraRare("Ultra Rare"),
	SecretRare("Secret Rare"),
	DuelTerminalNormalParallelRare("Duel Terminal Normal Parallel Rare"),
	StarfoilRare("Starfoil Rare"),
	GoldRare("Gold Rare"),
	ShatterfoilRare("Shatterfoil Rare"),
	MosaicRare("Mosaic Rare"),
	PrismaticSecretRare("Prismatic Secret Rare"),
	DuelTerminalRareParallelRare("Duel Terminal Rare Parallel Rare"),
	PremiumGoldRare("Premium Gold Rare"),
	GoldSecretRare("Gold Secret Rare"),
	CollectorsRare("Collector's Rare"),
	DuelTerminalSuperParallelRare("Duel Terminal Super Parallel Rare"),
	DuelTerminalUltraParallelRare("Duel Terminal Ultra Parallel Rare"),
	Starfoil("Starfoil"),
	PlatinumRare("Platinum Rare"),
	UltraParallelRare("Ultra Parallel Rare"),
	NormalParallelRare("Normal Parallel Rare"),
	GhostGoldRare("Ghost/Gold Rare"),
	ExtraSecretRare("Extra Secret Rare"),
	PlatinumSecretRare("Platinum Secret Rare"),
	SuperParallelRare("Super Parallel Rare"),
	UltraSecretRare("Ultra Secret Rare"),
	DuelTerminalNormalRareParallelRare("Duel Terminal Normal Rare Parallel Rare"),
	SecretRareTenThousand("10000 Secret Rare"),
	UltraRarePharohsRare("Ultra Rare (Pharaoh's Rare)"),
	SecretRarePharohsRare("Secret Rare (Pharaoh's Rare)"),
	StarlightRare("Starlight Rare"),
	GhostRare("Ghost Rare"),
	UltimateRare("Ultimate Rare"),
	QuarterCenturySecretRare("Quarter Century Secret Rare"),
	MillenniumSecretRare("Millennium Secret Rare"),
	nullRarity("nullRarity");

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
        return nullRarity;
    }

}
