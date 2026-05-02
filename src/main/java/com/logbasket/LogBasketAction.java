package com.logbasket;

import java.util.Arrays;

public enum LogBasketAction
{
	FILL("Fill"),
	EMPTY("Empty"),
	CHECK("Check");

	private final String menuOption;

	LogBasketAction(String menuOption)
	{
		this.menuOption = menuOption;
	}

	public static LogBasketAction forMenuOption(String option)
	{
		return Arrays.stream(values())
			.filter(a -> a.menuOption.equalsIgnoreCase(option))
			.findFirst()
			.orElse(null);
	}
}
