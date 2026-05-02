package com.logbasket;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class LogBasketOverlay extends WidgetItemOverlay
{
	private final LogBasketConfig config;

	@Inject
	public LogBasketOverlay(LogBasketConfig config)
	{
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!config.showItemOverlay())
		{
			return;
		}

		if (!LogBasket.BASKET_IDS.contains(itemId))
		{
			return;
		}

		final Rectangle bounds = widgetItem.getCanvasBounds();

		final Color green = new Color(0x00FF9A);

		if (LogBasket.STATE.isUnknown())
		{
			drawCount(graphics, "?", bounds, green);
			return;
		}

		drawCount(graphics, String.valueOf(LogBasket.STATE.getStoredLogs()), bounds, green);
	}

	private void drawCount(Graphics2D graphics, String text, Rectangle bounds, Color colour)
	{
		final FontMetrics fm = graphics.getFontMetrics();
		final int x = bounds.x + 1;
		final int y = bounds.y + fm.getAscent();

		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(colour);
		graphics.drawString(text, x, y);
	}
}
