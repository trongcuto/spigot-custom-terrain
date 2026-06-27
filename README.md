# spigot-custom-terrain

A Spigot plugin that generates Minecraft terrain procedurally using the **Custom
ChunkGenerator API** combined with **Simplex noise**, in the spirit of the
Minecraft 1.18+ multi-noise system.

The terrain character varies by region rather than being uniform everywhere:
**flat lowland plains, rolling hills and tall steep mountains**, with **rivers
and lakes** carved into the lowlands. Local 3D noise wobbles the surface into
**overhangs and cliffs** (stronger on mountains) without breaking into floating
islands. Biomes are derived from the same shared climate fields so the land and
its vegetation always agree.

- **Target:** Spigot **1.21.x** (`1.21.8-R0.1-SNAPSHOT`), **Java 21**
- **Output jar:** `target/CustomTerrainGenerator-1.0.0.jar`

## How it works

A shared `TerrainShape` evaluates several low-frequency noise fields per column:

```
continentalness -> broad land elevation (lowlands vs highlands)
relief          -> how mountainous the region is (plains stay flat)
mountain ridged -> sharp peaks where relief is high
river           -> carves channels toward the water table in lowlands
temperature /   -> climate, shared with the biome provider; colder with altitude
humidity
```

The column's target surface height is:

```
height = SEA_LEVEL + BASE_LAND
       + continentalness * CONTINENT_AMPLITUDE
       + reliefShaped * mountain * MOUNTAIN_AMPLITUDE
```

A voxel is solid when `(height - y) + detail3D * overhangAmp > 0`. Because the
`(height - y)` term dominates away from the surface, terrain never floats; the
3D detail only matters near the surface, producing overhangs and cliffs.

Vanilla biome **decoration and caves are enabled**, so each biome gets its
authentic vegetation, trees, ores and cave systems from the shared climate.

| Feature   | Class                  | Notes |
|-----------|------------------------|-------|
| Noise     | `noise.SimplexNoise`   | Gustavson's 3D Simplex with seeded permutation, `octaves()` + `ridged()` FBM |
| Shape     | `generator.TerrainShape` | Continentalness/relief/river/climate control fields |
| Terrain   | `generator.CustomChunkGenerator` | Per-column height + 3D overhang detail, biome-aware surface |
| Biomes    | `generator.BiomeManager` | `BiomeProvider` from the shared `TerrainShape` (rivers, swamps, snowy peaks...) |
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

Tunable parameters: `continent-scale`, `relief-scale`, `river-scale`,
`detail-scale`, `biome-scale`, `base-land`, `continent-amplitude`,
`mountain-amplitude`, `detail-amplitude-base`, `detail-amplitude-mountain`,
`river-width`, `sea-level`, `dirt-depth`.

Config changes apply to worlds generated afterwards; already-generated chunks
keep the settings they were built with.

To make a world's *default* generator this plugin (instead of `/terrain create`),
add to `bukkit.yml`:

```yaml
worlds:
  world:
    generator: CustomTerrainGenerator
```
