package com.randude14.lotteryplus.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.lottery.Lottery;

public class SignListener implements Listener {

	private boolean isLotterySign(String[] lines) {
		String signTag = ChatUtils.cleanColorCodes(Config
				.getString(Config.SIGN_TAG));
		String line1 = ChatColor.stripColor(lines[0]);
		return signTag.equalsIgnoreCase(line1);
	}

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		if (isLotterySign(lines)) {
			if (!Plugin.checkPermission(player, Perm.SIGN_CREATE)) {
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
			lines[0] = ChatUtils.replaceColorCodes(Config
					.getString(Config.SIGN_TAG));
			lines[1] = lottery.getName();
			ChatUtils.send(player, ChatColor.YELLOW, "Sign created for %s%s.",
					ChatColor.GOLD, lottery.getName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (!Plugin.isSign(block)) {
			return;
		}
		Sign sign = (Sign) block.getState();
		Player player = event.getPlayer();
		String[] lines = sign.getLines();
		if (isLotterySign(lines)) {
			if (!Plugin.checkPermission(player, Perm.SIGN_REMOVE)) {
				event.setCancelled(true);
				return;
			}
			ChatUtils.send(player, ChatColor.YELLOW, "Sign removed from %s.",
					lines[1]);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if(!Plugin.isSign(block)) {
			return;
		}
		Sign sign = (Sign) block.getState();
		String[] lines = sign.getLines();
		if(isLotterySign(lines)) {
			lines[0] = ChatUtils.replaceColorCodes(Config.getString(Config.SIGN_TAG));
			event.setCancelled(true);
			Player player = event.getPlayer();
			String name = player.getName();
			if (!Plugin.checkPermission(player, Perm.SIGN_USE)) {
				return;
			}
			Lottery lottery = LotteryManager.getLottery(lines[1]);
			if(lottery == null) {
				ChatUtils.error(player, "This lottery sign is broken. Contact an admin for help.");
				return;
			}
			lines[1] = lottery.getName();
			if (Plugin.isBuyer(name)) {
				String lotteryName = Plugin.removeBuyer(name);
				if(lotteryName.equalsIgnoreCase(lottery.getName())) {
					ChatUtils.errorRaw(player, "Transaction cancelled.");
					ChatUtils.sendRaw(player, ChatColor.GOLD,
							"---------------------------------------------------");
					return;
				} 
			}
			ChatUtils.sendRaw(player, ChatColor.GOLD,
					"---------------------------------------------------");
			String[] messages = getSignMessage(lottery);
			player.sendMessage(messages);
			ChatUtils.send(player, "");
			ChatUtils.sendRaw(player, ChatColor.YELLOW,
					"How many tickets would you like to buy?");
			Plugin.addBuyer(name, lottery.getName());
		}
	}

	private String[] getSignMessage(Lottery lottery) {
		String signMessage = Config.getString(Config.SIGN_MESSAGE);
		signMessage = lottery.format(ChatUtils.replaceColorCodes(signMessage));
		return signMessage.split(Config.getString(Config.LINE_SEPARATOR));
	}
}
