package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.Confidence;
import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.WorldCandidate;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import com.samhill.wildyslayerpicker.util.TimeHeuristics;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Builds human-readable reason strings for recommendations.
 */
@Singleton
public class ExplanationBuilder
{
	public List<String> reasonsFor(WorldCandidate candidate, List<WorldObservation> observations,
		WildySlayerWorldPickerConfig config, Instant now, int riskScore, Confidence confidence)
	{
		List<String> reasons = new ArrayList<>();
		if (candidate.getPlayerCount() <= 300)
		{
			reasons.add("Low population");
		}
		else if (candidate.getPlayerCount() <= 500)
		{
			reasons.add("Moderate population");
		}
		else
		{
			reasons.add("Higher population");
		}

		Optional<WorldObservation> latestEmpty = observations.stream()
			.filter(o -> o.getWorldId() == candidate.getWorldId() && o.getType() == ObservationType.EMPTY)
			.max((a, b) -> (a.getObservedAt() != null && b.getObservedAt() != null)
				? a.getObservedAt().compareTo(b.getObservedAt()) : 0);
		latestEmpty.ifPresent(o -> {
			long min = TimeHeuristics.minutesSince(o.getObservedAt(), now);
			if (min >= 0 && min <= 60) reasons.add("Recent empty cave report " + min + "m ago");
		});

		Optional<WorldObservation> latestPkers = observations.stream()
			.filter(o -> o.getWorldId() == candidate.getWorldId() && o.getType() == ObservationType.PKERS)
			.max((a, b) -> (a.getObservedAt() != null && b.getObservedAt() != null)
				? a.getObservedAt().compareTo(b.getObservedAt()) : 0);
		latestPkers.ifPresent(o -> {
			long min = TimeHeuristics.minutesSince(o.getObservedAt(), now);
			if (min >= 0 && min <= 180) reasons.add("Recent PKer report " + min + "m ago");
		});

		Optional<WorldObservation> latestPlayers = observations.stream()
			.filter(o -> o.getWorldId() == candidate.getWorldId() && o.getType() == ObservationType.PLAYERS)
			.max((a, b) -> (a.getObservedAt() != null && b.getObservedAt() != null)
				? a.getObservedAt().compareTo(b.getObservedAt()) : 0);
		latestPlayers.ifPresent(o -> {
			long min = TimeHeuristics.minutesSince(o.getObservedAt(), now);
			if (min >= 0 && min <= 180) reasons.add("Recent players seen " + min + "m ago");
		});

		if (config.preferOffPeak() && config.debugMode())
		{
			reasons.add("Off-peak region penalty applied");
		}

		if (reasons.isEmpty())
		{
			reasons.add("Based on population and world type only");
		}
		return reasons;
	}
}
