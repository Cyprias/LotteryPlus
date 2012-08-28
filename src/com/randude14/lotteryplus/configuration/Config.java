package com.randude14.lotteryplus.configuration;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.Plugin;

public class Config {
	private static final Plugin plugin = Plugin.getInstance();
	
	//PROPERTIES
	public static final Property<Integer> SAVE_DELAY = new Property<Integer>("properties.save-delay", 15);
	public static final Property<Integer> UPDATE_DELAY = new Property<Integer>("properties.update-delay", 30);
	public static final Property<Integer> REMINDER_MESSAGE_TIME = new Property<Integer>("properties.reminder-message-delay", 30);
	public static final Property<String> MAIN_LOTTERIES = new Property<String>("properties.main-lotteries", "");
	public static final Property<Boolean> REMINDER_ENABLE = new Property<Boolean>("properties.reminder-enable", true);
	public static final Property<Boolean> SHOULD_DROP = new Property<Boolean>("properties.should-drop", true);
	public static final Property<Boolean> BUY_ENABLE = new Property<Boolean>("properties.buy-enable", true);
	public static final Property<String> REMINDER_MESSAGE = new Property<String>("properties.reminder-message", "&eDont forget to check your servers lotteries. Type /lottery list to list lotteries.");
	public static final Property<String> SIGN_MESSAGE = new Property<String>("properties.sign-message", "&eName: <name><newline>&eTicket Cost: <ticketcost>");
	public static final Property<String> BUY_MESSAGE = new Property<String>("properties.buy-message", "&e<player> has bought <tickets> tickets for <lottery>");
	public static final Property<String> MONEY_FORMAT = new Property<String>("properties.money-format", "$<money>");
	public static final Property<String> NORMAL_LINE_ONE = new Property<String>("signs.normal.line1", "<name>");
	public static final Property<String> NORMAL_LINE_TWO = new Property<String>("signs.normal.line2", "<time>");
	public static final Property<String> NORMAL_LINE_THREE = new Property<String>("signs.normal.line3", "<reward>");
	public static final Property<String> DRAWING_LINE_ONE = new Property<String>("signs.drawing.line1", "<reward>");
	public static final Property<String> DRAWING_LINE_TWO = new Property<String>("signs.drawing.line2", "Drawing...");
	public static final Property<String> DRAWING_LINE_THREE = new Property<String>("signs.drawing.line3", "<name>");
	public static final Property<String> OVER_LINE_ONE = new Property<String>("signs.over.line1", "<name>");
	public static final Property<String> OVER_LINE_TWO = new Property<String>("signs.over.line2", "Over");
	public static final Property<String> OVER_LINE_THREE = new Property<String>("signs.over.line3", "<winner>");
	public static final Property<String> LINE_SEPARATOR = new Property<String>("properties.line-separator", "<newline>");
	
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
	public static final Property<Integer> DEFAULT_MAX_TICKETS = new Property<Integer>("defaults.max-tickets", 0);
	public static final Property<Integer> DEFAULT_MIN_PLAYERS = new Property<Integer>("defaults.min-players", 0);
	public static final Property<Integer> DEFAULT_MAX_PLAYERS = new Property<Integer>("defaults.max-players", 0);
	public static final Property<Integer> DEFAULT_RESET_ADD_MAX_TICKETS = new Property<Integer>("defaults.reset-add-max-tickets", 0);
	public static final Property<Integer> DEFAULT_RESET_ADD_MIN_PLAYERS = new Property<Integer>("defaults.reset-add-min-players", 0);
	public static final Property<Integer> DEFAULT_RESET_ADD_MAX_PLAYERS = new Property<Integer>("defaults.reset-add-max-players", 0);
	public static final Property<Integer> DEFAULT_COOLDOWN = new Property<Integer>("defaults.cooldown", 0);
	public static final Property<Long> DEFAULT_RESET_ADD_COOLDOWN = new Property<Long>("defaults.reset-add-cooldown", 0L);
	public static final Property<Long> DEFAULT_TIME = new Property<Long>("defaults.time", 72L);
	public static final Property<Long> DEFAULT_RESET_ADD_TIME = new Property<Long>("defaults.reset-add-time", 0L);
	
	public static <T> T getProperty(Property<T> property) {
		Object value = plugin.getConfig().get(property.getPath());
		
		if(value != null) {
			try {
				Class<T> valueClass = property.getValueClass();
				if(valueClass == Long.class) {
					value = (Long)((Number) value).longValue();
				}
				return valueClass.cast(value);
			} catch (Exception ex) {
				Logger.info("Illegal value at path " + property.getPath());
			}			
		}	
		return property.getDefaultValue();
	}
}
