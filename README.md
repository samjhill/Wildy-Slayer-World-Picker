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

## Use with your installed RuneLite client

Until the plugin is on the Plugin Hub, you can use it with your normal RuneLite in two ways.

### Option A: Run RuneLite from this project (recommended)

Running the plugin from the project **starts the same RuneLite client** with Wildy Slayer World Picker loaded:

```bash
cd /path/to/wildy-slayer-world
gradle run
```

(Or `./gradlew run` if the Gradle wrapper is set up.)

To make this easy, you can add an alias or a desktop shortcut that runs `gradle run` from the project directory. Then you’re effectively “using your real RuneLite” with the plugin.

### Option B: Install the JAR into your existing RuneLite

1. **Build the plugin JAR:**
   ```bash
   cd /path/to/wildy-slayer-world
   gradle shadowJar
   ```
   This creates `build/libs/wildy-slayer-world-1.0.0-all.jar`.

2. **Install it in RuneLite:**
   - **macOS:** Copy the JAR into `~/Library/Application Support/RuneLite/plugins` (create the `plugins` folder if it doesn’t exist).
   - **Windows:** Copy the JAR into `%USERPROFILE%\.runelite\plugins`.
   - **Linux:** Copy the JAR into `~/.runelite/plugins`.

3. Start RuneLite **with developer mode** (so it can load external plugins):
   - **macOS:** From Terminal: `open -a RuneLite --args --developer-mode`
   - Or in the RuneLite launcher, add `--developer-mode` to the “Client parameters” / “JVM options” if supported.

4. In RuneLite, open **Configuration** → **Plugin Hub** and check for an “External” or “Development” section, or see if the plugin appears in the list. If your RuneLite build doesn’t load plugins from the `plugins` folder, use **Option A** instead or publish the plugin to the Plugin Hub.

## Publish to the Plugin Hub

To list Wildy Slayer World Picker on the official [RuneLite Plugin Hub](https://runelite.net/plugin-hub) so anyone can install it from the client:

1. **Fork the plugin-hub repo**  
   Go to [github.com/runelite/plugin-hub](https://github.com/runelite/plugin-hub) and click **Fork**.

2. **Clone your fork and create a branch**
   ```bash
   git clone https://github.com/YOUR_USERNAME/plugin-hub.git
   cd plugin-hub
   git checkout -b wildy-slayer-world upstream/master
   ```
   If you don’t have `upstream` yet: `git remote add upstream https://github.com/runelite/plugin-hub.git`, then `git fetch upstream`.

3. **Add a plugin manifest**  
   Create a new file in the `plugins` directory. The filename is the plugin’s internal name (e.g. `wildy-slayer-world`). No file extension.

   **Path:** `plugins/wildy-slayer-world`

   **Contents:**
   ```text
   repository=https://github.com/samjhill/Wildy-Slayer-World-Picker.git
   commit=REPLACE_WITH_YOUR_LATEST_COMMIT_HASH
   ```
   - Use the **HTTPS** “Clone or download” URL for your plugin repo (as above).
   - For `commit=`, use the **full 40-character Git commit hash** of the commit you want the Hub to build (e.g. from GitHub: your repo → **Commits** → click the latest commit → copy the hash).

4. **Commit and push**
   ```bash
   git add plugins/wildy-slayer-world
   git commit -m "Add Wildy Slayer World Picker plugin"
   git push -u origin wildy-slayer-world
   ```

5. **Open a pull request**  
   On GitHub, go to [runelite/plugin-hub](https://github.com/runelite/plugin-hub) → **Pull requests** → **New pull request**. Choose “compare across forks”, set base to `runelite/plugin-hub` `master` and head to your fork’s `wildy-slayer-world` branch. Add a short description of what the plugin does and create the PR.

6. **Fix CI if needed**  
   The PR will run a build. If it fails, open the **Details** of the failed check, fix the reported issues in **this** repo (your plugin), push a new commit, then update the `commit=` hash in your plugin-hub PR to that new commit and push again.

7. **Wait for review**  
   A RuneLite maintainer will review the plugin (no malicious code, compliance with [Jagex’s third-party client guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1), etc.). Once the PR is merged, the plugin will appear on the Plugin Hub.

**Before submitting:** The [plugin-hub README](https://github.com/runelite/plugin-hub/blob/master/README.md) recommends a **BSD 2-Clause** LICENSE in your plugin repo and an **icon** (e.g. `icon.png` at repo root, no larger than 48×72 px). Your `runelite-plugin.properties` and plugin code should already match what the Hub expects.

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
2. Copy `build/libs/wildy-slayer-world-1.0.0-all.jar` into RuneLite’s `plugins` folder (or use RuneLite’s “Install plugin” from the plugin hub developer flow).

### Config

Configure in RuneLite: sidebar → wrench icon → **Wildy Slayer World Picker**.

| Option | Description |
|--------|-------------|
| **Exclude skill total worlds** | Hide total-level worlds from recommendations. |
| **Exclude high population worlds** | Exclude worlds above the max population cap. |
| **Max population** | Population ceiling when “Exclude high population worlds” is on (default 2000). |
| **Prefer off-peak** | Add a small time-based penalty during peak UTC hours so off-peak worlds rank slightly better. |
| **Include activity penalty** | Small penalty for busy-activity worlds (e.g. trade, Wintertodt, Barbarian Assault). |
| **Top list size** | Number of backup worlds shown (default 5). |
| **Observation decay (minutes)** | Observations older than this are ignored for scoring and can be cleared (default 180). |
| **Debug mode** | Extra logs and score breakdown in the panel. |

## Data and persistence

- **Observations** are stored in RuneLite’s config (group `wildyslayerworldpicker`, key `observations`) as JSON.
- Stale observations (older than the configured **Observation decay** in minutes) can be pruned with **Clear stale reports** or on load.
- **Blacklisted worlds** are stored in config (key `blacklistedWorlds`) and persist across sessions. Use **Blacklist** on a world row or **Clear blacklist** to manage.

## License

Use and modify as you like. No warranty; use at your own risk in the Wilderness.
