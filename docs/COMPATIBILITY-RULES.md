# Strict compatibility rules

A static variation is accepted only when:

1. Its species has an implemented native Cobblemon 1.5.2 model and texture.
2. The variation supplies a texture but no custom `model` or `poser`.
3. The matching native base/form/shiny texture can be resolved.
4. The texture dimensions are identical.
5. The texture alpha-layout intersection-over-union is at least 0.97.
6. Every copied layer texture exists.

This is intentionally conservative. A rejected texture is not necessarily broken; it is simply not proven safe enough for the baseline. Additional skins can be reviewed and added later.

Animated families do not replace the model or texture UV layout. They use the existing draw-time Oculus uniform bridge and the no-reflection shader:

- Radiant
- Magma
- Glitch
- Galaxy
- Matrix
- Firework
- Holographic
