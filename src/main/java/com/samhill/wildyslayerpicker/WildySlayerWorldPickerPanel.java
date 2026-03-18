package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.Confidence;
import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.RecommendationResult;
import com.samhill.wildyslayerpicker.model.ScoredWorld;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.client.ui.PluginPanel;

/**
 * Sidebar panel: best world card, backup list, quick report buttons, refresh.
 */
public class WildySlayerWorldPickerPanel extends PluginPanel
{
	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

	private final Client client;
	private final ObservationStore observationStore;
	private final WorldRefreshCoordinator refreshCoordinator;
	private final RiskScorer riskScorer;
	private final WildySlayerWorldPickerConfig config;
	private final Set<Integer> blacklistedWorlds;
	private final Runnable onRefreshRequest;
	private final Runnable onRecommendationChanged;
	private final Runnable onBlacklistChanged;

	private final JPanel topControls = new JPanel();
	private final JButton refreshButton = new JButton("Refresh");
	private final JButton clearStaleButton = new JButton("Clear stale");
	private final JPanel bestWorldCard = new JPanel();
	private final JPanel backupListPanel = new JPanel();
	private final JScrollPane backupScroll = new JScrollPane(backupListPanel);
	private final JPanel footerPanel = new JPanel();
	private final JLabel bestWorldLabel = new JLabel("—");
	private final JLabel bestScoreLabel = new JLabel("—");
	private final JLabel bestConfidenceLabel = new JLabel("—");
	private final JLabel bestReasonsLabel = new JLabel("—");
	private final JLabel observationAgeLabel = new JLabel("Obs: —");
	private final JLabel lastRefreshLabel = new JLabel("Last refresh: —");
	private final JLabel countsLabel = new JLabel("—");
	private final JLabel currentWorldLabel = new JLabel("Current world: —");
	private final JPanel reportButtonsPanel = new JPanel();
	private final JButton markEmptyButton = new JButton("Empty");
	private final JButton sawPlayersButton = new JButton("Players");
	private final JButton sawPkersButton = new JButton("PKers");
	private final JPanel blacklistPanel = new JPanel();
	private final JLabel blacklistLabel = new JLabel("");
	private final JButton clearBlacklistButton = new JButton("Clear blacklist");

	private RecommendationResult lastResult;
	private int currentWorldId = -1;

	public WildySlayerWorldPickerPanel(
		Client client,
		ObservationStore observationStore,
		WorldRefreshCoordinator refreshCoordinator,
		RiskScorer riskScorer,
		WildySlayerWorldPickerConfig config,
		Set<Integer> blacklistedWorlds,
		Runnable onRefreshRequest,
		Runnable onRecommendationChanged,
		Runnable onBlacklistChanged)
	{
		this.client = client;
		this.observationStore = observationStore;
		this.refreshCoordinator = refreshCoordinator;
		this.riskScorer = riskScorer;
		this.config = config;
		this.blacklistedWorlds = blacklistedWorlds != null ? blacklistedWorlds : new java.util.HashSet<>();
		this.onRefreshRequest = onRefreshRequest;
		this.onRecommendationChanged = onRecommendationChanged;
		this.onBlacklistChanged = onBlacklistChanged != null ? onBlacklistChanged : () -> {};

		setLayout(new BorderLayout(0, 10));
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Header
		JPanel header = new JPanel(new BorderLayout());
		header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		JPanel headerText = new JPanel();
		headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
		JLabel title = new JLabel("Wildy Slayer World Picker");
		title.setAlignmentX(LEFT_ALIGNMENT);
		JLabel subtitle = new JLabel("Revenant cave risk heuristic");
		subtitle.setAlignmentX(LEFT_ALIGNMENT);
		subtitle.setFont(subtitle.getFont().deriveFont(10f));
		headerText.add(title);
		headerText.add(subtitle);
		header.add(headerText, BorderLayout.NORTH);
		add(header, BorderLayout.NORTH);

		// Top controls
		topControls.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
		topControls.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		refreshButton.setMargin(new Insets(4, 8, 4, 8));
		clearStaleButton.setMargin(new Insets(4, 8, 4, 8));
		clearStaleButton.setToolTipText("Clear stale reports");
		topControls.add(refreshButton);
		topControls.add(clearStaleButton);
		refreshButton.addActionListener(e -> {
			if (onRefreshRequest != null) onRefreshRequest.run();
		});
		clearStaleButton.addActionListener(e -> {
			observationStore.clearStale();
			if (onRecommendationChanged != null) onRecommendationChanged.run();
		});
		JLabel settingsHint = new JLabel("Settings: RuneLite sidebar → wrench → Wildy Slayer World Picker");
		settingsHint.setFont(settingsHint.getFont().deriveFont(10f));
		settingsHint.setToolTipText("Open RuneLite configuration for this plugin");
		topControls.add(settingsHint);
		add(topControls, BorderLayout.PAGE_START);

		// Best world card
		bestWorldCard.setLayout(new BoxLayout(bestWorldCard, BoxLayout.Y_AXIS));
		bestWorldCard.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("Best world"),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)));
		bestWorldCard.add(bestWorldLabel);
		bestWorldCard.add(bestScoreLabel);
		bestWorldCard.add(bestConfidenceLabel);
		bestWorldCard.add(bestReasonsLabel);
		bestWorldCard.add(observationAgeLabel);
		JPanel bestActions = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 4));
		JButton copyWorldButton = new JButton("Copy #");
		copyWorldButton.setToolTipText("Copy world number");
		copyWorldButton.setMargin(new Insets(4, 8, 4, 8));
		copyWorldButton.addActionListener(e -> copyBestWorldNumber());
		JButton blacklistBestButton = new JButton("Blacklist");
		blacklistBestButton.setMargin(new Insets(4, 8, 4, 8));
		blacklistBestButton.addActionListener(e -> {
			if (lastResult != null && lastResult.getBestWorld() != null && lastResult.getBestWorld().getWorld() != null)
			{
				blacklistedWorlds.add(lastResult.getBestWorld().getWorld().getWorldId());
				onBlacklistChanged.run();
			}
		});
		bestActions.add(copyWorldButton);
		bestActions.add(blacklistBestButton);
		bestWorldCard.add(bestActions);
		// Stack report buttons vertically so they don't truncate
		reportButtonsPanel.setLayout(new GridLayout(3, 1, 0, 4));
		markEmptyButton.setToolTipText("Mark cave empty");
		sawPlayersButton.setToolTipText("Saw players");
		sawPkersButton.setToolTipText("Saw PKers");
		markEmptyButton.setMargin(new Insets(4, 8, 4, 8));
		sawPlayersButton.setMargin(new Insets(4, 8, 4, 8));
		sawPkersButton.setMargin(new Insets(4, 8, 4, 8));
		reportButtonsPanel.add(markEmptyButton);
		reportButtonsPanel.add(sawPlayersButton);
		reportButtonsPanel.add(sawPkersButton);
		reportButtonsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
		bestWorldCard.add(reportButtonsPanel);

		markEmptyButton.addActionListener(e -> reportCurrentBest(ObservationType.EMPTY));
		sawPlayersButton.addActionListener(e -> reportCurrentBest(ObservationType.PLAYERS));
		sawPkersButton.addActionListener(e -> reportCurrentBest(ObservationType.PKERS));
		clearBlacklistButton.setMargin(new Insets(2, 6, 2, 6));
		clearBlacklistButton.addActionListener(e -> {
			blacklistedWorlds.clear();
			onBlacklistChanged.run();
		});

		// Center: best card + backup list
		JPanel center = new JPanel(new BorderLayout(0, 10));
		center.add(bestWorldCard, BorderLayout.PAGE_START);
		backupListPanel.setLayout(new BoxLayout(backupListPanel, BoxLayout.Y_AXIS));
		backupScroll.setPreferredSize(new Dimension(0, 140));
		backupScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		center.add(backupScroll, BorderLayout.CENTER);
		blacklistPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 4, 4));
		blacklistPanel.add(blacklistLabel);
		blacklistPanel.add(clearBlacklistButton);
		blacklistLabel.setFont(blacklistLabel.getFont().deriveFont(10f));
		center.add(blacklistPanel, BorderLayout.PAGE_END);
		add(center, BorderLayout.CENTER);

		// Footer
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		footerPanel.add(lastRefreshLabel);
		footerPanel.add(countsLabel);
		footerPanel.add(currentWorldLabel);
		add(footerPanel, BorderLayout.SOUTH);

		setEmptyState("World data unavailable. Try refresh.");
	}

	private void copyBestWorldNumber()
	{
		if (lastResult == null || lastResult.getBestWorld() == null) return;
		String num = String.valueOf(lastResult.getBestWorld().getWorld().getWorldId());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(num), null);
	}

	private void reportCurrentBest(ObservationType type)
	{
		if (lastResult == null || lastResult.getBestWorld() == null) return;
		int worldId = lastResult.getBestWorld().getWorld().getWorldId();
		observationStore.add(worldId, type, null);
		if (onRecommendationChanged != null) onRecommendationChanged.run();
	}

	public void setRecommendation(RecommendationResult result, int currentWorldId, Set<Integer> blacklistedWorlds,
		List<WorldObservation> observations)
	{
		this.lastResult = result;
		this.currentWorldId = currentWorldId;
		SwingUtilities.invokeLater(() -> {
			if (result == null)
			{
				setEmptyState("World data unavailable. Try refresh.");
				return;
			}
			List<ScoredWorld> allEligible = new ArrayList<>();
			if (result.getBestWorld() != null) allEligible.add(result.getBestWorld());
			if (result.getBackups() != null) allEligible.addAll(result.getBackups());
			int considered = allEligible.size();
			int excludedCount = result.getExcluded() != null ? result.getExcluded().size() : 0;

			if (result.getBestWorld() == null)
			{
				setEmptyState("No eligible worlds found. Relax filters.");
				bestWorldLabel.setText("—");
				bestScoreLabel.setText("—");
				bestConfidenceLabel.setText("—");
				bestReasonsLabel.setText("—");
				observationAgeLabel.setText("Obs: —");
				markEmptyButton.setEnabled(false);
				sawPlayersButton.setEnabled(false);
				sawPkersButton.setEnabled(false);
			}
			else
			{
				ScoredWorld best = result.getBestWorld();
				int bestWorldId = best.getWorld().getWorldId();
				bestWorldLabel.setText("Best world: " + bestWorldId);
				bestScoreLabel.setText("Risk score: " + best.getRiskScore());
				bestConfidenceLabel.setText("Confidence: " + best.getConfidence());
				String reasons = best.getReasons() != null && !best.getReasons().isEmpty()
					? String.join("; ", best.getReasons()) : "—";
				bestReasonsLabel.setText(reasons);
				bestReasonsLabel.setToolTipText(reasons);
				// Age of latest observation for this world
				if (observations != null && !observations.isEmpty())
				{
					long ageMin = observations.stream()
						.filter(o -> o.getWorldId() == bestWorldId && o.getObservedAt() != null)
						.mapToLong(o -> java.time.Duration.between(o.getObservedAt(), java.time.Instant.now()).toMinutes())
						.min().orElse(-1);
					if (ageMin >= 0) observationAgeLabel.setText("Obs: " + ageMin + "m ago");
					else observationAgeLabel.setText("Obs: —");
				}
				else observationAgeLabel.setText("Obs: —");
				markEmptyButton.setEnabled(true);
				sawPlayersButton.setEnabled(true);
				sawPkersButton.setEnabled(true);
			}

			// Backup list
			backupListPanel.removeAll();
			if (result.getBackups() != null && !result.getBackups().isEmpty())
			{
				int rank = 2;
				for (ScoredWorld sw : result.getBackups())
				{
					if (sw != null && sw.getWorld() != null)
					{
						JPanel row = buildBackupRow(rank++, sw, blacklistedWorlds);
						backupListPanel.add(row);
					}
				}
			}
			if (result.getBestWorld() != null && (result.getBackups() == null || result.getBackups().isEmpty()))
			{
				JLabel noBackup = new JLabel("No other eligible worlds.");
				noBackup.setFont(noBackup.getFont().deriveFont(10f));
				backupListPanel.add(noBackup);
			}
			backupListPanel.revalidate();
			backupListPanel.repaint();

			// Blacklist line
			if (blacklistedWorlds != null && !blacklistedWorlds.isEmpty())
			{
				String ids = blacklistedWorlds.stream().sorted().map(id -> "W" + id).collect(Collectors.joining(", "));
				blacklistLabel.setText("Blacklisted: " + ids);
				blacklistPanel.setVisible(true);
			}
			else
			{
				blacklistLabel.setText("");
				blacklistPanel.setVisible(false);
			}

			// Footer
			if (result.getGeneratedAt() != null)
			{
				lastRefreshLabel.setText("Generated: " + TIME_FMT.format(result.getGeneratedAt()));
			}
			countsLabel.setText("Considered: " + considered + "  Excluded: " + excludedCount);
			boolean inShortlist = result.getBestWorld() != null && result.getBestWorld().getWorld().getWorldId() == currentWorldId
				|| (result.getBackups() != null && result.getBackups().stream().anyMatch(sw -> sw.getWorld().getWorldId() == currentWorldId));
			currentWorldLabel.setText("Current world: " + (currentWorldId > 0 ? currentWorldId : "—")
				+ (currentWorldId > 0 && inShortlist ? " (in shortlist)" : (currentWorldId > 0 ? " (not in shortlist)" : "")));
		});
	}

	private JPanel buildBackupRow(int rank, ScoredWorld sw, Set<Integer> blacklistedWorlds)
	{
		com.samhill.wildyslayerpicker.model.WorldCandidate w = sw.getWorld();
		if (w == null) return new JPanel();
		int worldId = w.getWorldId();
		String conf = sw.getConfidence() != null ? sw.getConfidence().name().substring(0, Math.min(3, sw.getConfidence().name().length())) : "—";
		String line = rank + ". W" + worldId + "  pop " + w.getPlayerCount() + "  risk " + sw.getRiskScore() + "  " + conf;
		String tooltip = "World " + worldId + " | pop " + w.getPlayerCount() + " | risk " + sw.getRiskScore() + " | " + sw.getConfidence();
		if (sw.getReasons() != null && !sw.getReasons().isEmpty())
		{
			tooltip += " | " + String.join("; ", sw.getReasons());
		}

		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
		row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

		JLabel label = new JLabel(line);
		label.setToolTipText(tooltip);
		row.add(label);

		JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
		JButton empty = new JButton("Empty");
		JButton players = new JButton("Players");
		JButton pkers = new JButton("PKers");
		JButton blacklistBtn = new JButton("Blacklist");
		empty.setMargin(new Insets(2, 6, 2, 6));
		players.setMargin(new Insets(2, 6, 2, 6));
		pkers.setMargin(new Insets(2, 6, 2, 6));
		blacklistBtn.setMargin(new Insets(2, 6, 2, 6));
		empty.addActionListener(e -> { observationStore.add(worldId, ObservationType.EMPTY, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		players.addActionListener(e -> { observationStore.add(worldId, ObservationType.PLAYERS, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		pkers.addActionListener(e -> { observationStore.add(worldId, ObservationType.PKERS, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		blacklistBtn.addActionListener(e -> {
			blacklistedWorlds.add(worldId);
			onBlacklistChanged.run();
		});
		btns.add(empty);
		btns.add(players);
		btns.add(pkers);
		btns.add(blacklistBtn);
		row.add(btns);
		return row;
	}

	private void setEmptyState(String message)
	{
		bestWorldLabel.setText(message);
		bestScoreLabel.setText("—");
		bestConfidenceLabel.setText("—");
		bestReasonsLabel.setText("—");
		markEmptyButton.setEnabled(false);
		sawPlayersButton.setEnabled(false);
		sawPkersButton.setEnabled(false);
		backupListPanel.removeAll();
		backupListPanel.revalidate();
		backupListPanel.repaint();
	}

	public void setCurrentWorld(int worldId)
	{
		this.currentWorldId = worldId;
		SwingUtilities.invokeLater(() -> currentWorldLabel.setText("Current world: " + (worldId > 0 ? worldId : "—")));
	}
}
