# spigot-custom-terrain

A Spigot plugin that generates Minecraft terrain procedurally using the **Custom
ChunkGenerator API** combined with a **true 3D Simplex-noise density field**.

Unlike a 2D heightmap (one surface height per column), the generator evaluates a
3D noise function at every block position, so the terrain naturally produces
**overhangs, arches, caves and floating crags**.

- **Target:** Spigot **1.21.x** (`1.21.8-R0.1-SNAPSHOT`), **Java 21**
- **Output jar:** `target/CustomTerrainGenerator-1.0.0.jar`

## How it works

For each block the generator computes:

```
density = fbm(x, y, z)                       # fractal Brownian motion (4 octaves)
        + (ridged(x, y, z) - 0.5) * weight   # ridged multifractal for sharp ridges
        - (y - terrainCenter) * squash       # vertical gradient: ground vs. sky
```

`density > 0` is solid, otherwise air (or water below sea level). The vertical
"squash" gradient is what turns isotropic 3D noise into a recognisable
ground/sky split while still allowing overhangs.

Layered on the same pipeline:

| Feature   | Class                  | Notes |
|-----------|------------------------|-------|
| Noise     | `noise.SimplexNoise`   | Gustavson's 3D Simplex with seeded permutation, `octaves()` + `ridged()` FBM |
| Terrain   | `generator.CustomChunkGenerator` | 3D density field, material selection by depth/altitude |
| Ores      | `generator.OreGenerator` | Vein-forming 3D noise, altitude bands tuned to -64..320 |
| Biomes    | `generator.BiomeManager` | `BiomeProvider` from temperature/humidity noise + altitude |
| Trees     | `generator.StructureGenerator` | `BlockPopulator` placing trees on the real 3D surface |
| Config    | `config.TerrainConfig` | Runtime-tunable parameters |

## Build

```bash
mvn clean package
```

The shaded plugin jar is produced at `target/CustomTerrainGenerator-1.0.0.jar`.
Copy it into your server's `plugins/` folder.

## Commands

`/terrain` (aliases `/tg`, `/terrain-gen`, permission `terrain.admin`):

| Command | Description |
|---------|-------------|
| `/terrain create <name>` | Create a world that uses the 3D generator and teleport to it |
| `/terrain config <param> <value>` | Tune a generation parameter at runtime |
| `/terrain list` | Show current parameter values |
| `/terrain reset` | Reset parameters to defaults |

Tunable parameters: `horizontal-scale`, `vertical-scale`, `squash`,
`terrain-center`, `ridge-weight`, `sea-level`, `dirt-depth`, `ore-scale`,
`biome-scale`, `tree-threshold`.

Config changes apply to worlds generated afterwards; already-generated chunks
keep the settings they were built with.

To make a world's *default* generator this plugin (instead of `/terrain create`),
add to `bukkit.yml`:

```yaml
worlds:
  world:
    generator: CustomTerrainGenerator
```
