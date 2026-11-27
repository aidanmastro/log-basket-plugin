package com.logbasket;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(LogBasketConfig.GROUP)
public interface LogBasketConfig extends Config
{
	String GROUP = "coffincounter";

	@ConfigItem(
		keyName = "showItemOverlay",
		name = "Show inventory overlay",
		description = "Show total log count inside of the log basket as an inventory overlay"
	)
	default boolean showItemOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showInfobox",
		name = "Show infobox",
		description = "Show total log count inside of the log basket as an infobox"
	)
	default boolean showInfobox()
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

	@Alpha
	@ConfigItem(
		keyName = "infoboxColour",
		name = "Infobox colour",
		description = "Colour of the text in the infobox"
	)
	default Color infoboxColour()
	{
		return new Color(0x00FF9A);
	}
}
