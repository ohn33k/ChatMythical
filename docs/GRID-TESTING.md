# Grid testing

The generated grid contains 64 appearances per page in an 8 × 8 layout.

Ordering is:

1. Species by National Dex number.
2. Normal appearance.
3. Accepted default-form static texture families.
4. Animated shader families.

`next` and `previous` clear all Pokémon and labels tagged by the script before creating the replacement page.

v0.2 parses each entry using the same registered property syntax used by `/pokespawn`, for example:

```text
squirtle level=50 mythical_gilded
```

The script verifies that the parsed Pokémon actually contains the requested aspect before adding the entity. A failed property parse or rejected entity creates a red `SPAWN ERROR` label and writes the exact species/variant to the KubeJS log.
