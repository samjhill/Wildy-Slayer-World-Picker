package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import com.samhill.wildyslayerpicker.util.JsonUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
	private static final int STALE_MINUTES = 180;

	private final ConfigManager configManager;
	private final List<WorldObservation> memory = new CopyOnWriteArrayList<>();
	private volatile boolean loaded;

	@Inject
	public ObservationStore(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	/**
	 * Load observations from config and prune stale. Call once at startup.
	 */
	public void load()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, KEY_OBSERVATIONS);
		List<WorldObservation> list = JsonUtil.observationsFromJson(json);
		Instant cutoff = Instant.now().minusSeconds(STALE_MINUTES * 60L);
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
		Instant cutoff = Instant.now().minusSeconds(STALE_MINUTES * 60L);
		memory.removeIf(o -> o.getObservedAt() == null || o.getObservedAt().isBefore(cutoff));
		save();
		log.debug("Cleared stale observations");
	}

	public void clearAll()
	{
		memory.clear();
		save();
	}
}
