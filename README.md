# Wildy Slayer World Picker

A RuneLite Plugin Hub plugin that recommends an OSRS world with the **lowest estimated revenant cave activity** for Wilderness Slayer. This is a **heuristic recommendation only**—it does not guarantee safety.

## Features

- **Live world list** from RuneLite’s `WorldService` (no manual entry)
- **Automatic exclusions**: PvP, high-risk, seasonal, tournament, fresh start, deadman, non-members
- **Optional exclusions**: skill-total worlds, high population threshold, blacklist
- **Risk scoring**: population, activity, and your own observations (empty / players / PKers)
- **Sidebar panel**: best world, top 5 backups, risk score, confidence, reasons
- **Quick reporting**: Mark Empty, Saw Players, Saw PKers (persisted across sessions)
- **Copy world number** and **manual refresh**
- No automatic world hopping or menu injection

## Install (Plugin Hub)

1. Install [RuneLite](https://runelite.net/) and open the **Plugin Hub**.
2. Search for **Wildy Slayer World Picker** and install.

## Development

### Requirements

- **Java 11** (e.g. Eclipse Temurin)
- **Gradle** (wrapper included)

### Build

**Requires Java 11** (e.g. [Eclipse Temurin](https://adoptium.net/) or OpenJDK 11).

```bash
./gradlew build
```

(The Gradle wrapper JAR is included; no need to run `gradle wrapper` unless it was removed.)

### Test locally (run RuneLite with the plugin)

```bash
./gradlew run
```

This launches RuneLite with `--developer-mode` and `--debug` and **loads this plugin as a built-in**, so you can test it immediately. Open the sidebar (e.g. the puzzle-piece icon) and click **Wildy Slayer World Picker** to open the panel.

### Install as external plugin (testing)

1. Build the shadow JAR:
   ```bash
   ./gradlew shadowJar
   ```
2. Copy `build/libs/wildy-slayer-world-1.0-SNAPSHOT-all.jar` into RuneLite’s `plugins` folder (or use RuneLite’s “Install plugin” from the plugin hub developer flow).

### Config

- **Exclude skill total worlds** – hide total-level worlds
- **Exclude high population worlds** – cap by max population
- **Prefer off-peak** – optional region/time penalty (disabled by default)
- **Include activity penalty** – small penalty for busy-activity worlds (e.g. trade, Wintertodt)
- **Top list size** – number of backup worlds shown (default 5)
- **Debug mode** – extra logs and score breakdown

## Data and persistence

- **Observations** are stored in RuneLite’s config (group `wildyslayerworldpicker`, key `observations`) as JSON.
- Stale observations (older than 180 minutes) can be pruned with **Clear stale reports** or on load.

## License

Use and modify as you like. No warranty; use at your own risk in the Wilderness.
