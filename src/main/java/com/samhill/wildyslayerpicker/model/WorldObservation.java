package com.samhill.wildyslayerpicker.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single user observation for a world (empty / players / pkers).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldObservation
{
	private int worldId;
	private ObservationType type;
	private Instant observedAt;
	private String note;
}
