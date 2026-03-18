package com.samhill.wildyslayerpicker.model;

import java.util.EnumSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.http.api.worlds.WorldType;

/**
 * Normalized view of a world from RuneLite for scoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldCandidate
{
	private int worldId;
	private String activity;
	private int location;
	private int playerCount;
	private EnumSet<WorldType> types;
}
