package com.randude14.lotteryplus;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class LotteryConfig {
	private static final String DEFAULT_REMINDER_MESSAGE = "&eDont forget to check your servers lotteries. Type /lottery list to list lotteries.";
	private static final long DEFAULT_REMINDER_MESSAGE_TIME = 30;
	private static final boolean DEFAULT_PERMISSIONS = false;
	private static final long DEFAULT_TIME = 24;
	private static final int DEFAULT_MAX_PLAYERS = 10;
	private static final int DEFAULT_MIN_PLAYERS = 2;
	private static final double DEFAULT_POT = 100.00;
	private static final double DEFAULT_TICKETCOST = 1.00;
	private static final int DEFAULT_MAXTICKETS = 10;
	private boolean PERMISSIONS;
	private long TIME;
	private long MESSAGE_TIME;
	private int MAX_PLAYERS;
	private int MIN_PLAYERS;
	private double POT;
	private double TICKETCOST;
	private int MAXTICKETS;
	private String REMINDER_MESSAGE;
	private final Plugin plugin;

	public LotteryConfig(final Plugin plugin) {
		this.plugin = plugin;
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
			REMINDER_MESSAGE = replaceColors(REMINDER_MESSAGE);
			ConfigurationSection lotteryProperties = plugin.getConfig()
					.getConfigurationSection("lottery properties");
			TIME = lotteryProperties.getLong("default-time", DEFAULT_TIME);
			MAX_PLAYERS = lotteryProperties.getInt("max-players",
					DEFAULT_MAX_PLAYERS);
			MIN_PLAYERS = lotteryProperties.getInt("min-players",
					DEFAULT_MIN_PLAYERS);
			POT = lotteryProperties.getDouble("default-pot", DEFAULT_POT);
			TICKETCOST = lotteryProperties.getDouble("default-ticketcost",
					DEFAULT_TICKETCOST);
			MAXTICKETS = lotteryProperties.getInt("max-tickets",
					DEFAULT_MAXTICKETS);
			plugin.info("config loaded.");
		} catch (Exception ex) {
			plugin.warning("error occured while loading config.");
			plugin.info("writing default config.");
			writeConfig();
			loadConfig();
		}

	}

	private String replaceColors(String message) {
		message = message.replaceAll("&0", ChatColor.BLACK.toString());
		message = message.replaceAll("&1", ChatColor.DARK_BLUE.toString());
		message = message.replaceAll("&2", ChatColor.DARK_GREEN.toString());
		message = message.replaceAll("&3", ChatColor.DARK_AQUA.toString());
		message = message.replaceAll("&4", ChatColor.DARK_RED.toString());
		message = message.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
		message = message.replaceAll("&6", ChatColor.GOLD.toString());
		message = message.replaceAll("&7", ChatColor.GRAY.toString());
		message = message.replaceAll("&8", ChatColor.DARK_GRAY.toString());
		message = message.replaceAll("&9", ChatColor.BLUE.toString());
		message = message.replaceAll("&a", ChatColor.GREEN.toString());
		message = message.replaceAll("&b", ChatColor.AQUA.toString());
		message = message.replaceAll("&c", ChatColor.RED.toString());
		message = message.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
		message = message.replaceAll("&e", ChatColor.YELLOW.toString());
		message = message.replaceAll("&f", ChatColor.WHITE.toString());
		message = message.replaceAll("&k", ChatColor.MAGIC.toString());
		return message;
	}

	public void writeConfig() {
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("permissions", Boolean.FALSE);
		propertyMap.put("reminder-message-time", DEFAULT_REMINDER_MESSAGE_TIME);
		propertyMap.put("reminder-message", DEFAULT_REMINDER_MESSAGE);
		Map<String, Object> lotteryMap = new HashMap<String, Object>();
		lotteryMap.put("default-time", DEFAULT_TIME);
		lotteryMap.put("default-pot", DEFAULT_POT);
		lotteryMap.put("default-ticketcost", DEFAULT_TICKETCOST);
		lotteryMap.put("max-players", DEFAULT_MAX_PLAYERS);
		lotteryMap.put("min-players", DEFAULT_MIN_PLAYERS);
		FileConfiguration config = plugin.getConfig();
		config.createSection("properties", propertyMap);
		config.createSection("lottery properties", lotteryMap);
		plugin.saveConfig();
		plugin.info("config written.");
		plugin.reloadConfig();
		loadConfig();
	}

	public boolean isPermsEnabled() {
		return PERMISSIONS;
	}

	public long getDefaultTime() {
		return TIME;
	}

	public int getDefaultMaxPlayers() {
		return MAX_PLAYERS;
	}

	public int getDefaultMinPlayers() {
		return MIN_PLAYERS;
	}

	public double getDefaultPot() {
		return POT;
	}

	public double getDefaultTicketCost() {
		return TICKETCOST;
	}

	public int getDefaultMaxTickets() {
		return MAXTICKETS;
	}

	public long getReminderMessageTime() {
		return MESSAGE_TIME;
	}

	public String getReminderMessage() {
		return REMINDER_MESSAGE;
	}

}
