LastLife "Dungeon" (Jigsaw) – How To Build Your Own
====================================================

This project ships a built‑in datapack skeleton for a modular, Jigsaw‑driven dungeon similar to Vault Hunters vaults. You only need to export your room pieces as NBT using Structure Blocks, drop them into the expected paths, and reload.

What you get out of the box
- A registered jigsaw structure: lastlife:lastlife_dungeon
- Worldgen structure set for frequency/spacing
- Biome tag so it can spawn in overworld biomes
- Template pools (start, rooms) that reference NBTs you will provide

Folder layout (inside the mod, also works as a datapack)
- data/lastlife/worldgen/structure/lastlife_dungeon.json – structure definition
- data/lastlife/worldgen/structure_set/lastlife_dungeon.json – placement (spacing/separation)
- data/lastlife/worldgen/template_pool/lastlife/dungeon/
  - start.json – picks your dungeon entrance piece
  - rooms.json – picks room pieces during expansion
- data/minecraft/tags/worldgen/biome/has_structure/lastlife_dungeon.json – allows generation in overworld biomes
- EXPECTED NBT LOCATIONS you will create:
  - data/lastlife/structures/dungeon/entrances/entrance_1.nbt
  - data/lastlife/structures/dungeon/rooms/room_1.nbt
  - data/lastlife/structures/dungeon/rooms/room_2.nbt

Minecraft versions
- Targets modern 1.20+ style worldgen JSONs. If you’re on a different version, keys may differ slightly (placement, biome tags). The defaults here work on 1.20.x Fabric.

Step-by-step: Make your first dungeon
1) Build your entrance and rooms in a test world
   - Use creative mode. Enable cheats.
   - Place a Structure Block in SAVE mode to capture each piece.
   - Place Jigsaw Blocks where you want connections.
     - For the entrance piece, set one of the jigsaw blocks name to lastlife:entrance (it is looked up by start_jigsaw_name).
     - For connectors, choose a consistent target pool name, e.g. lastlife:dungeon/rooms or lastlife:dungeon/corridors if you add more pools later.
     - For each Jigsaw Block, set:
       - target pool: e.g. lastlife:dungeon/rooms
       - name (this piece’s connector name), e.g. lastlife:room
       - target name (what it wants to connect to), e.g. lastlife:room
       - joint type: rigid is most typical for dungeons
   - Set your piece bounding boxes cleanly and avoid liquids; use solid floors.

2) Export NBT files
   - With the Structure Block (SAVE):
     - Structure Name becomes the path (no extension). Example:
       - dungeon/entrances/entrance_1
       - dungeon/rooms/room_1
       - dungeon/rooms/room_2
     - Click “Save” to write <name>.nbt into your world save folder: saves/<WorldName>/generated/minecraft/structures/<path>.nbt
   - Copy those .nbt files into the mod’s resources so they become part of the built‑in datapack at runtime:
     - src/main/resources/data/lastlife/structures/dungeon/entrances/entrance_1.nbt
     - src/main/resources/data/lastlife/structures/dungeon/rooms/room_1.nbt
     - src/main/resources/data/lastlife/structures/dungeon/rooms/room_2.nbt

3) Wire pools and connectors
   - start.json already points at lastlife:dungeon/entrances/entrance_1
   - rooms.json points at two sample rooms; add more entries or new pools as you build out.
   - Ensure your jigsaw connectors agree:
     - Entrance piece has a connector whose target pool is lastlife:dungeon/rooms and whose target name matches the room pieces’ jigsaw name (e.g., lastlife:room)
     - Each room piece should place outgoing connectors pointing back to lastlife:dungeon/rooms (or to other pools like corridors/terminators if you add them), so the structure can expand.

4) Tune generation
   - Size (depth): data/lastlife/worldgen/structure/lastlife_dungeon.json -> size: 7 controls branching
   - Start height: uniform 20..40; change to fit your design
   - Terrain adaptation: beard_thin blends edges; set to none if underground
   - Placement density: data/lastlife/worldgen/structure_set/lastlife_dungeon.json -> spacing/separation

5) Test in-game (dev run or a normal client including this mod)
   - Create a new world (or delete region files) after changing placement to see new chunks.
   - Use commands:
     - /locate structure lastlife:lastlife_dungeon
     - /place structure lastlife:lastlife_dungeon ~ ~ ~ (for quick sanity checks)
   - If nothing generates:
     - Check that your NBTs exist at data/lastlife/structures/... and names match the template pool JSONs
     - Verify your Jigsaw Blocks’ target pool and names match across pieces
     - Watch the log for errors about missing pool elements

Expanding like Vaults
- Add more template pools: corridors.json, intersections.json, dead_ends.json, treasure_rooms.json
- Use processors (data/lastlife/worldgen/processor_list/...) to randomize blocks (e.g., crack stone bricks, replace cobblestone, add spawners)
- Use bounding boxes and jigsaw alignment so pieces fit seamlessly
- Control rarity by adjusting weights in the pool elements array

Common pitfalls
- Mismatched jigsaw names (name vs target name) prevent connections
- Missing NBTs (paths must match exactly the location fields in template pools)
- Not resetting world or moving to new chunks after changing structure placement
- Using wrong namespace (must be lastlife: for files in this mod)

Advanced: Separate datapack instead of built‑in
- Everything here can live in a stand‑alone datapack placed into <world>/datapacks/your_pack
- Use the same folder structure beginning with data/your_namespace/...

License/ownership
- This skeleton is provided to help you bootstrap your dungeon. The rooms you create are yours.
