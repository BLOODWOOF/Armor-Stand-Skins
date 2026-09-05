# Armor Stand Skins

Fabric mod for Minecraft 26.2. An armor stand wearing a player head is drawn as that player, using the stand's pose (including [Armor Poser](https://modrinth.com/mod/armor-poser) poses). The extra skull is hidden so you do not get a head sitting on top of the player model.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

[Armor Poser](https://modrinth.com/mod/armor-poser) is optional. If it is loaded, its armor stand screen gets a Head Skin Yes/No toggle under Scale. That toggle is remembered on your client, and if this jar is also on the server it is saved on the stand as `HeadSkinDisabled`.

[3D Skin Layers](https://modrinth.com/mod/3dskinlayers) is optional. If it is present, hat, jacket, sleeves, and pants get the extra 3D overlay on skinned stands.

Without the jar on a dedicated server, other players still see whatever their own client draws. Putting it on the server later uses the same NBT key, so you do not need a new format.

## Usage

1. Place an armor stand.
2. Put a player head on it.

The stand uses that skin, slim or wide, from the head. Pose it with vanilla commands or Armor Poser. Use the Armor Poser toggle if you want the wooden stand back on that entity.

## License

MIT. Copyright BLOODWOLF.
