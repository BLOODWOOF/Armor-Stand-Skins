# Armor Stand Skins

Fabric mod for Minecraft 26.2. An armor stand wearing a player head is drawn as that player, using the stand's pose (including [Armor Poser](https://modrinth.com/mod/armor-poser) poses). The extra skull is hidden so you do not get a head sitting on top of the player model.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

[Armor Poser](https://modrinth.com/mod/armor-poser) is highly recommended. If it is loaded, its armor stand screen gets Head Skin, Lock Skin, Cape, and Cape Source toggles under Scale. Without it, sneak and use an armor stand to open a small screen with those same toggles. Head Skin off is remembered on your client, and if this jar is also on the server it is saved with the world (not on the stand, so it does not show up in item tooltips). Lock Skin snapshots the current skin and cape so they do not follow later changes; turning it off loads the live look again, and putting a different player's head on the stand unlocks it. Cape is off by default; turning it on shows that player's cape, and an elytra uses the cape texture only while Cape is on. Cape Source picks Mojang only, Essential only, or Both (Mojang when that player has a Mojang cape equipped, otherwise Essential).

Other players need this mod too. On a dedicated server, putting this jar next to the world is still the straightforward way to share Head Skin, Lock Skin, Cape, and Cape Source. If you cannot do that but Armor Poser is on that server, survival clients can piggyback the same flags through Poser's pose sync. Vanilla players still see a normal stand with a head. Pick-block may show a larger DisabledSlots number; that is the flag channel, not a new named tag.

[3D Skin Layers](https://modrinth.com/mod/3dskinlayers) is optional. If it is present, hat, jacket, sleeves, and pants get the extra 3D overlay on skinned stands.

[Wavey Capes](https://modrinth.com/mod/wavey-capes) is optional. It only animates player capes; skinned stands keep their own cloak so Wavey Capes does not hide it.

[Essential](https://essential.gg) is optional. Cape Source can load wardrobe capes from it. Mojang-only ignores those, Essential-only uses them, and Both prefers a live Mojang cape when that player has one.

If neither this jar nor Armor Poser is on the dedicated server, flags stay on your client only.

## Usage

1. Place an armor stand.
2. Put a player head on it.

The stand uses that skin, slim or wide, from the head. Putting a player head on also turns arms on so you can give it items; turning Show Arms off hides the skin arms too. Pose it with vanilla commands or Armor Poser. Use Head Skin if you want the wooden stand back on that entity. Use Lock Skin after the look has loaded if you want to freeze the current skin and cape; unlock it to pick up the live ones again. Use Cape if you want that player's cape (or cape-textured elytra). Use Cape Source to choose Mojang, Essential, or Both.

## License

MIT. Copyright BLOODWOLF.
