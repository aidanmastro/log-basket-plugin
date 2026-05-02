package com.logbasket;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("logbasket")
public interface LogBasketConfig extends Config
{
	@ConfigItem(
		keyName = "showInfoBox",
		name = "Show InfoBox",
		description = "Show a RuneLite InfoBox with the current log basket count"
	)
	default boolean showInfoBox()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showItemOverlay",
		name = "Show inventory overlay",
		description = "Show total log count inside of the log basket as an inventory overlay"
	)
	default boolean showItemOverlay()
	{
		return true;
	}
}
