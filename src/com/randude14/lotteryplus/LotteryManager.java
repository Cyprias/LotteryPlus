package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.lottery.LotteryOptions;
import com.randude14.lotteryplus.util.CustomYaml;
import com.randude14.lotteryplus.util.TimeConstants;

public class LotteryManager implements TimeConstants, Runnable {
	private static final CustomYaml lotteriesConfig = new CustomYaml("lotteries.yml");
	private static final Map<String, Lottery> lotteries = new HashMap<String, Lottery>();
	
	public static boolean loadLottery(CommandSender sender, String find) {
		if(lotteries.containsKey(find.toLowerCase())) {
			return false;
		}
		lotteriesConfig.reloadConfig();
		ConfigurationSection section = getOrCreateLotteriesSection();
		for(String sectionName : section.getKeys(false)) {
			if(sectionName.equalsIgnoreCase(find)) {
				ConfigurationSection lotteriesSection = section.getConfigurationSection(sectionName);
				Lottery lottery = new Lottery(sectionName);
				Map<String, Object> values = lotteriesSection.getValues(true);
				try {
					lottery.setOptions(new LotteryOptions(values));
				} catch (Exception ex) {
					Logger.warning("Exception caught while trying to load '%s'.", sectionName);
					Logger.warning("You can try to load this later using '/lottery load <lottery name>'");
					continue;
				}
				lotteries.put(sectionName.toLowerCase(), lottery);
				return true;
			}
		}
		return false;
	}
	
	public static boolean unloadLottery(String find) {
		ConfigurationSection section = getOrCreateLotteriesSection();
		for(String sectionName : section.getKeys(false)) {
			if(sectionName.equalsIgnoreCase(find)) {
				section.set(sectionName, null);
				ConfigurationSection savesSection = lotteriesConfig.getConfig().getConfigurationSection("saves");
				if(savesSection.contains(sectionName)) {
					savesSection.set(sectionName, null);
				}
				return true;
			}
		}
		return false;
	}
	
	public static List<Lottery> getLotteries() {
		return new ArrayList<Lottery>(lotteries.values());
	}
	
	public static Lottery getLottery(String lotteryName) {
		return lotteries.get(lotteryName.toLowerCase());
	}
	
	public static void loadLotteries() {
		lotteriesConfig.reloadConfig();
		ConfigurationSection section = getOrCreateLotteriesSection();
		ConfigurationSection savesSection = lotteriesConfig.getConfig().getConfigurationSection("saves");
		for(String lotteryName : section.getKeys(false)) {
			if(lotteries.containsKey(lotteryName.toLowerCase()))
				continue;
			ConfigurationSection lotteriesSection;
			if(savesSection != null && savesSection.contains(lotteryName)) {
				lotteriesSection = savesSection.getConfigurationSection("saves");
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
			lotteries.put(lotteryName.toLowerCase(), lottery);
		}
	}
	
	public static void saveLotteries() {
		ConfigurationSection savesSection = lotteriesConfig.getConfig().createSection("saves");
		for(Lottery lottery : lotteries.values()) {
			lottery.save();
			savesSection.createSection(lottery.getName(), lottery.getOptions().options());
		}
		lotteriesConfig.saveConfig();
	}
	
	private static ConfigurationSection getOrCreateLotteriesSection() {
		FileConfiguration config = lotteriesConfig.getConfig();
		ConfigurationSection lotteriesSection = config.getConfigurationSection("lotteries");
		return (lotteriesSection != null) ? lotteriesSection : config.createSection("lotteries");
	}
	
	public void run() {
		
	}
	
	static class TimerTask implements Runnable {

		public void run() {
			
		}
	}
}
