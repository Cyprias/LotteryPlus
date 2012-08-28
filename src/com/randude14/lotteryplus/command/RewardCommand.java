package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.lottery.Lottery;

public class RewardCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.DRAW)) {
			return false;
		}
		if(args.length < 3) {
			return ChatUtils.sendCommandHelp(sender, Permission.DRAW, "/%s reward <player> <lottery name> <x tickets> - reward a player tickets", cmd);
		}
		OfflinePlayer player = Plugin.getOfflinePlayer(args[0]);
		Lottery lottery = LotteryManager.getLottery(args[1]);
		if(lottery == null) {
			ChatUtils.error(sender, "%s does not exist.", args[1]);
			return false;
		}
		try {
			int tickets = Integer.parseInt(args[2]);
			lottery.rewardPlayer(player.getName(), tickets);
			return true;
		} catch (Exception ex) {
			ChatUtils.error(sender, "Invalid int.");
		}
		return false;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.DRAW, "/%s reward <player> <lottery name> <x tickets> - reward a player tickets", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.DRAW))
			list.add("/%s reward <player> <lottery name> <x tickets> - reward a player tickets");
	}
	
	public boolean hasValues() {
		return true;
	}
}
