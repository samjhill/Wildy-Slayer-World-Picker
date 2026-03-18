package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import com.samhill.wildyslayerpicker.util.JsonUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Persists and loads user observations via ConfigManager.
 */
@Singleton
@Slf4j
public class ObservationStore
{
	static final String CONFIG_GROUP = "wildyslayerworldpicker";
	private static final String KEY_OBSERVATIONS = "observations";
	private static final String KEY_BLACKLIST = "blacklistedWorlds";
	private static final int DEFAULT_STALE_MINUTES = 180;

	private final ConfigManager configManager;
	private final List<WorldObservation> memory = new CopyOnWriteArrayList<>();
	private volatile boolean loaded;

	@Inject
	public ObservationStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	private int getDecayMinutes()
	{
		String v = configManager.getConfiguration(CONFIG_GROUP, "observationDecayMinutes");
		if (v == null || v.isEmpty()) return DEFAULT_STALE_MINUTES;
		try
		{
			int min = Integer.parseInt(v);
			return min > 0 ? Math.min(min, 10080) : DEFAULT_STALE_MINUTES; // cap 1 week
		}
		catch (NumberFormatException e)
		{
			return DEFAULT_STALE_MINUTES;
		}
	}

	/**
	 * Load observations from config and prune stale. Call once at startup.
	 */
	public void load()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, KEY_OBSERVATIONS);
		List<WorldObservation> list = JsonUtil.observationsFromJson(json);
		int decayMin = getDecayMinutes();
		Instant cutoff = Instant.now().minusSeconds(decayMin * 60L);
		List<WorldObservation> pruned = list.stream()
			.filter(o -> o.getObservedAt() != null && !o.getObservedAt().isBefore(cutoff))
			.collect(Collectors.toList());
		memory.clear();
		memory.addAll(pruned);
		loaded = true;
		if (pruned.size() != list.size())
		{
			save();
		}
	}

	private void save()
	{
		configManager.setConfiguration(CONFIG_GROUP, KEY_OBSERVATIONS, JsonUtil.observationsToJson(new ArrayList<>(memory)));
	}

	public List<WorldObservation> getAll()
	{
		if (!loaded)
		{
			load();
		}
		return new ArrayList<>(memory);
	}

	public void add(int worldId, ObservationType type, String note)
	{
		WorldObservation obs = WorldObservation.builder()
			.worldId(worldId)
			.type(type)
			.observedAt(Instant.now())
			.note(note)
			.build();
		// Keep only latest observation per world per type (or replace any for that world for simplicity)
		memory.removeIf(o -> o.getWorldId() == worldId);
		memory.add(obs);
		save();
		log.debug("Observation added: world={} type={}", worldId, type);
	}

	public void removeForWorld(int worldId)
	{
		memory.removeIf(o -> o.getWorldId() == worldId);
		save();
	}

	/**
	 * Remove observations older than configured decay; persist.
	 */
	public void clearStale()
	{
		int decayMin = getDecayMinutes();
		Instant cutoff = Instant.now().minusSeconds(decayMin * 60L);
		memory.removeIf(o -> o.getObservedAt() == null || o.getObservedAt().isBefore(cutoff));
		save();
		log.debug("Cleared stale observations");
	}

	public void clearAll()
	{
		memory.clear();
		save();
	}

	// ---- Blacklist persistence ----

	public Set<Integer> getBlacklistedWorlds()
	{
		String raw = configManager.getConfiguration(CONFIG_GROUP, KEY_BLACKLIST);
		if (raw == null || raw.isBlank()) return new HashSet<>();
		Set<Integer> set = new HashSet<>();
		for (String s : raw.split(","))
		{
			s = s.trim();
			if (!s.isEmpty())
			{
				try
				{
					set.add(Integer.parseInt(s));
				}
				catch (NumberFormatException ignored) { }
			}
		}
		return set;
	}

	public void setBlacklistedWorlds(Set<Integer> worldIds)
	{
		if (worldIds == null || worldIds.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, KEY_BLACKLIST);
			return;
		}
		String value = worldIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		configManager.setConfiguration(CONFIG_GROUP, KEY_BLACKLIST, value);
	}

	public void addBlacklistedWorld(int worldId)
	{
		Set<Integer> set = new HashSet<>(getBlacklistedWorlds());
		set.add(worldId);
		setBlacklistedWorlds(set);
	}

	public void removeBlacklistedWorld(int worldId)
	{
		Set<Integer> set = new HashSet<>(getBlacklistedWorlds());
		set.remove(worldId);
		setBlacklistedWorlds(set);
	}
}
