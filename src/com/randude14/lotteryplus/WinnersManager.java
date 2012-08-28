package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.util.CustomYaml;

public class WinnersManager {
	private static final CustomYaml winnersConfig = new CustomYaml("winners.yml");
	private static final List<String> winners = new ArrayList<String>();
	
	public static void logWinner(String record) {
		WinnersLogger.log(record);
		winners.add(record);
		updateWinners();
		winnersConfig.getConfig().set("winners", winners);
		winnersConfig.saveConfig();
	}
	
	public static void listWinners(CommandSender sender) {
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "--------[%sRecent Winners%s]--------", ChatColor.GOLD, ChatColor.YELLOW);
		for(int cntr = 0;cntr < winners.size();cntr++) {
			ChatUtils.send(sender, ChatColor.GOLD, (cntr+1) + ". " + winners.get(cntr));
		}
	}
	
	public static void loadWinners() {
		winners.clear();
		List<String> list = winnersConfig.getConfig().getStringList("winners");
		if(list != null && !list.isEmpty()) {
			winners.addAll(list);
		}
		updateWinners();
	}
	
	private static void updateWinners() {
		while(winners.size() > 10) {
			winners.remove(0);
		}
	}
}
