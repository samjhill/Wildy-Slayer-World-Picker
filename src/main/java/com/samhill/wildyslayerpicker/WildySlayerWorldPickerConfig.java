package com.samhill.wildyslayerpicker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("wildyslayerworldpicker")
public interface WildySlayerWorldPickerConfig extends Config
{
	@ConfigItem(
		keyName = "excludeSkillTotalWorlds",
		name = "Exclude skill total worlds",
		description = "Hide total-level worlds from recommendations"
	)
	default boolean excludeSkillTotalWorlds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "excludeHighPopulationWorlds",
		name = "Exclude high population worlds",
		description = "Remove worlds above a threshold"
	)
	default boolean excludeHighPopulationWorlds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "maxPopulation",
		name = "Max population",
		description = "Maximum population allowed when high-pop exclusion is enabled"
	)
	default int maxPopulation()
	{
		return 900;
	}

	@ConfigItem(
		keyName = "preferOffPeak",
		name = "Prefer off-peak worlds",
		description = "Apply a small penalty during likely peak play windows"
	)
	default boolean preferOffPeak()
	{
		return false;
	}

	@ConfigItem(
		keyName = "includeActivityPenalty",
		name = "Include activity penalty",
		description = "Add small penalty for busy activity worlds (e.g. trade, wintertodt)"
	)
	default boolean includeActivityPenalty()
	{
		return true;
	}

	@ConfigItem(
		keyName = "topListSize",
		name = "Top list size",
		description = "How many recommended worlds to show"
	)
	default int topListSize()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "observationDecayMinutes",
		name = "Observation decay (minutes)",
		description = "Observations older than this are ignored for scoring and can be cleared"
	)
	default int observationDecayMinutes()
	{
		return 180;
	}

	@ConfigItem(
		keyName = "debugMode",
		name = "Debug mode",
		description = "Show score breakdown and logs"
	)
	default boolean debugMode()
	{
		return false;
	}
}
