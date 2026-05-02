package com.logbasket;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import net.runelite.api.gameval.ItemID;

public final class InfernalAxe
{
	private InfernalAxe()
	{
	}

	public static final Collection<Integer> CHARGED_AXE_IDS = ImmutableList.of(
		ItemID.INFERNAL_AXE,
		25066,  // Infernal Axe (or)
		30347   // Infernal Axe (or) (Trailblazer Reloaded)
	);
}
