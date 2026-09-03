# PAS Head Skins

Client-only Fabric companion for [Player Armor Stands](https://modrinth.com/mod/player-armor-stands). Unnamed armor stands that are wearing a player head use that player's skin, slim arms included. You do not have to rename the stand.

Named stands are left alone. PAS keeps handling those the way it already does.

The extra skull on the stand is hidden once the player skin is drawing, so you do not get a head sitting on top of the player model. Optional [3D Skin Layers](https://modrinth.com/mod/3dskinlayers) support is wired through PAS so hat, jacket, sleeves, and pants follow the stand pose.

This is not a fork of Player Armor Stands. You still need PAS installed.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Player Armor Stands (`pas`)
- Java 25

3D Skin Layers is optional. If it is present, this mod uses it. If it is not, stands still get the PAS player skin.

Client-only. The server does not need this jar.

## Usage

1. Place an armor stand.
2. Leave it unnamed.
3. Put a player head on it.

PAS downloads and shows that skin. Slim vs wide arms come from the head, not from a name flag.

Rename the stand if you want PAS's normal name syntax instead (`Player`, `Player|S`, cape flags, and so on).

## Build

```bat
gradlew.bat build
```

The output jar is `build/libs/pas-head-skins-<version>.jar`. Compile-only copies of PAS and 3D Skin Layers belong in `libs/` and are not shipped in the jar.

## License

MIT. Copyright BLOODWOLF.
