package com.samhill.wildyslayerpicker;

import com.samhill.wildyslayerpicker.model.RecommendationResult;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class RiskScorerTest
{
	@Mock
	private WildySlayerWorldPickerConfig config;

	private final RiskScorer riskScorer = new RiskScorer();

	@Test
	public void score_nullWorlds_returnsEmptyResult()
	{
		RecommendationResult result = riskScorer.score(
			null,
			Collections.emptyList(),
			config,
			Instant.now(),
			new HashSet<>()
		);
		assertNotNull(result);
		assertNull(result.getBestWorld());
		assertTrue(result.getBackups() == null || result.getBackups().isEmpty());
	}

	@Test
	public void score_emptyWorlds_returnsEmptyResult()
	{
		RecommendationResult result = riskScorer.score(
			Collections.emptyList(),
			Collections.emptyList(),
			config,
			Instant.now(),
			new HashSet<>()
		);
		assertNotNull(result);
		assertNull(result.getBestWorld());
		assertTrue(result.getBackups() == null || result.getBackups().isEmpty());
	}
}
