Donation-scaled dungeon loot (Paper plugin + datapack marker)

Goal
- Make dungeon chest loot scale with the current charity total.
- Keep worldgen via datapack, but allow dynamic tuning from a Paper plugin.

Approach overview
1) Datapack side (this repository):
   - All dungeon chests are stamped with LootTable: lastlife:chests/dungeon_scaled by the processor list.
   - We provide three static tier tables: lastlife:chests/dungeon_tier_low, _mid, _high.
   - The marker table lastlife:chests/dungeon_scaled defaults to the low tier so it works even without a plugin.

2) Paper plugin side (server):
   - Listen to LootGenerateEvent for chests whose LootTable key == lastlife:chests/dungeon_scaled.
   - Read the current charity total (e.g., from plugins/lastlife/donations.json) and map to a tier.
   - Replace/augment the generated loot by drawing from the appropriate tier table, or just construct items programmatically.

Why this design?
- Vanilla datapacks cannot read arbitrary server-side values (like donation totals) at loot time.
- LootGenerateEvent gives you full control over the final items while still benefiting from vanilla randomization.

File map (in this repo)
- data path here is under: lastlife/data/lastlife
  - processor_list/dungeon_chest_loot.json → stamps LootTable to lastlife:chests/dungeon_scaled
  - chests/dungeon_scaled.json → marker table (defaults to low tier)
  - chests/dungeon_tier_low.json → bread/apples/coal/etc.
  - chests/dungeon_tier_mid.json → iron/gold/lapis/enchanted book
  - chests/dungeon_tier_high.json → diamonds/emeralds/netherite goodies

Paper listener sketch (Java)

// In your Paper plugin (server module), register this listener onEnable
@EventHandler(ignoreCancelled = true)
public void onLoot(LootGenerateEvent event) {
    if (event.getLootTable() == null) return;
    NamespacedKey key = event.getLootTable().getKey();
    if (key == null) return;

    // Only target our dungeon chests
    if (!key.getNamespace().equals("lastlife") || !key.getKey().equals("chests/dungeon_scaled")) return;

    // Determine the tier from donations
    long donations = DonationService.getCurrentTotal(); // implement this for your plugin
    Tier tier = mapDonationsToTier(donations);

    // Option A: replace with a different loot table
    NamespacedKey tierKey = switch (tier) {
        case LOW -> new NamespacedKey("lastlife", "chests/dungeon_tier_low");
        case MID -> new NamespacedKey("lastlife", "chests/dungeon_tier_mid");
        case HIGH -> new NamespacedKey("lastlife", "chests/dungeon_tier_high");
    };

    LootTable table = Bukkit.getLootTable(tierKey);
    if (table != null) {
        // Generate items from the tier table into this event
        List<ItemStack> generated = table.populateLoot(event.getLootContext());
        event.getLoot().clear();
        event.getLoot().addAll(generated);
        return;
    }

    // Option B: manual injection if you want full control
    // event.getLoot().clear();
    // event.getLoot().add(new ItemStack(Material.DIAMOND, 3));
}

// Helper: donation tiers (example)
private Tier mapDonationsToTier(long amount) {
    if (amount < 1000) return Tier.LOW;      // < $1k
    if (amount < 10000) return Tier.MID;     // $1k–$10k
    return Tier.HIGH;                        // $10k+
}

enum Tier { LOW, MID, HIGH }

Donation source
- Example path (seen in dev): plugins/lastlife/donations.json
- Keep it simple: { "total": 12345 }
- Implement DonationService to read this file periodically or hit your donation API.

Testing
1) Ensure the datapack in this jar is loaded (ships inside the plugin/mod). New chests placed by the dungeon will have LootTable: lastlife:chests/dungeon_scaled.
2) With no plugin listener, chests will use the low tier (safe default).
3) Enable your Paper plugin with the listener above and set donations.json to different totals.
4) Generate a few dungeons (/place structure or explore new chunks) and open multiple chests. Loot will scale with your configured total.

Notes
- LootGenerateEvent fires server-side and works with Paper 1.20–1.21+.
- If you prefill items in NBT for structure pieces, the LootTable tag will overwrite that; keep NBT empty for chests.
- You can refine tiers, add more tier tables, or mix-in entries by editing the JSON files here.
