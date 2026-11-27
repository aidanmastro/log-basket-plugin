package com.logbasket;

import net.runelite.client.ui.overlay.infobox.InfoBox;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class LogBasketInfobox extends InfoBox
{
	private final LogBasketConfig config;

	LogBasketInfobox(LogBasketPlugin plugin, LogBasketConfig config, BufferedImage image)
	{
    	super(image, plugin);
		this.config = config;
		setTooltip("Log Basket Count");
	}

	@Override
	public String getText()
	{
		if (LogBasket.STATE.isUnknown())
		{
			return "?";
		}
		return String.valueOf(LogBasket.STATE.getStoredLogs());
	}

	@Override
	public Color getTextColor()
	{
		return config.infoboxColour();
	}
}
