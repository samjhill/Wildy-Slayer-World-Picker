package com.samhill.wildyslayerpicker;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WorldListLoad;
import net.runelite.api.events.WorldChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.WorldsFetch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

/**
 * RuneLite plugin: recommends OSRS world with lowest estimated rev-cave risk for Wilderness Slayer.
 * Heuristic only; no guarantee of safety.
 */
@PluginDescriptor(
	name = "Wildy Slayer World Picker",
	description = "Ranks OSRS worlds by estimated revenant cave risk for Wilderness Slayer",
	tags = {"wildy", "slayer", "revenants", "worlds", "panel"}
)
@Slf4j
public class WildySlayerWorldPickerPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ConfigManager configManager;
	@Inject
	private WildySlayerWorldPickerConfig config;
	@Inject
	private ObservationStore observationStore;
	@Inject
	private WorldRefreshCoordinator refreshCoordinator;
	@Inject
	private RiskScorer riskScorer;

	private NavigationButton navButton;
	private WildySlayerWorldPickerPanel panel;
	private final Set<Integer> blacklistedWorlds = new HashSet<>();

	@Provides
	WildySlayerWorldPickerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildySlayerWorldPickerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		observationStore.load();
		panel = new WildySlayerWorldPickerPanel(
			client,
			observationStore,
			refreshCoordinator,
			riskScorer,
			config,
			this::refreshAndRecompute,
			this::recomputeAndUpdatePanel
		);

		NavigationButton.NavigationButtonBuilder navBuilder = NavigationButton.builder()
			.tooltip("Wildy Slayer World Picker")
			.panel(panel);
		try
		{
			java.awt.image.BufferedImage icon = ImageUtil.loadImageResource(getClass(), "assets/icon.png");
			if (icon != null)
			{
				navBuilder.icon(icon);
			}
		}
		catch (Exception e)
		{
			log.debug("Could not load panel icon", e);
		}
		navButton = navBuilder.build();
		clientToolbar.addNavigation(navButton);

		refreshCoordinator.refresh();
		recomputeAndUpdatePanel();
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
		panel = null;
	}

	private void refreshAndRecompute()
	{
		refreshCoordinator.refresh();
		recomputeAndUpdatePanel();
	}

	private void recomputeAndUpdatePanel()
	{
		var worlds = refreshCoordinator.getWorlds();
		if (config.debugMode())
		{
			log.debug("Worlds loaded: {}", worlds != null ? worlds.size() : 0);
		}
		var observations = observationStore.getAll();
		var result = riskScorer.score(worlds, observations, config, java.time.Instant.now(), blacklistedWorlds);
		if (config.debugMode() && result.getBestWorld() != null)
		{
			log.debug("Recommendations generated, best world: {}", result.getBestWorld().getWorld().getWorldId());
		}
		int currentWorld = client.getWorld();
		panel.setRecommendation(result, currentWorld, blacklistedWorlds, observations);
		panel.setCurrentWorld(currentWorld);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN || event.getGameState() == GameState.HOPPING)
		{
			recomputeAndUpdatePanel();
		}
	}

	@Subscribe
	public void onWorldChanged(WorldChanged event)
	{
		panel.setCurrentWorld(client.getWorld());
		recomputeAndUpdatePanel();
	}

	@Subscribe
	public void onWorldsFetch(WorldsFetch event)
	{
		recomputeAndUpdatePanel();
	}

	@Subscribe
	public void onWorldListLoad(WorldListLoad event)
	{
		recomputeAndUpdatePanel();
	}
}
