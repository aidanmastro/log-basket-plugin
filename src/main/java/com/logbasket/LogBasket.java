package com.logbasket;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.runelite.api.gameval.ItemID;

public enum LogBasket
{
	STATE;

	public static final int EMPTY = 0;
	public static final int FULL = 28;

	public static final Collection<Integer> BASKET_IDS = ImmutableList.of(
		ItemID.LOG_BASKET_OPEN,
		ItemID.LOG_BASKET_CLOSED,
		ItemID.FORESTRY_BASKET_OPEN,
		ItemID.FORESTRY_BASKET_CLOSED
	);

	public static final Collection<Integer> OPEN_BASKET_IDS = ImmutableList.of(
		ItemID.LOG_BASKET_OPEN,
		ItemID.FORESTRY_BASKET_OPEN
	);

	private int storedLogs;
	private boolean isUnknown = true;

	public int getStoredLogs()
	{
		return storedLogs;
	}

	public void setStoredLogs(int storedLogs)
	{
		this.storedLogs = storedLogs;
	}

	public boolean isUnknown()
	{
		return isUnknown;
	}

	public void setUnknown(boolean unknown)
	{
		isUnknown = unknown;
	}
}
