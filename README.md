# Sunlit Compatible Skins — ALPHA v0.2

A clean replacement project for Society Sunlit Cobblemon on Forge 1.20.1 / Cobblemon 1.5.2 / Java 17.

This project starts from the original Cobblemon 1.5.2 visual system used by the pack. It does **not** import replacement Pokémon models, posers, or animation groups. Static skins are admitted only when their texture dimensions and alpha/UV layout closely match the corresponding native 1.5.2 texture. Animated appearances reuse the patched Oculus bridge and no-reflection shader.

## v0.2 fixes

- Restores actual Cobblemon `flag` feature definitions for every included `mythical_*` appearance.
- `/pokespawn <species> <mythical_variant>` is parsed normally again.
- Mythical variants are hidden from the initial completion list until a species is typed.
- After a recognized species is typed, Mythical autocomplete is filtered to that species' accepted default-form static skins plus the seven supported animated effects.
- Static assignments now match the same default-form compatibility list used by the grid and autocomplete.
- The grid now creates Pokémon from normal `PokemonProperties` text instead of forcing an unregistered aspect onto an already-created entity.

Examples:

```text
/pokespawn pikachu 
```

offers `mythical_gilded`, `mythical_radioactive`, and the seven animated effects, but not Squirtle-only skins.

```text
/pokespawn squirtle 
```

offers `mythical_blazing`, `mythical_frosted`, `mythical_gilded`, `mythical_shadow`, and the seven animated effects.

## Included texture source

The full source package includes every accepted Mythical texture and generated resolver under `src/main/resources/`:

```text
src/main/resources/assets/mythicalcobbled/textures/pokemon/
```

Native textures remain under `assets/cobblemon/textures/pokemon/`. The namespaces are intentionally separate. The project contains 3,055 accepted Mythical texture/layer PNG files.

## Current strict baseline

- 633 supported native species
- 2,144 accepted static texture variation records
- 44 registered Mythical appearance features
- 7 animated shader families available on every supported native model
- 6,073 ordered grid appearances across 95 pages
- Wild appearance assignment disabled by default during testing

## Build inputs

Place the private input files listed in `inputs/README.txt` into `inputs/`, then run `build-java17.ps1` on Windows or `./build-java17.sh` on Linux/macOS.

## Grid test

Install `generated/kubejs/server_scripts/sunlitSkinGrid.js` into the same path in the instance/server and use:

```text
/sunlitskingrid spawn 1
/sunlitskingrid next
/sunlitskingrid previous
/sunlitskingrid respawn
/sunlitskingrid clear
/sunlitskingrid info
```

The grid follows National Dex order. For each species it displays Normal, every accepted default-form static skin, then the seven animated effects. Changing pages deletes the current page before spawning the replacement page.

## Separation from the paused project

The earlier MythicalBackport v1.8 project remains archived and unchanged. Do not mix its Cobblemon JAR or visual resources into this project.
