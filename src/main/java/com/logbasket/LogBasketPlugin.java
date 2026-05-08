package com.logbasket;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Log Basket",
	description = "Displays amount of logs contained within the log basket",
	tags = {"woodcutting", "log", "basket", "forestry"}
)
public class LogBasketPlugin extends Plugin
{
	private static final int GAME_TICK_MARGIN = 2;
	private static final int CHECK_WIDGET_INTERFACE = 193;
	private static final int CHECK_WIDGET_COMPONENT = 2;

	private static final String FULL_BASKET_MESSAGE = "The basket is full.";
	private static final String EMPTY_BASKET_MESSAGE = "Your basket is empty.";
	private static final String CHECKED_EMPTY_BASKET_MESSAGE = "The basket is empty.";
	private static final String EMPTIED_TO_BANK_MESSAGE = "You empty your basket into the bank.";
	private static final String EMPTIED_ALL_TO_INVENTORY_MESSAGE = "You empty your basket.";
	private static final String EMPTIED_PARTIAL_TO_INVENTORY_MESSAGE = "You empty as many logs as you can carry.";
	private static final String LOG_CUT_KANDARIN_HEADGEAR_MESSAGE = "Your Kandarin headgear provides you with an additional log.";
	private static final String LOG_CUT_NATURE_OFFERINGS_MESSAGE = "The nature offerings enabled you to chop an extra log.";
	private static final String LOG_CUT_MESSAGE_PREFIX = "You get some ";

	private static final Pattern CHECK_ENTRY_PATTERN = Pattern.compile("(\\d+)\\s*[×x]\\s*([^,<\\n]+)", Pattern.CASE_INSENSITIVE);

	private static final Map<String, Integer> LOG_NAME_TO_ID = ImmutableMap.<String, Integer>builder()
		.put("logs", ItemID.LOGS)
		.put("achey tree logs", ItemID.ACHEY_TREE_LOGS)
		.put("oak logs", ItemID.OAK_LOGS)
		.put("willow logs", ItemID.WILLOW_LOGS)
		.put("teak logs", ItemID.TEAK_LOGS)
		.put("maple logs", ItemID.MAPLE_LOGS)
		.put("mahogany logs", ItemID.MAHOGANY_LOGS)
		.put("arctic pine logs", ItemID.ARCTIC_PINE_LOG)
		.put("yew logs", ItemID.YEW_LOGS)
		.put("magic logs", ItemID.MAGIC_LOGS)
		.put("redwood logs", ItemID.REDWOOD_LOGS)
        .put("rosewood logs", ItemID.ROSEWOOD_LOGS)
        .build();

	public static final Collection<Integer> LOG_IDS = ImmutableList.of(
		ItemID.LOGS,
		ItemID.ACHEY_TREE_LOGS,
		ItemID.OAK_LOGS,
		ItemID.WILLOW_LOGS,
		ItemID.TEAK_LOGS,
		ItemID.MAPLE_LOGS,
		ItemID.MAHOGANY_LOGS,
		ItemID.ARCTIC_PINE_LOG,
		ItemID.YEW_LOGS,
		ItemID.MAGIC_LOGS,
		ItemID.REDWOOD_LOGS,
        ItemID.ROSEWOOD_LOGS
	);

	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private OverlayManager overlayManager;
	@Inject private LogBasketOverlay logBasketOverlay;

	private final Multiset<Integer> inventoryItems = HashMultiset.create();
	private final Multiset<Integer> equipmentItems = HashMultiset.create();
	private final Map<LogBasketAction, Integer> basketActions = new HashMap<>();
	private final AtomicInteger newLogsInInventory = new AtomicInteger();
	private final AtomicInteger burnedLogsFromInfernalAxe = new AtomicInteger();

	private boolean pendingPartialEmpty = false;

	@Provides
	public LogBasketConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LogBasketConfig.class);
	}

	@Override
	protected void startUp()
	{
		LogBasket.STATE.setStoredLogs(0);
		LogBasket.STATE.setUnknown(true);

		overlayManager.add(logBasketOverlay);

		clientThread.invokeLater(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				refreshContainerSnapshots();
			}
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(logBasketOverlay);
		inventoryItems.clear();
		equipmentItems.clear();
		basketActions.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(this::refreshContainerSnapshots);
		}
	}

	private void refreshContainerSnapshots()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (inventory != null)
		{
			updateInventory(inventory, inventoryItems);
		}
		if (equipment != null)
		{
			updateInventory(equipment, equipmentItems);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		final String message = event.getMessage();

		if (event.getType() == ChatMessageType.GAMEMESSAGE && hasAnyOfItem(LogBasket.BASKET_IDS))
		{
			switch (message)
			{
				case EMPTIED_TO_BANK_MESSAGE:
				case EMPTIED_ALL_TO_INVENTORY_MESSAGE:
				case EMPTY_BASKET_MESSAGE:
				case CHECKED_EMPTY_BASKET_MESSAGE:
					LogBasket.STATE.setStoredLogs(LogBasket.EMPTY);
					LogBasket.STATE.setUnknown(false);
					return;

				case FULL_BASKET_MESSAGE:
					LogBasket.STATE.setStoredLogs(LogBasket.FULL);
					LogBasket.STATE.setUnknown(false);
					return;

				case EMPTIED_PARTIAL_TO_INVENTORY_MESSAGE:
					if (!LogBasket.STATE.isUnknown())
					{
						pendingPartialEmpty = true;
					}
					return;

				default:
					break;
			}
		}

		if (event.getType() == ChatMessageType.SPAM && hasAnyOfItem(LogBasket.OPEN_BASKET_IDS))
		{
			if (message.startsWith(LOG_CUT_MESSAGE_PREFIX))
			{
				newLogsInInventory.incrementAndGet();
			}
			if (message.equals(LOG_CUT_KANDARIN_HEADGEAR_MESSAGE) || message.equals(LOG_CUT_NATURE_OFFERINGS_MESSAGE))
			{
				newLogsInInventory.incrementAndGet();
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		final int containerId = event.getContainerId();

		if (containerId == InventoryID.INVENTORY.getId())
		{
			if (!hasAnyOfItem(LogBasket.BASKET_IDS))
			{
				updateInventory(event.getItemContainer(), inventoryItems);
				return;
			}

			final Multiset<Integer> previousInventory = HashMultiset.create(inventoryItems);
			updateInventory(event.getItemContainer(), inventoryItems);
			final Multiset<Integer> removedItems = Multisets.difference(previousInventory, inventoryItems);

			if (pendingPartialEmpty)
			{
				pendingPartialEmpty = false;
				int previousLogCount = (int) previousInventory.stream().filter(LOG_IDS::contains).count();
				int newLogCount = (int) inventoryItems.stream().filter(LOG_IDS::contains).count();
				int logsTransferred = newLogCount - previousLogCount;
				int remaining = Math.max(LogBasket.EMPTY, LogBasket.STATE.getStoredLogs() - logsTransferred);
				LogBasket.STATE.setStoredLogs(remaining);
				LogBasket.STATE.setUnknown(false);
			}
			else if (removedItems.size() > 0 && consumeRecentAction(LogBasketAction.FILL))
			{
				int addedLogs = (int) removedItems.stream().filter(LOG_IDS::contains).count();
				if (addedLogs > 0)
				{
					int newCount = Math.min(LogBasket.FULL, LogBasket.STATE.getStoredLogs() + addedLogs);
					LogBasket.STATE.setStoredLogs(newCount);
					LogBasket.STATE.setUnknown(false);
				}
			}
		}
		else if (containerId == InventoryID.EQUIPMENT.getId())
		{
			updateInventory(event.getItemContainer(), equipmentItems);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		int itemId = -1;

		switch (event.getMenuAction())
		{
			case CC_OP:
			case CC_OP_LOW_PRIORITY:
				Widget widget = event.getWidget();
				if (widget != null)
				{
					itemId = widget.getItemId();
				}
				break;
			case WIDGET_TARGET_ON_WIDGET:
				Widget targetWidget = event.getWidget();
				if (targetWidget != null && LogBasket.BASKET_IDS.contains(targetWidget.getItemId()))
				{
					basketActions.put(LogBasketAction.FILL, client.getTickCount());
				}
				return;
			default:
				return;
		}

		final LogBasketAction action = LogBasketAction.forMenuOption(event.getMenuOption());
		if (action == null)
		{
			return;
		}

		boolean clickedOnBasket = LogBasket.BASKET_IDS.contains(itemId);
		boolean basketEquipped = equipmentItems.stream().anyMatch(LogBasket.BASKET_IDS::contains);

		if (clickedOnBasket || basketEquipped)
		{
			basketActions.put(action, client.getTickCount());
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == CHECK_WIDGET_INTERFACE && consumeRecentAction(LogBasketAction.CHECK))
		{
			clientThread.invokeLater(() ->
			{
				final Widget widget = client.getWidget(CHECK_WIDGET_INTERFACE, CHECK_WIDGET_COMPONENT);
				parseCheckWidget(widget);
			});
		}
	}

	private void parseCheckWidget(Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		final String text = widget.getText();
		if (text == null)
		{
			return;
		}

		if (text.contains("basket is empty"))
		{
			LogBasket.STATE.setStoredLogs(LogBasket.EMPTY);
			LogBasket.STATE.setUnknown(false);
			return;
		}

		if (!text.contains("The basket contains"))
		{
			return;
		}

		final String plainText = text.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");
		final Matcher matcher = CHECK_ENTRY_PATTERN.matcher(plainText);

		int total = 0;
		boolean matched = false;

		while (matcher.find())
		{
			final int count = Integer.parseInt(matcher.group(1));
			final String logName = matcher.group(2).trim().toLowerCase();

			if (LOG_NAME_TO_ID.containsKey(logName))
			{
				total += count;
				matched = true;
			}
		}

		if (matched)
		{
			LogBasket.STATE.setStoredLogs(total);
			LogBasket.STATE.setUnknown(false);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.FIREMAKING)
		{
			return;
		}
		if (!hasAnyOfItem(LogBasket.BASKET_IDS) || !hasAnyOfItem(InfernalAxe.CHARGED_AXE_IDS))
		{
			return;
		}

		burnedLogsFromInfernalAxe.incrementAndGet();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		final int delta = newLogsInInventory.getAndSet(0);
		final int burned = burnedLogsFromInfernalAxe.getAndSet(0);

		if (!hasAnyOfItem(LogBasket.BASKET_IDS))
		{
			return;
		}

		if (delta > 0)
		{
			final int newCount = Math.min(LogBasket.FULL, LogBasket.STATE.getStoredLogs() + delta);
			LogBasket.STATE.setStoredLogs(newCount);
			LogBasket.STATE.setUnknown(false);
		}

		if (burned > 0)
		{
			final int reducedCount = Math.max(LogBasket.EMPTY, LogBasket.STATE.getStoredLogs() - burned);
			LogBasket.STATE.setStoredLogs(reducedCount);
			LogBasket.STATE.setUnknown(false);
		}
	}

	private void updateInventory(ItemContainer container, Collection<Integer> target)
	{
		target.clear();
		Arrays.stream(container.getItems())
			.map(Item::getId)
			.forEach(target::add);
	}

	private boolean hasAnyOfItem(Collection<Integer> itemIds)
	{
		for (final int itemId : itemIds)
		{
			if (inventoryItems.contains(itemId) || equipmentItems.contains(itemId))
			{
				return true;
			}
		}
		return false;
	}

	private boolean consumeRecentAction(LogBasketAction action)
	{
		if (basketActions.containsKey(action) && basketActions.get(action) > client.getTickCount() - GAME_TICK_MARGIN)
		{
			basketActions.remove(action);
			return true;
		}
		return false;
	}
}
