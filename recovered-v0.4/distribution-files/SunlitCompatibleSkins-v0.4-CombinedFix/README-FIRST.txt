SUNLIT COMPATIBLE SKINS — ALPHA v0.4 COMBINED FIX
Forge 1.20.1 / Cobblemon 1.5.2 / Java 17
=================================================

THIS BUILD COMPLETES
--------------------
1. Jade labels
   With Jade 11.13.2 installed, looking at a non-default Pokemon shows only the
   clean variant name, such as Glitched, Blazing, or Gilded. Default Pokemon
   receive no extra line. JadeAddons can remain installed. JEED is unrelated.

2. Static skin repair
   Gyarados Pixie, every accepted Moltres static variant, and every accepted
   Mewtwo static variant now use their exact original textures with the matching
   legacy-UV model. The failed repainted textures from v0.3.1 are not used.

3. Spawn systems retained/restored
   Wild skin assignment remains configurable. The configured non-default share
   is split evenly among valid enabled appearances for that species.
   Legendary Pokemon can spawn on a persistent player-online timer.

4. Earlier fixes retained
   Species-specific /pokespawn autocomplete, the 5x5 origin-fixed test grid,
   and the Wailord/Avalugg 512-pixel animated-effect shader fix remain included.

INSTALLATION
------------
1. Stop the dedicated server completely and close every Minecraft client.
2. Remove the previous SunlitCompatibleSkins Cobblemon replacement JAR.
   Never keep two main Cobblemon JARs together.
3. Put the JAR from server-and-client-mods into the mods folder on BOTH the
   server and every client.
4. On clients, keep/replace the patched Oculus JAR from client-mods and select
   the shader pack supplied in shaderpacks. These are unchanged in purpose from
   the Wailord/Avalugg fix but are included so the files stay matched.
5. Keep Jade-1.20.1-Forge-11.13.2.jar installed. JadeAddons 5.5.0 is fine.
6. Put kubejs/server_scripts/sunlitSkinGrid.js in the same server script folder,
   replacing the older grid script.
7. Review config/sunlit-compatible-skins.toml. If preserving your existing
   config, merge the new [legendary_spawning] settings rather than overwriting
   your custom values. An older config with appearance_spawning.enabled=false
   will continue to keep automatic wild skins disabled until changed to true.
8. Fully restart server and clients.

SKIN TESTS
----------
/pokespawn gyarados mythical_pixie
/pokespawn moltres mythical_blazing
/pokespawn moltres mythical_gilded
/pokespawn mewtwo mythical_error
/pokespawn mewtwo mythical_metallic
/pokespawn wailord mythical_galaxy

Look at each non-default Pokemon with Jade. The tooltip should contain only the
variant name. A normal Pokemon should have no variant line.

LEGENDARY TIMER
---------------
The timer advances only while at least one player is online and is saved to:
  config/sunlit-compatible-skins-state.properties

If config/mythicalbackport-state.properties exists and the new state file does
not, its accumulated timer is imported automatically.

The native Cobblemon 1.5.2 scheduler list in this build is:
Articuno, Iron Leaves, Mew, Mewtwo, Moltres, Rayquaza, Walking Wake, Xerneas,
and Zapdos. Relative weights can be changed in [legendary_species_weights].

For a temporary test, set minimum_online_minutes=1, maximum_online_minutes=1,
check_interval_minutes=1, and both chance percentages to 100. Restore normal
values afterward.

GRID
----
/sunlitskingrid spawn 1
/sunlitskingrid next
/sunlitskingrid previous
/sunlitskingrid clear

The grid is 5x5 and remains anchored at X=0, Y=-60, Z=0 in a default flatworld.

VALIDATION LIMIT
----------------
Archive, bytecode, resolver, texture, model-bone, shader, and configuration
checks passed. The repaired skins and Jade tooltip still require the final
in-game visual check in your exact modpack.
