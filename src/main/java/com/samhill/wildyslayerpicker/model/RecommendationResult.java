package com.samhill.wildyslayerpicker.model;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full recommendation output: best world, backups, and excluded list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult
{
	private Instant generatedAt;
	private ScoredWorld bestWorld;
	private List<ScoredWorld> backups;
	private List<ScoredWorld> excluded;
}
