package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.configuration.CustomYaml;
import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.lottery.LotteryOptions;

public class LotteryManager {
	private static final CustomYaml lotteriesConfig = new CustomYaml("lotteries.yml");
	private static final Map<String, Lottery> lotteries = new TreeMap<String, Lottery>(String.CASE_INSENSITIVE_ORDER);

	public static boolean createLotterySection(CommandSender sender,
			String lotteryName) {
		if (lotteries.containsKey(lotteryName.toLowerCase())) {
			ChatUtils.error(sender, "%s already exists.", lotteryName);
			return false;
		}
		ConfigurationSection lotteriesSection = getOrCreateLotteriesSection();
		for (String key : lotteriesSection.getKeys(false)) {
			if (key.equalsIgnoreCase(lotteryName)) {
				ChatUtils.error(sender, "Section already exists for %s.",
						lotteryName);
				return false;
			}
		}
		ConfigurationSection section = lotteriesSection.createSection(lotteryName);
		writeDefaults(section);
		lotteriesConfig.saveConfig();
		ChatUtils.send(sender, ChatColor.YELLOW, "Section created for %s%s, %syou can then " + 
		                                         "load this lottery using '/lottery load <lottery name>'" + 
				                                 " when you are done setting the values.", 
				                                 ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		return true;
	}

	public static boolean loadLottery(CommandSender sender, String find) {
		Lottery l = lotteries.get(find.toLowerCase());
		if (l != null) {
			ChatUtils.error(sender, "%s already exists.", l.getName());
			return false;
		}
		lotteriesConfig.reloadConfig();
		ConfigurationSection section = getOrCreateLotteriesSection();
		for (String sectionName : section.getKeys(false)) {
			if (sectionName.equalsIgnoreCase(find)) {
				ConfigurationSection lotteriesSection = section
						.getConfigurationSection(sectionName);
				Lottery lottery = new Lottery(sectionName);
				Map<String, Object> values = lotteriesSection.getValues(true);
				try {
					lottery.setOptions(new LotteryOptions(values));
				} catch (Exception ex) {
					Logger.warning(
							"Exception caught while trying to load '%s'.",
							sectionName);
					Logger.warning("You can try to load this later using '/lottery load <lottery name>'");
					ex.printStackTrace();
					continue;
				}
				ChatUtils.send(sender, ChatColor.GOLD, "%s%s has been loaded and created.", ChatColor.YELLOW, lottery.getName());
				lotteries.put(sectionName.toLowerCase(), lottery);
				return true;
			}
		}
		ChatUtils.error(sender, "%s was not found.", find);
		return false;
	}

	public static boolean unloadLottery(String find) {
		return unloadLottery(Bukkit.getConsoleSender(), find, false);
	}
	
	public static boolean unloadLottery(CommandSender sender, String find) {
		return unloadLottery(sender, find, false);
	}

	public static boolean unloadLottery(CommandSender sender, String find, boolean flag) {
		if (!lotteries.containsKey(find.toLowerCase())) {
			ChatUtils.error(sender, "%s does not exist.", find);
		}
		Lottery lottery = lotteries.remove(find.toLowerCase());
		if(flag) {
			ConfigurationSection section = getOrCreateLotteriesSection();
			ConfigurationSection savesSection = lotteriesConfig.getConfig()
					.getConfigurationSection("saves");
			deleteSection(section, find);
			if (savesSection != null) {
				deleteSection(savesSection, find);
			}
			ChatUtils.send(sender, ChatColor.YELLOW, "%s has been unloaded and removed.", lottery.getName());
			return true;
		}
		ChatUtils.send(sender, ChatColor.YELLOW, "%s has been unloaded.", lottery.getName());
		return false;
	}
	
	private static void deleteSection(ConfigurationSection section, String find) {
		for (String key : section.getKeys(false)) {
			if (key.equalsIgnoreCase(find)) {
				section.set(key, null);
			}
		}
	}

	public static List<Lottery> getLotteries() {
		return new ArrayList<Lottery>(lotteries.values());
	}

	public static Lottery getLottery(String lotteryName) {
		return lotteries.get(lotteryName.toLowerCase());
	}

	public static boolean reloadLottery(String lotteryName) {
		return reloadLottery(Bukkit.getConsoleSender(), lotteryName, true);
	}

	public static boolean reloadLottery(CommandSender sender, String lotteryName, boolean force) {
		Lottery lottery = lotteries.get(lotteryName.toLowerCase());
		if (lottery == null) {
			ChatUtils.error(sender, "%s does not exist.", lotteryName);
			return false;
		}
		if(lottery.isDrawing()) {
			lottery.cancelDrawing();
			ChatUtils.broadcast(ChatColor.RED + "Cancelling drawing due to being reloaded.");
		}
		lotteriesConfig.reloadConfig();
		ConfigurationSection section = getOrCreateLotteriesSection();
		for (String sectionName : section.getKeys(false)) {
			if (sectionName.equalsIgnoreCase(lotteryName)) {
				ConfigurationSection lotteriesSection = section
						.getConfigurationSection(sectionName);
				Map<String, Object> values = lotteriesSection.getValues(true);
				try {
					lottery.setOptions(new LotteryOptions(values), force);
				} catch (Exception ex) {
					ChatUtils.error(sender,
							"Exception caught while trying to reload '%s'.",
							lotteryName);
					ChatUtils
							.error(sender,
									"You can try to load this later using '/lottery load <lottery name>'");
					ex.printStackTrace();
					lotteries.remove(lotteryName.toLowerCase());
					return false;
				}
				ChatUtils.send(sender, ChatColor.GOLD,
						"%s %shas been successfully reloaded.",
						lottery.getName(), ChatColor.YELLOW);
			}
		}
		return true;
	}
	
	public static void reloadLotteries(CommandSender sender) {
		for(Lottery lottery : lotteries.values()) {
			reloadLottery(sender, lottery.getName(), true);
		}
	}
	
	public static int loadLotteries() {
		return loadLotteries(true);
	}

	public static int loadLotteries(boolean clear) {
		if(clear) {
			lotteries.clear();
		}
		if(!lotteriesConfig.exists()) {
			lotteriesConfig.saveDefaultConfig();
		}
		lotteriesConfig.reloadConfig();
		ConfigurationSection section = getOrCreateLotteriesSection();
		ConfigurationSection savesSection = lotteriesConfig.getConfig()
				.getConfigurationSection("saves");
		int numLotteries = 0;
		for (String lotteryName : section.getKeys(false)) {
			if (lotteries.containsKey(lotteryName.toLowerCase()))
				continue;
			ConfigurationSection lotteriesSection;
			if (savesSection != null && savesSection.contains(lotteryName)) {
				lotteriesSection = savesSection
						.getConfigurationSection(lotteryName);
			} else {
				lotteriesSection = section.getConfigurationSection(lotteryName);
			}
			Lottery lottery = new Lottery(lotteryName);
			Map<String, Object> values = lotteriesSection.getValues(true);
			try {
				lottery.setOptions(new LotteryOptions(values));
			} catch (Exception ex) {
				Logger.warning("Exception caught while trying to load '%s'.", lotteryName);
				Logger.warning("You can try to load this later using '/lottery load <lottery name>'");
				ex.printStackTrace();
				continue;
			}
			numLotteries++;
			lotteries.put(lotteryName.toLowerCase(), lottery);
		}
		return numLotteries;
	}

	public static void saveLotteries() {
		lotteriesConfig.reloadConfig();
		ConfigurationSection savesSection = lotteriesConfig.getConfig().createSection("saves");
		for (Lottery lottery : lotteries.values()) {
			lottery.save();
			savesSection.createSection(lottery.getName(), lottery.getOptions().getValues());
		}
		lotteriesConfig.saveConfig();
	}
	
	public static void listLotteries(CommandSender sender, int page) {
		List<Lottery> list = getLotteries();
		int len = list.size();
		int max = (len / 10) + 1;
		if (len % 10 == 0)
			max--;
		if (page > max)
			page = max;
		if (page < 1)
			page = 1;
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "--------[%sLotteries, Page (%d/%d)%s]--------", ChatColor.GOLD, page, max, ChatColor.YELLOW);
		if(Plugin.hasPermission(sender, Perm.INFO)) {
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "For Info: -> '/lottery info <lottery name>'");
		}
		for (int cntr = (page * 10) - 10, stop = cntr + 10; cntr < stop && cntr < len; cntr++) {
			ChatUtils.sendRaw(sender, ChatColor.GOLD, (cntr+1) + ". " + list.get(cntr));
		}
	}

	private static ConfigurationSection getOrCreateLotteriesSection() {
		FileConfiguration config = lotteriesConfig.getConfig();
		ConfigurationSection lotteriesSection = config.getConfigurationSection("lotteries");
		return (lotteriesSection != null) ? lotteriesSection : config.createSection("lotteries");
	}

	static class TimerTask implements Runnable {

		public void run() {
			Lottery prevLottery = null;
			for (Lottery lottery : getLotteries()) {
				if (prevLottery != null && prevLottery.isDrawing()) {
					while(prevLottery.isDrawing()) {
						Utils.sleep(10L);
					}
				}
				lottery.onTick();
				prevLottery = lottery;
			}
		}
	}
	
	private static void writeDefaults(ConfigurationSection section) {
		section.set(Config.DEFAULT_TICKET_COST.getName(), Config.getDouble(Config.DEFAULT_TICKET_COST));
		section.set(Config.DEFAULT_POT.getName(), Config.getDouble(Config.DEFAULT_POT));
		section.set(Config.DEFAULT_TIME.getName(), Config.getDouble(Config.DEFAULT_TIME));
		section.set(Config.DEFAULT_MAX_TICKETS.getName(), Config.getInt(Config.DEFAULT_MAX_TICKETS));
		section.set(Config.DEFAULT_MIN_PLAYERS.getName(), Config.getInt(Config.DEFAULT_MIN_PLAYERS));
		section.set(Config.DEFAULT_MAX_PLAYERS.getName(), Config.getInt(Config.DEFAULT_MAX_PLAYERS));
		section.set(Config.DEFAULT_TICKET_TAX.getName(), Config.getDouble(Config.DEFAULT_TICKET_TAX));
		section.set(Config.DEFAULT_POT_TAX.getName(), Config.getDouble(Config.DEFAULT_POT_TAX));
	}
}
