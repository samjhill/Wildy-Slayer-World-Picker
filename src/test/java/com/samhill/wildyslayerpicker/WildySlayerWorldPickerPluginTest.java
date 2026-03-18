package com.samhill.wildyslayerpicker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Launches RuneLite with this plugin loaded for local testing.
 */
public class WildySlayerWorldPickerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WildySlayerWorldPickerPlugin.class);
		RuneLite.main(args);
	}
}
