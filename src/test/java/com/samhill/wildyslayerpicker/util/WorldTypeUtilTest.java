package com.samhill.wildyslayerpicker.util;

import java.util.EnumSet;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorldTypeUtilTest
{
	@Mock
	private World world;

	@Test
	public void isAlwaysExcluded_nullWorld()
	{
		assertTrue(WorldTypeUtil.isAlwaysExcluded(null));
	}

	@Test
	public void isAlwaysExcluded_nullTypes()
	{
		when(world.getTypes()).thenReturn(null);
		assertTrue(WorldTypeUtil.isAlwaysExcluded(world));
	}

	@Test
	public void isAlwaysExcluded_nonMembers()
	{
		when(world.getTypes()).thenReturn(EnumSet.noneOf(WorldType.class));
		assertTrue(WorldTypeUtil.isAlwaysExcluded(world));
	}

	@Test
	public void isAlwaysExcluded_pvp()
	{
		when(world.getTypes()).thenReturn(EnumSet.of(WorldType.MEMBERS, WorldType.PVP));
		assertTrue(WorldTypeUtil.isAlwaysExcluded(world));
	}

	@Test
	public void isAlwaysExcluded_membersOnly_notExcluded()
	{
		when(world.getTypes()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		assertFalse(WorldTypeUtil.isAlwaysExcluded(world));
	}

	@Test
	public void isSkillTotalWorld_null()
	{
		assertFalse(WorldTypeUtil.isSkillTotalWorld(null));
	}

	@Test
	public void isSkillTotalWorld_hasSkillTotal()
	{
		when(world.getTypes()).thenReturn(EnumSet.of(WorldType.MEMBERS, WorldType.SKILL_TOTAL));
		assertTrue(WorldTypeUtil.isSkillTotalWorld(world));
	}

	@Test
	public void isSkillTotalWorld_noSkillTotal()
	{
		when(world.getTypes()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		assertFalse(WorldTypeUtil.isSkillTotalWorld(world));
	}
}
