package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.Confidence;
import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.RecommendationResult;
import com.samhill.wildyslayerpicker.model.ScoredWorld;
import com.samhill.wildyslayerpicker.model.WorldCandidate;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import com.samhill.wildyslayerpicker.util.TimeHeuristics;
import com.samhill.wildyslayerpicker.util.WorldTypeUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;

/**
 * Pure scoring engine: exclusions, risk score, confidence, reasons.
 */
@Singleton
public class RiskScorer
{
	private static final Set<String> ACTIVITY_PENALTY_KEYWORDS = Set.of(
		"trade", "barbarian assault", "wintertodt", "forestry", "blast furnace"
	);

	@Inject
	private ExplanationBuilder explanationBuilder;

	public RecommendationResult score(
		Collection<World> worlds,
		List<WorldObservation> observations,
		WildySlayerWorldPickerConfig config,
		Instant now,
		Set<Integer> blacklistedWorlds)
	{
		if (worlds == null || worlds.isEmpty())
		{
			return RecommendationResult.builder()
				.generatedAt(now)
				.bestWorld(null)
				.backups(new ArrayList<>())
				.excluded(new ArrayList<>())
				.build();
		}

		List<ScoredWorld> excluded = new ArrayList<>();
		List<ScoredWorld> scored = new ArrayList<>();

		for (World world : worlds)
		{
			WorldCandidate candidate = toCandidate(world);
			boolean exclude;
			String exclusionReason = null;

			if (WorldTypeUtil.isAlwaysExcluded(world))
			{
				exclude = true;
				exclusionReason = "World type excluded (e.g. PvP, seasonal, non-members)";
			}
			else if (config.excludeSkillTotalWorlds() && WorldTypeUtil.isSkillTotalWorld(world))
			{
				exclude = true;
				exclusionReason = "Skill total world";
			}
			else if (config.excludeHighPopulationWorlds() && world.getPlayers() > config.maxPopulation())
			{
				exclude = true;
				exclusionReason = "Above max population";
			}
			else if (blacklistedWorlds != null && blacklistedWorlds.contains(world.getId()))
			{
				exclude = true;
				exclusionReason = "Blacklisted";
			}
			else
			{
				exclude = false;
			}

			int riskScore = exclude ? 0 : computeRiskScore(world, candidate, observations, config, now, blacklistedWorlds);
			Confidence confidence = confidenceFor(world, observations, now, exclude);
			List<String> reasons = explanationBuilder.reasonsFor(candidate, observations, config, now, riskScore, confidence);

			ScoredWorld sw = ScoredWorld.builder()
				.world(candidate)
				.riskScore(riskScore)
				.confidence(confidence)
				.reasons(reasons)
				.excluded(exclude)
				.exclusionReason(exclusionReason)
				.build();

			if (exclude)
			{
				excluded.add(sw);
			}
			else
			{
				scored.add(sw);
			}
		}

		scored.sort(Comparator.comparingInt(ScoredWorld::getRiskScore));

		int topN = Math.max(1, config.topListSize());
		ScoredWorld best = scored.isEmpty() ? null : scored.get(0);
		List<ScoredWorld> backups = scored.size() <= 1 ? new ArrayList<>() : scored.subList(1, Math.min(topN + 1, scored.size()));

		return RecommendationResult.builder()
			.generatedAt(now)
			.bestWorld(best)
			.backups(new ArrayList<>(backups))
			.excluded(excluded)
			.build();
	}

	private WorldCandidate toCandidate(World world)
	{
		return WorldCandidate.builder()
			.worldId(world.getId())
			.activity(world.getActivity() != null ? world.getActivity() : "")
			.location(world.getLocation())
			.playerCount(world.getPlayers())
			.types(world.getTypes() != null ? world.getTypes().clone() : java.util.EnumSet.noneOf(WorldType.class))
			.build();
	}

	private int computeRiskScore(World world, WorldCandidate candidate, List<WorldObservation> observations,
		WildySlayerWorldPickerConfig config, Instant now, Set<Integer> blacklistedWorlds)
	{
		int score = 0;
		score += populationPenalty(world.getPlayers());
		if (config.includeActivityPenalty())
		{
			score += activityPenalty(world.getActivity());
		}
		score += observationAdjustment(world.getId(), observations, config, now);
		if (config.preferOffPeak())
		{
			score += TimeHeuristics.offPeakPenalty(now, world.getLocation());
		}
		if (blacklistedWorlds != null && blacklistedWorlds.contains(world.getId()))
		{
			score += RiskConfig.BLACKLIST_PENALTY;
		}
		return Math.max(0, score);
	}

	private int populationPenalty(int playerCount)
	{
		if (playerCount <= 300) return RiskConfig.POP_PENALTY_0_300;
		if (playerCount <= 500) return RiskConfig.POP_PENALTY_301_500;
		if (playerCount <= 800) return RiskConfig.POP_PENALTY_501_800;
		if (playerCount <= 1200) return RiskConfig.POP_PENALTY_801_1200;
		return RiskConfig.POP_PENALTY_1201_PLUS;
	}

	private int activityPenalty(String activity)
	{
		if (activity == null || activity.isBlank()) return 0;
		String lower = activity.toLowerCase();
		for (String kw : ACTIVITY_PENALTY_KEYWORDS)
		{
			if (lower.contains(kw)) return RiskConfig.ACTIVITY_PENALTY_SMALL;
		}
		return 0;
	}

	private int observationAdjustment(int worldId, List<WorldObservation> observations, WildySlayerWorldPickerConfig config, Instant now)
	{
		int decayMin = config != null ? Math.max(1, config.observationDecayMinutes()) : RiskConfig.OBSERVATION_DECAY_MINUTES;
		int adj = 0;
		for (WorldObservation o : observations)
		{
			if (o.getWorldId() != worldId) continue;
			long minutes = TimeHeuristics.minutesSince(o.getObservedAt(), now);
			if (minutes < 0 || minutes > decayMin) continue;

			switch (o.getType())
			{
				case PKERS:
					if (minutes < 15) adj += RiskConfig.PKERS_UNDER_15_MIN;
					else if (minutes < 60) adj += RiskConfig.PKERS_UNDER_60_MIN;
					else adj += RiskConfig.PKERS_UNDER_180_MIN;
					break;
				case PLAYERS:
					if (minutes < 15) adj += RiskConfig.PLAYERS_UNDER_15_MIN;
					else if (minutes < 60) adj += RiskConfig.PLAYERS_UNDER_60_MIN;
					else adj += RiskConfig.PLAYERS_UNDER_180_MIN;
					break;
				case EMPTY:
					if (minutes < 10) adj -= RiskConfig.EMPTY_UNDER_10_MIN_BONUS;
					else if (minutes < 30) adj -= RiskConfig.EMPTY_UNDER_30_MIN_BONUS;
					else if (minutes < 60) adj -= RiskConfig.EMPTY_UNDER_60_MIN_BONUS;
					break;
			}
		}
		return adj;
	}

	private Confidence confidenceFor(World world, List<WorldObservation> observations, Instant now, boolean excluded)
	{
		if (excluded) return Confidence.LOW;
		boolean hasRecentObservation = observations.stream()
			.anyMatch(o -> o.getWorldId() == world.getId() &&
				TimeHeuristics.minutesSince(o.getObservedAt(), now) <= 30);
		boolean hasData = world.getActivity() != null;
		int pc = world.getPlayers();
		if (hasData && hasRecentObservation) return Confidence.HIGH;
		if (hasData) return Confidence.MEDIUM;
		return Confidence.LOW;
	}
}
