package com.samhill.wildyslayerpicker.util;

import java.util.EnumSet;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;

/**
 * Helpers for world type checks (exclusions).
 */
public final class WorldTypeUtil
{
	private WorldTypeUtil() {}

	/**
	 * True if this world should always be excluded for rev-cave recommendations
	 * (non-members, PvP, high-risk, seasonal, tournament, fresh start, deadman).
	 */
	public static boolean isAlwaysExcluded(World world)
	{
		if (world == null)
		{
			return true;
		}
		EnumSet<WorldType> types = world.getTypes();
		if (types == null)
		{
			return true;
		}
		// Rev caves are members-only
		if (!types.contains(WorldType.MEMBERS))
		{
			return true;
		}
		if (types.contains(WorldType.PVP))
		{
			return true;
		}
		if (types.contains(WorldType.HIGH_RISK))
		{
			return true;
		}
		if (types.contains(WorldType.SEASONAL))
		{
			return true;
		}
		if (containsTournamentWorld(types))
		{
			return true;
		}
		if (types.contains(WorldType.FRESH_START_WORLD))
		{
			return true;
		}
		if (types.contains(WorldType.DEADMAN))
		{
			return true;
		}
		return false;
	}

	public static boolean isSkillTotalWorld(World world)
	{
		return world != null && world.getTypes() != null && world.getTypes().contains(WorldType.SKILL_TOTAL);
	}

	private static boolean containsTournamentWorld(EnumSet<WorldType> types)
	{
		if (types == null) return false;
		for (WorldType t : types)
		{
			if (t != null && t.name().contains("TOURNAMENT")) return true;
		}
		return false;
	}
}
