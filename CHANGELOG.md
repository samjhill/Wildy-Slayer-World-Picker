# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-03-18

### Added

- Initial release: Wildy Slayer World Picker RuneLite plugin.
- Live world list from RuneLite WorldService with automatic exclusions (PvP, high-risk, seasonal, tournament, non-members, etc.).
- Optional exclusions: skill-total worlds, high population cap, and per-world blacklist.
- Risk scoring from population, activity, and user-reported observations (Empty / Players / PKers).
- Sidebar panel: best world, top-N backups, risk score, confidence, and reasons.
- Quick reporting buttons (Empty, Players, PKers) with persistence across sessions.
- Configurable observation decay (minutes); observations older than this are ignored and can be cleared.
- Prefer off-peak: optional time-based penalty during peak UTC hours.
- Blacklist: persist blacklisted worlds in config; Blacklist per row and Clear blacklist in the panel.
- Settings hint and “No other eligible worlds” when the backup list is empty.
- Unit tests for TimeHeuristics, WorldTypeUtil, and RiskScorer (null/empty input).

### Technical

- Java 11, Gradle 7.x, RuneLite client `latest.release`.
- Icon at repo root (48×72 px) for Plugin Hub; Gradle wrapper and GitHub Actions build workflow.
