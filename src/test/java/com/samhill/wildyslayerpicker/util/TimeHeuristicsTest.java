package com.samhill.wildyslayerpicker.util;

import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TimeHeuristicsTest
{
	@Test
	public void minutesBetween()
	{
		Instant a = Instant.parse("2024-01-01T12:00:00Z");
		Instant b = Instant.parse("2024-01-01T13:30:00Z");
		assertEquals(90, TimeHeuristics.minutesBetween(a, b));
		assertEquals(-90, TimeHeuristics.minutesBetween(b, a));
	}

	@Test
	public void minutesSince()
	{
		Instant past = Instant.parse("2024-01-01T10:00:00Z");
		Instant now = Instant.parse("2024-01-01T11:15:00Z");
		assertEquals(75, TimeHeuristics.minutesSince(past, now));
	}

	@Test
	public void offPeakPenalty_peakHours_returnPenalty()
	{
		// 18:00 UTC = peak
		Instant peak = Instant.parse("2024-06-01T18:00:00Z");
		assertEquals(5, TimeHeuristics.offPeakPenalty(peak, 0));
		// 12:30 UTC = lunch peak
		Instant lunch = Instant.parse("2024-06-01T12:30:00Z");
		assertEquals(5, TimeHeuristics.offPeakPenalty(lunch, 0));
	}

	@Test
	public void offPeakPenalty_offPeak_returnZero()
	{
		Instant early = Instant.parse("2024-06-01T06:00:00Z");
		assertEquals(0, TimeHeuristics.offPeakPenalty(early, 0));
		Instant late = Instant.parse("2024-06-01T23:30:00Z");
		assertEquals(0, TimeHeuristics.offPeakPenalty(late, 0));
	}
}
