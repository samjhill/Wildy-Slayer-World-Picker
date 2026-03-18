package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.Confidence;
import com.samhill.wildyslayerpicker.model.ObservationType;
import com.samhill.wildyslayerpicker.model.RecommendationResult;
import com.samhill.wildyslayerpicker.model.ScoredWorld;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
	private final Runnable onRefreshRequest;
	private final Runnable onRecommendationChanged;

	private final JPanel topControls = new JPanel();
	private final JButton refreshButton = new JButton("Refresh");
	private final JButton clearStaleButton = new JButton("Clear stale reports");
	private final JPanel bestWorldCard = new JPanel();
	private final JPanel backupListPanel = new JPanel();
	private final JScrollPane backupScroll = new JScrollPane(backupListPanel);
	private final JPanel footerPanel = new JPanel();
	private final JLabel bestWorldLabel = new JLabel("—");
	private final JLabel bestScoreLabel = new JLabel("—");
	private final JLabel bestConfidenceLabel = new JLabel("—");
	private final JLabel bestReasonsLabel = new JLabel("—");
	private final JLabel observationAgeLabel = new JLabel("Latest observation: —");
	private final JLabel lastRefreshLabel = new JLabel("Last refresh: —");
	private final JLabel countsLabel = new JLabel("—");
	private final JLabel currentWorldLabel = new JLabel("Current world: —");
	private final JPanel reportButtonsPanel = new JPanel();
	private final JButton markEmptyButton = new JButton("Mark Empty");
	private final JButton sawPlayersButton = new JButton("Saw Players");
	private final JButton sawPkersButton = new JButton("Saw PKers");

	private RecommendationResult lastResult;
	private int currentWorldId = -1;

	public WildySlayerWorldPickerPanel(
		Client client,
		ObservationStore observationStore,
		WorldRefreshCoordinator refreshCoordinator,
		RiskScorer riskScorer,
		WildySlayerWorldPickerConfig config,
		Runnable onRefreshRequest,
		Runnable onRecommendationChanged)
	{
		this.client = client;
		this.observationStore = observationStore;
		this.refreshCoordinator = refreshCoordinator;
		this.riskScorer = riskScorer;
		this.config = config;
		this.onRefreshRequest = onRefreshRequest;
		this.onRecommendationChanged = onRecommendationChanged;

		setLayout(new BorderLayout(0, 8));

		// Header
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		JLabel title = new JLabel("Wildy Slayer World Picker");
		title.setAlignmentX(LEFT_ALIGNMENT);
		JLabel subtitle = new JLabel("Revenant cave risk heuristic");
		subtitle.setAlignmentX(LEFT_ALIGNMENT);
		subtitle.setFont(subtitle.getFont().deriveFont(10f));
		header.add(title);
		header.add(subtitle);
		add(header, BorderLayout.NORTH);

		// Top controls
		topControls.setLayout(new GridLayout(1, 2, 4, 0));
		topControls.add(refreshButton);
		topControls.add(clearStaleButton);
		refreshButton.addActionListener(e -> {
			if (onRefreshRequest != null) onRefreshRequest.run();
		});
		clearStaleButton.addActionListener(e -> {
			observationStore.clearStale();
			if (onRecommendationChanged != null) onRecommendationChanged.run();
		});
		add(topControls, BorderLayout.PAGE_START);

		// Best world card
		bestWorldCard.setLayout(new BoxLayout(bestWorldCard, BoxLayout.Y_AXIS));
		bestWorldCard.setBorder(BorderFactory.createTitledBorder("Best world"));
		bestWorldCard.add(bestWorldLabel);
		bestWorldCard.add(bestScoreLabel);
		bestWorldCard.add(bestConfidenceLabel);
		bestWorldCard.add(bestReasonsLabel);
		bestWorldCard.add(observationAgeLabel);
		JButton copyWorldButton = new JButton("Copy world number");
		copyWorldButton.addActionListener(e -> copyBestWorldNumber());
		bestWorldCard.add(copyWorldButton);
		reportButtonsPanel.setLayout(new GridLayout(1, 3, 4, 0));
		reportButtonsPanel.add(markEmptyButton);
		reportButtonsPanel.add(sawPlayersButton);
		reportButtonsPanel.add(sawPkersButton);
		bestWorldCard.add(reportButtonsPanel);

		markEmptyButton.addActionListener(e -> reportCurrentBest(ObservationType.EMPTY));
		sawPlayersButton.addActionListener(e -> reportCurrentBest(ObservationType.PLAYERS));
		sawPkersButton.addActionListener(e -> reportCurrentBest(ObservationType.PKERS));

		// Center: best card + backup list
		JPanel center = new JPanel(new BorderLayout(0, 8));
		center.add(bestWorldCard, BorderLayout.PAGE_START);
		backupListPanel.setLayout(new BoxLayout(backupListPanel, BoxLayout.Y_AXIS));
		backupScroll.setPreferredSize(new Dimension(0, 120));
		center.add(backupScroll, BorderLayout.CENTER);
		add(center, BorderLayout.CENTER);

		// Footer
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
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
				observationAgeLabel.setText("Latest observation: —");
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
					if (ageMin >= 0) observationAgeLabel.setText("Latest observation: " + ageMin + "m ago");
					else observationAgeLabel.setText("Latest observation: —");
				}
				else observationAgeLabel.setText("Latest observation: —");
				markEmptyButton.setEnabled(true);
				sawPlayersButton.setEnabled(true);
				sawPkersButton.setEnabled(true);
			}

			// Backup list
			backupListPanel.removeAll();
			if (result.getBackups() != null)
			{
				int rank = 2;
				for (ScoredWorld sw : result.getBackups())
				{
					JPanel row = buildBackupRow(rank++, sw, blacklistedWorlds);
					backupListPanel.add(row);
				}
			}
			backupListPanel.revalidate();
			backupListPanel.repaint();

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
		JPanel row = new JPanel(new BorderLayout());
		int worldId = sw.getWorld().getWorldId();
		String line = rank + ". World " + worldId + " | pop " + sw.getWorld().getPlayerCount()
			+ " | risk " + sw.getRiskScore() + " | " + sw.getConfidence();
		if (sw.getReasons() != null && !sw.getReasons().isEmpty())
		{
			line += " | " + sw.getReasons().get(0);
		}
		JLabel label = new JLabel(line);
		label.setToolTipText(sw.getReasons() != null ? String.join("; ", sw.getReasons()) : "");
		row.add(label, BorderLayout.CENTER);

		JPanel btns = new JPanel(new GridLayout(1, 3, 2, 0));
		JButton empty = new JButton("Empty");
		JButton players = new JButton("Players");
		JButton pkers = new JButton("PKers");
		empty.addActionListener(e -> { observationStore.add(worldId, ObservationType.EMPTY, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		players.addActionListener(e -> { observationStore.add(worldId, ObservationType.PLAYERS, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		pkers.addActionListener(e -> { observationStore.add(worldId, ObservationType.PKERS, null); if (onRecommendationChanged != null) onRecommendationChanged.run(); });
		btns.add(empty);
		btns.add(players);
		btns.add(pkers);
		row.add(btns, BorderLayout.EAST);
		row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
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
