package com.samhill.wildyslayerpicker.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Time-based thresholds for observation decay and scoring.
 */
public final class TimeHeuristics
{
	private static final int OFF_PEAK_PENALTY = 5;

	private TimeHeuristics() {}

	/** Small penalty when in "peak" hours (UTC) so off-peak worlds rank slightly better when prefer-off-peak is on. */
	public static int offPeakPenalty(Instant now, int location)
	{
		int hourUtc = now.atZone(ZoneOffset.UTC).getHour();
		// Peak-ish: 17:00–23:00 and 12:00–14:00 UTC
		if ((hourUtc >= 17 && hourUtc < 23) || (hourUtc >= 12 && hourUtc < 14))
		{
			return OFF_PEAK_PENALTY;
		}
		return 0;
	}

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
