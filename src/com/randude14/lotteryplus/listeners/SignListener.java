package com.randude14.lotteryplus.listeners;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.util.FormatOptions;

public class SignListener implements Listener, FormatOptions {

	public SignListener() {
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {

		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		Block block = event.getBlock();
		if(!Plugin.isSign(block)) 
			return;
		Sign sign = (Sign) block.getState();
		String[] lines = event.getLines();

		if (lines[0].equalsIgnoreCase("[Lottery+]")) {

			if (!Plugin.hasPermission(player, Permission.SIGN_CREATE)) {
				event.setCancelled(true);
				return;
			}

			if (lines[1] == null || lines[1].equals("")) {
				ChatUtils.error(player, "Must specify lottery.");
				event.setCancelled(true);
				return;
			}

			Lottery lottery = LotteryManager.getLottery(lines[1]);

			if (lottery == null) {
				ChatUtils.error(player, "%s does not exist.", lines[1]);
				event.setCancelled(true);
				return;
			}
			
			lottery.registerSign(sign);
			ChatUtils.send(player, ChatColor.YELLOW, "Sign created for %s.", lottery.getName());
		}

	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		List<Lottery> lotteries = LotteryManager.getLotteries();
		Location loc = event.getBlock().getLocation();
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (event.isCancelled()) {
			return;
		}

		if (!Plugin.isSign(block)) {
			return;
		}

		for (Lottery lottery : lotteries) {

			if (lottery.signAtLocation(loc)) {

				if (!Plugin.checkPermission(player, Permission.SIGN_REMOVE)) {
					event.setCancelled(true);
					lottery.updateSigns();
				}

				else {
					lottery.unregisterSign(loc);
					ChatUtils.send(player, ChatColor.YELLOW, "Sign removed.");
				}

			}

		}

	}

	@EventHandler
	public void playerRightClick(PlayerInteractEvent event) {
		List<Lottery> lotteries = LotteryManager.getLotteries();
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		String name = player.getName();

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Location loc = block.getLocation();

		for (Lottery lottery : lotteries) {

			if (lottery.signAtLocation(loc)) {
				event.setCancelled(true);

				if (!Plugin.hasPermission(player, Permission.BUY)) {
					ChatUtils.error(player, "You do not have permission");
					return;
				}

				if (Plugin.isBuyer(name)) {
					Plugin.removeBuyer(name);
					ChatUtils.error(player, "Transaction cancelled.");
					ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
					return;
				}

				ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
				String[] messages = getSignMessage(lottery);
				for (String message : messages) {
					player.sendMessage(message);
				}
				ChatUtils.send(player, "");
				ChatUtils.send(player, "How many tickets would you like to buy?");
				Plugin.addBuyer(player.getName(), lottery.getName());
			}

		}

	}

	private String[] getSignMessage(Lottery lottery) {
		String signMessage = Config.getProperty(Config.SIGN_MESSAGE);
		lottery.format(ChatUtils.replaceColorCodes(signMessage));
		return signMessage.split("\\n");
	}
}
