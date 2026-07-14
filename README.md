# FreshwaterHaqiWorld (哈气世界生存)

A Minecraft **Forge 1.21.11** mod where vanilla melee combat is removed and you fight by
**"哈气" (haqi)** — breathing into your microphone. Your voice fires a Warden-style sonic
boom whose range and damage scale with how loud you are and with your permanently-unlocked
haqi tier. Strong mobs can haqi back, and every haqi kill is tracked on a leaderboard.

> Single mod, loaded on **both the client and a dedicated Forge server**. There is no
> Paper/Bukkit component (Forge mods cannot run on Paper).

## Requirements

- Minecraft **1.21.11**
- Minecraft **Forge 61.1.1+**
- **Java 21**
- **[Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat)** (Forge build for
  1.21.11) on both client and server — this is a **mandatory dependency** and provides the
  microphone input.

## Building

```bash
./gradlew build
```

The mod jar is produced at `build/libs/fhw-1.0.0.jar`.

For a development client/server (drop the Simple Voice Chat jar into `run/mods` first):

```bash
./gradlew runClient
./gradlew runServer
```

## How to play

1. Install this mod **and** Simple Voice Chat on the client and server.
2. You start with a **哈气 (Haqi)** item and the Basic tier unlocked.
3. Hold a haqi item and **speak/shout into your mic** — the louder you are, the longer and
   stronger the sonic boom. Vanilla weapons no longer deal damage to mobs.
4. **Upgrade** by crafting a stronger haqi (ore ring around your current haqi), then
   **sneak + right-click** it to *permanently* unlock that tier (the item is consumed).
   The unlock is kept even if you lose the item or die.
5. Run `/haqi top` to see the multiplayer kill leaderboard.

### Haqi tiers

| Item             | Recipe (ring around base)        | Comparable to | Damage* | Cooldown |
|------------------|----------------------------------|---------------|---------|----------|
| `哈气` (basic)    | (starter / creative)             | Stone sword   | 5       | 30t      |
| 升级哈气 (upgraded)| 8× Iron Ingot + 哈气             | Iron sword    | 6       | 26t      |
| 强化哈气 (enhanced)| 8× Diamond + 升级哈气            | Diamond sword | 7       | 22t      |
| 坚守者哈气 (warden)| 8× Warden Echo + 强化哈气        | End-game      | 10      | 18t      |

*Damage shown is the maximum (full-volume haqi); quiet haqi deals less. `Warden Echo` is
dropped by killing a **Warden**.

### Mobs that haqi back

Iron Golems, Wither Skeletons, Vindicators and the Ender Dragon fire their own sonic booms
at their targets. Most other mobs just emit cosmetic haqi sounds.

## Debug keybind (no microphone needed)

Press **H** (rebindable) to simulate a full-volume haqi while held — handy for testing
without speaking. Toggle off via the `enableDebugKeybind` config option.

## Configuration

See `config/fhw-common.toml` after first launch. Notable options:

- `haqiVolumeThreshold` / `haqiReferenceLevel` — mic sensitivity.
- `requireHaqiItem` — must hold a haqi item to fire.
- `removeMeleeCombat` — cancel player melee weapon damage.
- `mobsCanHaqi`, `mobHaqiCooldownTicks`, `mobHaqiDamage` — strong-mob booms.
- `leaderboardSize` — entries shown by `/haqi top`.

## Notes on placeholder assets

Item textures, the cosmetic haqi sounds (`fhw:haqi_player`, `fhw:haqi_mob`) are placeholders
(the mob/player sounds currently reuse vanilla Warden sounds). Drop real `.png` textures into
`assets/fhw/textures/item/` and real `.ogg` files referenced by `assets/fhw/sounds.json` to
replace them — no code changes required.
