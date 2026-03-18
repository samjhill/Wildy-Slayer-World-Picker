package com.samhill.wildyslayerpicker;

/**
 * Scoring constants for risk calculation (not user config).
 */
public final class RiskConfig
{
	private RiskConfig() {}

	// Population penalty brackets (lower = safer)
	public static final int POP_PENALTY_0_300 = 5;
	public static final int POP_PENALTY_301_500 = 12;
	public static final int POP_PENALTY_501_800 = 22;
	public static final int POP_PENALTY_801_1200 = 35;
	public static final int POP_PENALTY_1201_PLUS = 50;

	// PKER observation (minutes -> penalty)
	public static final int PKERS_UNDER_15_MIN = 80;
	public static final int PKERS_UNDER_60_MIN = 50;
	public static final int PKERS_UNDER_180_MIN = 20;

	// PLAYERS observation (minutes -> penalty)
	public static final int PLAYERS_UNDER_15_MIN = 40;
	public static final int PLAYERS_UNDER_60_MIN = 25;
	public static final int PLAYERS_UNDER_180_MIN = 10;

	// EMPTY observation (minutes -> bonus, applied as negative penalty)
	public static final int EMPTY_UNDER_10_MIN_BONUS = 35;
	public static final int EMPTY_UNDER_30_MIN_BONUS = 20;
	public static final int EMPTY_UNDER_60_MIN_BONUS = 10;

	// Activity penalty range
	public static final int ACTIVITY_PENALTY_SMALL = 3;
	public static final int ACTIVITY_PENALTY_LARGE = 20;

	// Blacklist
	public static final int BLACKLIST_PENALTY = 9999;

	// Observation decay (minutes) - observations older than this contribute 0
	public static final int OBSERVATION_DECAY_MINUTES = 180;
}
