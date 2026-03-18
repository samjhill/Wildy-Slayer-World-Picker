package com.samhill.wildyslayerpicker.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A world with computed risk score, confidence, and reasons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoredWorld
{
	private WorldCandidate world;
	private int riskScore;
	private Confidence confidence;
	private List<String> reasons;
	private boolean excluded;
	private String exclusionReason;
}
