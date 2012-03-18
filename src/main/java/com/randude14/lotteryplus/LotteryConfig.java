package com.randude14.lotteryplus;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.randude14.lotteryplus.util.FormatOptions;

public class LotteryConfig implements FormatOptions {
	private static final String DEFAULT_REMINDER_MESSAGE = "&eDont forget to check your servers lotteries. Type /lottery list to list lotteries.";
	private static final String DEFAULT_BUY_SIGN_MESSAGE = "&eName: <name><newline>&eTicket Cost: <ticketcost>";
	private static final String DEFAULT_SIGN_NORMAL_ONE = "<name>";
	private static final String DEFAULT_SIGN_NORMAL_TWO = "<time>";
	private static final String DEFAULT_SIGN_NORMAL_THREE = "<reward>";
	private static final String DEFAULT_SIGN_DRAWING_ONE = "<name>";
	private static final String DEFAULT_SIGN_DRAWING_TWO = "Drawing...";
	private static final String DEFAULT_SIGN_DRAWING_THREE = "<reward>";
	private static final String DEFAULT_SIGN_END_ONE = "<name>";
	private static final String DEFAULT_SIGN_END_TWO = "Over";
	private static final String DEFAULT_SIGN_END_THREE = "<winner>";
	private static final long DEFAULT_REMINDER_MESSAGE_TIME = 30L;
	private static final long DEFAULT_UPDATE_TIME = 60L;
	private static final long DEFAULT_TIME_AFTER_DRAWS = 5L;
	private static final boolean DEFAULT_PERMISSIONS = false;
	private static final int DEFAULT_MAX_PLAYERS = 10;
	private static final int DEFAULT_MIN_PLAYERS = 2;
	private static final double DEFAULT_POT = 100.00;
	private static final double DEFAULT_TICKETCOST = 1.00;
	private static final long DEFAULT_TIME = 24;
	private static final int DEFAULT_MAXTICKETS = 10;
	private boolean PERMISSIONS;
	private long MESSAGE_TIME;
	private long UPDATE_TIME;
	private long TIME_AFTER_DRAWS;
	private String REMINDER_MESSAGE;
	private String SIGN_BUY_MESSAGE;
	private String[] normalArgs;
	private String[] drawingArgs;
	private String[] endArgs;
	private final Plugin plugin;

	public LotteryConfig(final Plugin plugin) {
		this.plugin = plugin;
		normalArgs = new String[3];
		drawingArgs = new String[3];
		endArgs = new String[3];
	}

	public void loadConfig() {

		try {
			ConfigurationSection properties = plugin.getConfig()
					.getConfigurationSection("properties");
			PERMISSIONS = properties.getBoolean("permissions",
					DEFAULT_PERMISSIONS);
			MESSAGE_TIME = properties.getLong("reminder-message-time",
					DEFAULT_REMINDER_MESSAGE_TIME);
			REMINDER_MESSAGE = properties.getString("reminder-message",
					REMINDER_MESSAGE);
			UPDATE_TIME = properties.getLong("update-delay", DEFAULT_UPDATE_TIME);
			SIGN_BUY_MESSAGE = properties.getString("sign-message", DEFAULT_BUY_SIGN_MESSAGE);
			TIME_AFTER_DRAWS = properties.getLong("time-after-draws", DEFAULT_TIME_AFTER_DRAWS);
			REMINDER_MESSAGE = plugin.replaceColors(REMINDER_MESSAGE);
			ConfigurationSection signSection = plugin.getConfig()
					.getConfigurationSection("signs");
			normalArgs[0] = signSection.getString("normal.line1",
					DEFAULT_SIGN_NORMAL_ONE);
			normalArgs[1] = signSection.getString("normal.line2",
					DEFAULT_SIGN_NORMAL_TWO);
			normalArgs[2] = signSection.getString("normal.line3",
					DEFAULT_SIGN_NORMAL_THREE);
			drawingArgs[0] = signSection.getString("drawing.line1",
					DEFAULT_SIGN_DRAWING_ONE);
			drawingArgs[1] = signSection.getString("drawing.line2",
					DEFAULT_SIGN_DRAWING_TWO);
			drawingArgs[2] = signSection.getString("drawing.line3",
					DEFAULT_SIGN_DRAWING_THREE);
			endArgs[0] = signSection.getString("over.line1", DEFAULT_SIGN_END_ONE);
			endArgs[1] = signSection.getString("over.line2", DEFAULT_SIGN_END_TWO);
			endArgs[2] = signSection.getString("over.line3", DEFAULT_SIGN_END_THREE);
			plugin.info("config loaded.");
		} catch (Exception ex) {
			plugin.warning("error occured while loading config.");
			plugin.info("writing default config.");
			writeConfig();
			loadConfig();
		}

	}

	public void writeConfig() {
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("permissions", Boolean.FALSE);
		propertyMap.put("reminder-message-time", DEFAULT_REMINDER_MESSAGE_TIME);
		propertyMap.put("reminder-message", DEFAULT_REMINDER_MESSAGE);
		propertyMap.put("update-delay", DEFAULT_UPDATE_TIME);
		propertyMap.put("sign-message", DEFAULT_BUY_SIGN_MESSAGE);
		propertyMap.put("time-after-draws", DEFAULT_TIME_AFTER_DRAWS);
		Map<String, Object> signMap = new HashMap<String, Object>();
		signMap.put("normal.line1", DEFAULT_SIGN_NORMAL_ONE);
		signMap.put("normal.line2", DEFAULT_SIGN_NORMAL_TWO);
		signMap.put("normal.line3", DEFAULT_SIGN_NORMAL_THREE);
		signMap.put("drawing.line1", DEFAULT_SIGN_DRAWING_ONE);
		signMap.put("drawing.line2", DEFAULT_SIGN_DRAWING_TWO);
		signMap.put("drawing.line3", DEFAULT_SIGN_DRAWING_THREE);
		signMap.put("over.line1", DEFAULT_SIGN_END_ONE);
		signMap.put("over.line2", DEFAULT_SIGN_END_TWO);
		signMap.put("over.line3", DEFAULT_SIGN_END_THREE);
		
		FileConfiguration config = plugin.getConfig();
		config.createSection("properties", propertyMap);
		config.createSection("signs", signMap);
		plugin.saveConfig();
		plugin.info("config written.");
		plugin.reloadConfig();
		loadConfig();
	}

	public boolean isPermsEnabled() {
		return PERMISSIONS;
	}

	public int getDefaultMaxPlayers() {
		return DEFAULT_MAX_PLAYERS;
	}

	public int getDefaultMinPlayers() {
		return DEFAULT_MIN_PLAYERS;
	}

	public double getDefaultPot() {
		return DEFAULT_POT;
	}

	public double getDefaultTicketCost() {
		return DEFAULT_TICKETCOST;
	}

	public int getDefaultMaxTickets() {
		return DEFAULT_MAXTICKETS;
	}

	public long getDefaultTime() {
		return DEFAULT_TIME;
	}
	
	public String[] getNormalArgs() {
		return normalArgs;
	}
	
	public String[] getDrawingArgs() {
		return drawingArgs;
	}
	
	public String[] getEndArgs() {
		return endArgs;
	}

	public long getReminderMessageTime() {
		return MESSAGE_TIME;
	}

	public String getReminderMessage() {
		return REMINDER_MESSAGE;
	}
	
	public long getUpdateDelay() {
		return UPDATE_TIME;
	}
	
	public long getTimeAfterDraws() {
		return TIME_AFTER_DRAWS;
	}
	
	public String getBuyMessage() {
		return SIGN_BUY_MESSAGE;
	}

}
