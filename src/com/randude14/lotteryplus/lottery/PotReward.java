package com.randude14.lotteryplus.lottery;

import java.util.Map;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.Utils;

@SerializableAs("PotReward")
public class PotReward implements Reward {
	private final double pot;
	
	public PotReward(final double pot) {
		this.pot = pot;
	}

	public void rewardPlayer(Player player) {
		Economy econ = Plugin.getEconomy();
		econ.depositPlayer(player.getName(), pot);
		ChatUtils.send(player, ChatColor.YELLOW, "You have been rewarded %s.", Utils.format(pot));
	}
	
	public String getInfo() {
		return String.format("Pot Reward: %s", Utils.format(pot));
	}
	
	public String toString() {
		return String.format("[%s]", Utils.format(pot));
	}
	
	public static PotReward deserialize(Map<String, Object> map) {
		double pot = (Double) map.get("pot");
		return new PotReward(pot);
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pot", pot);
		return map;
	}
}
