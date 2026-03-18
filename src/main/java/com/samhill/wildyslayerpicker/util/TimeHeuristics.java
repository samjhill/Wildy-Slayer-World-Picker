package com.samhill.wildyslayerpicker.util;

import java.time.Duration;
import java.time.Instant;

/**
 * Time-based thresholds for observation decay and scoring.
 */
public final class TimeHeuristics
{
	private TimeHeuristics() {}

	public static long minutesBetween(Instant from, Instant to)
	{
		return Duration.between(from, to).toMinutes();
	}

	/** Minutes since observed (positive = in the past). */
	public static long minutesSince(Instant observedAt, Instant now)
	{
		return minutesBetween(observedAt, now);
	}
}
