# Armor Stand Skins

Fabric mod for Minecraft 26.2. An armor stand wearing a player head is drawn as that player, using the stand's pose (including [Armor Poser](https://modrinth.com/mod/armor-poser) poses). The extra skull is hidden so you do not get a head sitting on top of the player model.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

[Armor Poser](https://modrinth.com/mod/armor-poser) is optional. If it is loaded, its armor stand screen gets a Head Skin Yes/No toggle under Scale. That toggle turns the skin off for that stand only and saves on the entity (`HeadSkinDisabled`). Put this jar on the server as well if you want the flag to persist and stay in sync for other players.

[3D Skin Layers](https://modrinth.com/mod/3dskinlayers) is optional. If it is present, hat, jacket, sleeves, and pants get the extra 3D overlay on skinned stands.

Dedicated servers without this jar will not persist the flag. The Armor Poser button still flips the skin off locally for the person who clicked it.

## Usage

1. Place an armor stand.
2. Put a player head on it.

The stand uses that skin, slim or wide, from the head. Pose it with vanilla commands or Armor Poser. Use the Armor Poser toggle if you want the wooden stand back on that entity.

## Build

```bat
gradlew.bat build
```

The output jar is `build/libs/armorstand-skins-<version>.jar`. Compile-only copies of Armor Poser and 3D Skin Layers belong in `libs/` and are not shipped in the jar.

## License

MIT. Copyright BLOODWOLF.
