package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import com.randude14.lotteryplus.lottery.ItemReward;
import com.randude14.lotteryplus.lottery.LotteryClaim;
import com.randude14.lotteryplus.lottery.PotReward;
import com.randude14.lotteryplus.lottery.Reward;
import com.randude14.lotteryplus.util.CustomYaml;

public class ClaimManager {
	
	public static void loadClaims() {
		FileConfiguration config = claimsConfig.getConfig();
		ConfigurationSection section = config.getConfigurationSection("claims");
		if(section == null)
			section = config.createSection("claims");
		for(String player : section.getKeys(false)) {
			List<LotteryClaim> playerClaims = new ArrayList<LotteryClaim>();
			ConfigurationSection playerSection = section.getConfigurationSection(player);
			if(playerSection == null)
				continue;
			for(String claimPath : playerSection.getKeys(false)) {
				try {
					playerClaims.add((LotteryClaim) playerSection.get(claimPath));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			claims.put(player, playerClaims);
		}
	}

	public static void addClaim(String name, String lottery, List<Reward> rewards) {
		if (!claims.containsKey(name))
			claims.put(name, new ArrayList<LotteryClaim>());
		LotteryClaim claim = new LotteryClaim(lottery, rewards);
		claims.get(name).add(claim);
		FileConfiguration config = claimsConfig.getConfig();
		ConfigurationSection playerSection = config.getConfigurationSection("claims." + name);
		if(playerSection == null)
			playerSection = config.createSection("claims." + name);
		int cntr = 0;
		while(playerSection.get("claim" + ++cntr) != null) {
		}
		playerSection.set("claim" + cntr, claim);
		claimsConfig.saveConfig();
	}
	
	public static void rewardClaims(Player player) {
		List<LotteryClaim> playerClaims = claims.get(player.getName());
		int num = (playerClaims != null) ? playerClaims.size() : 0;
		player.sendMessage(num + " claims found.");
	}

	private static final Map<String, List<LotteryClaim>> claims = new HashMap<String, List<LotteryClaim>>();
	private static final CustomYaml claimsConfig;
	
	static {
		ConfigurationSerialization.registerClass(LotteryClaim.class);
		ConfigurationSerialization.registerClass(ItemReward.class);
		ConfigurationSerialization.registerClass(PotReward.class);
		claimsConfig = new CustomYaml("claims.yml");
	}

	public static void notifyOfClaims(Player player) {
		List<LotteryClaim> playerClaims = claims.get(player.getName());
		if(playerClaims != null && !playerClaims.isEmpty()) {
			ChatUtils.send(player, ChatColor.YELLOW, "You have recently won some lotteries! Type '/lottery claim' to claim your rewards!");
		}
	} 
}
