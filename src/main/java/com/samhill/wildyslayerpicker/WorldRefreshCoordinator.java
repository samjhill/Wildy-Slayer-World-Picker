package com.samhill.wildyslayerpicker;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.worlds.World;
import net.runelite.client.game.WorldService;

/**
 * Handles world list refresh and exposes current world list with debouncing.
 */
@Singleton
@Slf4j
public class WorldRefreshCoordinator
{
	private static final long REFRESH_DEBOUNCE_MS = 2_000;

	private final WorldService worldService;
	private final AtomicReference<Long> lastRefreshAt = new AtomicReference<>(0L);

	@Inject
	public WorldRefreshCoordinator(WorldService worldService)
	{
		this.worldService = worldService;
	}

	/**
	 * Trigger a refresh (debounced).
	 */
	public void refresh()
	{
		long now = System.currentTimeMillis();
		if (now - lastRefreshAt.get() < REFRESH_DEBOUNCE_MS)
		{
			log.debug("Debouncing world refresh");
			return;
		}
		lastRefreshAt.set(now);
		worldService.refresh();
	}

	/**
	 * Get current world list from WorldService (may be null if not loaded).
	 */
	public List<World> getWorlds()
	{
		var result = worldService.getWorlds();
		if (result == null)
		{
			return Collections.emptyList();
		}
		List<World> list = result.getWorlds();
		return list != null ? list : Collections.emptyList();
	}

	public long getLastRefreshAt()
	{
		return lastRefreshAt.get();
	}
}
