# Armor Stand Skins

Fabric mod for Minecraft 26.2. An armor stand wearing a player head is drawn as that player, using the stand's pose (including [Armor Poser](https://modrinth.com/mod/armor-poser) poses). The extra skull is hidden so you do not get a head sitting on top of the player model.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

[Armor Poser](https://modrinth.com/mod/armor-poser) is optional. If it is loaded, its armor stand screen gets a Head Skin Yes/No toggle under Scale, and a Lock Skin toggle under that. Head Skin off is remembered on your client, and if this jar is also on the server it is saved with the world (not on the stand, so it does not show up in item tooltips). Lock Skin snapshots the current look so it does not follow later skin changes; putting a different player's head on the stand unlocks it.

[3D Skin Layers](https://modrinth.com/mod/3dskinlayers) is optional. If it is present, hat, jacket, sleeves, and pants get the extra 3D overlay on skinned stands.

Without the jar on a dedicated server, other players still see whatever their own client draws. Putting it on the server later keeps using the same packets; flags live in world data instead of stand NBT.

## Usage

1. Place an armor stand.
2. Put a player head on it.

The stand uses that skin, slim or wide, from the head. Putting a player head on also turns arms on so you can give it items; turning Show Arms off hides the skin arms too. Pose it with vanilla commands or Armor Poser. Use Head Skin if you want the wooden stand back on that entity. Use Lock Skin after the look has loaded if you want to change your own skin without updating the stand.

## License

MIT. Copyright BLOODWOLF.
