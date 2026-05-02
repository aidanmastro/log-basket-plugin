package com.logbasket;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class LogBasketInfoBox extends InfoBox
{
	public LogBasketInfoBox(BufferedImage image, Plugin plugin)
	{
		super(image, plugin);
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
		if (LogBasket.STATE.isUnknown())
		{
			return Color.YELLOW;
		}
		if (LogBasket.STATE.getStoredLogs() >= LogBasket.FULL)
		{
			return Color.RED;
		}
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		if (LogBasket.STATE.isUnknown())
		{
			return "Select 'Check' on log basket";
		}
		return "Log basket: " + LogBasket.STATE.getStoredLogs() + " / " + LogBasket.FULL + " logs";
	}
}
