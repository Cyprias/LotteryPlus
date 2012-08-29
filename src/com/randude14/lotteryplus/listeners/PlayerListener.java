package com.randude14.lotteryplus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.ClaimManager;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.lottery.Lottery;

public class PlayerListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		ClaimManager.notifyOfClaims(player);
		String[] mainLotteries = Config.getString(Config.MAIN_LOTTERIES).split("\\s+");
		for(String lotteryName : mainLotteries) {
			Lottery lottery = LotteryManager.getLottery(lotteryName);
			if(lottery == null)
				return;
			String message = Config.getString(Config.MAIN_LOTTERIES_MESSAGE);
			message = lottery.format(message);
			ChatUtils.send(player, message);
		}
	}
}
