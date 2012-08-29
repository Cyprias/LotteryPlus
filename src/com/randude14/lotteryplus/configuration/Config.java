package com.randude14.lotteryplus.configuration;

import com.randude14.lotteryplus.Plugin;

public class Config {
	private static final Plugin plugin = Plugin.getInstance();
	
	//PROPERTIES
	public static final Property<Long> SAVE_DELAY = new Property<Long>("properties.save-delay", 15L);
	public static final Property<Long> UPDATE_DELAY = new Property<Long>("properties.update-delay", 30L);
	public static final Property<Long> REMINDER_MESSAGE_TIME = new Property<Long>("properties.reminder-message-delay", 10L);
	public static final Property<String> MAIN_LOTTERIES = new Property<String>("properties.main-lotteries", "");
	public static final Property<Boolean> SHOULD_DROP = new Property<Boolean>("properties.should-drop", true);
	public static final Property<Boolean> BUY_ENABLE = new Property<Boolean>("properties.buy-enable", true);
	public static final Property<Boolean> OPEN_INVENTORY = new Property<Boolean>("properties.open-inv", false);
	public static final Property<Boolean> SHOULD_LOG = new Property<Boolean>("properties.should-log", false);
	public static final Property<String> REMINDER_MESSAGE = new Property<String>("properties.reminder-message", "&e[Lottery+] - Dont forget to check your servers lotteries. Type /lottery list to list lotteries.");
	public static final Property<String> SIGN_MESSAGE = new Property<String>("properties.sign-message", "&eName: <name><newline>&eTicket Cost: <ticketcost>");
	public static final Property<String> BUY_MESSAGE = new Property<String>("properties.buy-message", "&e<player> has bought <tickets> tickets for <lottery>");
	public static final Property<String> MAIN_LOTTERIES_MESSAGE = new Property<String>("properties.main-lotteries-message", "&e[Lottery+] - <name>: Time Left: <time>, Reward: <reward>");
	public static final Property<String> MONEY_FORMAT = new Property<String>("properties.money-format", "$<money>");
	public static final Property<String> LINE_SEPARATOR = new Property<String>("properties.line-separator", "<newline>");
	public static final Property<String> CHAT_PREFIX = new Property<String>("properties.chat-prefix", "&e[LotteryPlus] - ");
	public static final Property<String> SIGN_TAG = new Property<String>("properties.sign-tag", "&a[Lottery+]");
	
	//LOTTERY DEFAULTS
	public static final Property<String> DEFAULT_SEED = new Property<String>("defaults.seed", "LotteryPlus");
	public static final Property<String> DEFAULT_ITEM_REWARDS = new Property<String>("defaults.item-rewards", "");
	public static final Property<String> DEFAULT_RESET_ADD_ITEM_REWARDS = new Property<String>("defaults.item-rewards", "");
	public static final Property<Boolean> DEFAULT_ITEM_ONLY = new Property<Boolean>("defaults.item-only", false);
	public static final Property<Boolean> DEFAULT_REPEAT = new Property<Boolean>("defaults.repeat", true);
	public static final Property<Double> DEFAULT_POT = new Property<Double>("defaults.pot", 1000.0);
	public static final Property<Double> DEFAULT_TICKET_COST = new Property<Double>("defaults.ticket-cost", 10.0);
	public static final Property<Double> DEFAULT_TICKET_TAX = new Property<Double>("defaults.ticket-tax", 0.0);
	public static final Property<Double> DEFAULT_POT_TAX = new Property<Double>("defaults.pot-tax", 0.0);
	public static final Property<Double> DEFAULT_RESET_ADD_TICKET_COST = new Property<Double>("defaults.reset-add-ticket-cost", 0.0);
	public static final Property<Double> DEFAULT_RESET_ADD_POT = new Property<Double>("defaults.reset-add-pot", 0.0);
	public static final Property<Double> DEFAULT_RESET_ADD_TICKET_TAX = new Property<Double>("defaults.reset-add-ticket-tax", 0.0);
	public static final Property<Double> DEFAULT_RESET_ADD_POT_TAX = new Property<Double>("defaults.reset-add-pot-tax", 0.0);
	public static final Property<Integer> DEFAULT_MAX_TICKETS = new Property<Integer>("defaults.max-tickets", -1);
	public static final Property<Integer> DEFAULT_MIN_PLAYERS = new Property<Integer>("defaults.min-players", 2);
	public static final Property<Integer> DEFAULT_MAX_PLAYERS = new Property<Integer>("defaults.max-players", 10);
	public static final Property<Integer> DEFAULT_RESET_ADD_MAX_TICKETS = new Property<Integer>("defaults.reset-add-max-tickets", 0);
	public static final Property<Integer> DEFAULT_RESET_ADD_MIN_PLAYERS = new Property<Integer>("defaults.reset-add-min-players", 0);
	public static final Property<Integer> DEFAULT_RESET_ADD_MAX_PLAYERS = new Property<Integer>("defaults.reset-add-max-players", 0);
	public static final Property<Long> DEFAULT_COOLDOWN = new Property<Long>("defaults.cooldown", 0L);
	public static final Property<Long> DEFAULT_RESET_ADD_COOLDOWN = new Property<Long>("defaults.reset-add-cooldown", 0L);
	public static final Property<Long> DEFAULT_TIME = new Property<Long>("defaults.time", 72L);
	public static final Property<Long> DEFAULT_RESET_ADD_TIME = new Property<Long>("defaults.reset-add-time", 0L);
	
	public static long getLong(Property<Long> property) {
		return plugin.getConfig().getLong(property.getPath(), property.getDefaultValue());
	}
	
	public static int getInt(Property<Integer> property) {
		return plugin.getConfig().getInt(property.getPath(), property.getDefaultValue());
	}
	
	public static double getDouble(Property<Double> property) {
		return plugin.getConfig().getDouble(property.getPath(), property.getDefaultValue());
	}
	
	public static boolean getBoolean(Property<Boolean> property) {
		return plugin.getConfig().getBoolean(property.getPath(), property.getDefaultValue());
	}
	
	public static String getString(Property<String> property) {
		return plugin.getConfig().getString(property.getPath(), property.getDefaultValue());
	}
}
