package com.logbasket;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("logbasket")
public interface LogBasketConfig extends Config
{
	@ConfigItem(
		keyName = "showItemOverlay",
		name = "Show inventory overlay",
		description = "Show total log count inside of the log basket as an inventory overlay"
	)
	default boolean showItemOverlay()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "overlayColour",
		name = "Overlay colour",
		description = "Colour of the count drawn on the basket in your inventory"
	)
	default Color overlayColour()
	{
		return new Color(0x00FF9A);
	}
}
