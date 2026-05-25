# Solid Fuel Thruster

The **Solid Fuel Thruster** is a compact 1×1 thruster that burns **items** instead of fluids. It is tuned weaker than liquid thrusters by default (lower base thrust and smaller, slower exhaust particles).

## Block behavior

### Fuel input

- Accepts items from the **back** of the block (the `facing` side — same side as the funnel on the model). Right-click with a valid fuel to insert; right-click empty-handed to take queued fuel back.
- Compatible with Create **funnels**, **hoppers**, chests, and other item handlers on that face.
- Only **data-driven fuels** are accepted (see [Solid thruster fuels](#solid-thruster-fuels) below). There is no implicit `minecraft:coal` or furnace-fuel-tag fallback unless you add it in a datapack.

### Burn pipeline

The thruster keeps up to **two** items internally:

| Slot | Role |
|------|------|
| Burning | The item currently being consumed |
| Queued | The next item waiting to burn |

### Redstone and thrust

- Throttle follows redstone strength **1–15** (0–100% power), like other thrusters.
- Thrust scales with: base thrust (config) × throttle × obstruction × fuel `thrust_multiplier` × fuel efficiency (config, if added later).
- Exhaust **particle count and velocity** scale with throttle and are further reduced by config multipliers (weaker plume than liquid thrusters).

### Crafting

```
C I C
A B A
I P I
```

- `C` — copper sheet (top left & right)  
- `I` — iron sheet (top center, bottom left & right)  
- `A` — andesite alloy (middle left & right)  
- `B` — chute (center)  
- `P` — fluid pipe (bottom center; exhaust / output)  

## Config

Section: **`solidFuelThruster`** in `createpropulsion-common.toml`

| Key | Default | Description |
|-----|---------|-------------|
| `solidFuelThrusterBaseThrust` | `200` | Base thrust at full power and full obstruction clearance |
| `solidFuelThrusterNozzleOffset` | `0.75` | Force application offset from block center |
| `solidFuelThrusterParticleCountMultiplier` | `0.35` | Scales exhaust particle count |
| `solidFuelThrusterParticleVelocityMultiplier` | `0.4` | Scales exhaust particle speed |

Shared physics/display settings (`thrustUnitsPerKn`, obstruction scan, atmospheric pressure, etc.) use the global **thruster** / **physics** sections like other thrusters.

---

## Solid thruster fuels

Fuel definitions live under:

`data/<namespace>/solid_thruster_fuels/<path>.json`

Only the **Solid Fuel Thruster** uses these files. Liquid thrusters still use [`thruster_fuels`](Datapack-Example-Usage.md).

### Resolution rules (strict data-driven)

- Only declared entries are valid fuels.
- No automatic furnace-fuel or coal-only fallback.
- Precedence:
  1. Removed fuel ids (KubeJS `removeFuel`)
  2. KubeJS scripted / override entries
  3. Datapack entries (items and tags)

Tag entries apply to every item in that tag that is not already defined by a specific `item` entry.

### JSON schema

Each file must define **exactly one** of `item` or `item_tag`.

```json
{
  "item": "minecraft:coal",
  "thrust_multiplier": 1.0,
  "consumption_multiplier": 1.0,
  "burn_ticks": 1600,
  "particle": "plume",
  "override_textures": [
    "createpropulsion:plume_0",
    "createpropulsion:plume_1"
  ],
  "override_color": 6750207,
  "use_item_color": false,
  "required_mod": "optional_mod_id"
}
```

Tag example:

```json
{
  "item_tag": "minecraft:planks",
  "thrust_multiplier": 0.3,
  "consumption_multiplier": 1.0,
  "particle": "plume"
}
```

### Fields

| Field | Required | Description |
|-------|----------|-------------|
| `item` | One of `item` / `item_tag` | Item id (e.g. `minecraft:coal`) |
| `item_tag` | One of `item` / `item_tag` | Item tag id (e.g. `minecraft:logs`) |
| `thrust_multiplier` | Yes | Thrust scaling for this fuel |
| `consumption_multiplier` | Yes | Burn speed scaling. Higher = **faster** burn (shorter duration). `1.0` = normal smelting burn time |
| `burn_ticks` | No | Fixed burn length in ticks. If omitted, uses the item’s smelting fuel time ÷ `consumption_multiplier` |
| `particle` | No | `plume`, `plasma`, or `none` (default: `plume`) |
| `override_textures` | No | Particle atlas texture ids |
| `override_color` | No | RGB color as decimal int (`6750207` = `0x66CCFF`) |
| `use_item_color` | No | Reserved for per-item tinting (prefer `override_color` in datapacks) |
| `required_mod` | No | Entry loads only if that mod id is loaded |

### Burn time

- Default: `item.getBurnTime(SMELTING) / consumption_multiplier` (minimum 1 tick), unless `burn_ticks` is set.
- **Coal / charcoal** use explicit longer burns (`coal` 4000 ticks, `charcoal` 3600 ticks). Blocks stay at `burn_ticks: 16000`.
- **Logs and planks** use normal smelting burn time at `consumption_multiplier: 1.0`.
- **Other wood** (stairs, slabs, fences, trapdoors, doors) use `consumption_multiplier: 8.0` so they burn much faster than logs/planks.

### Particle names

- `plume` (default) — standard exhaust
- `plasma` — plasma-style exhaust
- `none` — no particles for this fuel

### Default pack (`createpropulsion`)

Shipped under `data/createpropulsion/solid_thruster_fuels/minecraft/`:

**Items**

- `coal`, `coal_block`, `charcoal`, `charcoal_block`

**Tags** (wood and variants)

- `minecraft:logs`
- `minecraft:planks`
- `minecraft:wooden_stairs`
- `minecraft:wooden_slabs`
- `minecraft:wooden_fences`
- `minecraft:wooden_trapdoors`
- `minecraft:wooden_doors`

Wood fuels use lower `thrust_multiplier` values (~0.25–0.35) than coal. Logs/planks last a normal smelting-length burn; other wooden parts burn ~8× faster via `consumption_multiplier`.

### Example: custom datapack fuel

Path: `data/my_pack/solid_thruster_fuels/blaze_rod.json`

```json
{
  "item": "minecraft:blaze_rod",
  "thrust_multiplier": 1.2,
  "consumption_multiplier": 0.8,
  "particle": "plasma",
  "override_color": 16744448,
  "required_mod": "minecraft"
}
```

### Example: mod wood via tag

Path: `data/my_pack/solid_thruster_fuels/c_wood.json`

```json
{
  "item_tag": "c:wooden_logs",
  "thrust_multiplier": 0.4,
  "consumption_multiplier": 1.0
}
```

### Reload

- Run `/reload` to re-read datapack files.
- Fuel data is synced to clients automatically (same as liquid thruster fuels).

---

## KubeJS

Global: **`SolidThrusterFuelManager`**

Same method names as [`ThrusterFuelManager`](KubeJS-API.md), but ids are **items**, not fluids.

Example (`kubejs/server_scripts/solid_thruster_fuels.js`):

```js
ServerEvents.loaded(event => {
  SolidThrusterFuelManager.registerScriptedFuel('minecraft:blaze_rod', {
    thrust_multiplier: 1.2,
    consumption_multiplier: 0.8,
    particle: 'plasma',
    override_color: 0xFF8000
  })

  SolidThrusterFuelManager.registerScriptedFuel('minecraft:bamboo', {
    thrust_multiplier: 0.2,
    consumption_multiplier: 1.5,
    particle: 'plume'
  })

  // SolidThrusterFuelManager.removeFuel('minecraft:coal')
})
```

See [KubeJS API — Solid Fuel Thruster](KubeJS-API.md#solid-fuel-thruster-solidthrusterfuelmanager) for the full method list.

---

## ComputerCraft

When **CC: Tweaked** is installed, the block exposes peripheral type **`solid_fuel_thruster`**.

- Throttle: `setPower` / `setPowerNormalized` / `getPower` (same [peripheral control rules](cc/README.md) as other thrusters)
- Thrust: `getCurrentThrustPN`, `getCurrentThrustKN`, `getDisplayedThrustPN`, `getDisplayedThrustKN`, `getAirflowMs`, `getObstruction`
- Fuel: `getFuelAmount`, `getFuelCapacity`, `getBurnTimeRemaining`, `isBurning`
- Items: `list`, `pushItems`, `pullItems` on the back-side fuel slot

Full method list: [ComputerCraft peripherals — Solid fuel thruster](ComputerCraft-Peripherals.md#solid-fuel-thruster-solid_fuel_thruster).

---

## Related docs

- [Liquid thruster fuels (datapack)](Datapack-Example-Usage.md)
- [KubeJS API](KubeJS-API.md)
- [ComputerCraft peripherals](ComputerCraft-Peripherals.md#solid-fuel-thruster-solid_fuel_thruster)
