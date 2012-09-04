package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.lottery.Lottery;

public class RewardCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.REWARD)) {
			return false;
		}
		if(args.length < 3) {
			return ChatUtils.sendCommandHelp(sender, Perm.REWARD, "/%s reward <player> <lottery name> <x tickets> - reward a player tickets", cmd);
		}
		Lottery lottery = LotteryManager.getLottery(args[1]);
		if(lottery == null) {
			ChatUtils.error(sender, "%s does not exist.", args[1]);
			return false;
		}
		if(args[0].equalsIgnoreCase(sender.getName())) {
			ChatUtils.error(sender, "You cannot reward yourself.", args[1]);
			return false;
		}
		OfflinePlayer player = Plugin.getOfflinePlayer(args[0]);
		String name = player.getName();
		try {
			int tickets = Integer.parseInt(args[2]);
			if(lottery.rewardPlayer(sender, name, tickets)) {
				ChatUtils.send(sender, ChatColor.GOLD, "%s %shas been rewarded %s%d ticket(s) %sfor %s%s", 
						name, ChatColor.YELLOW, ChatColor.GOLD, tickets, ChatColor.YELLOW, ChatColor.GOLD, lottery.getName());
			}		
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
		ChatUtils.sendCommandHelp(sender, Perm.REWARD, "/%s reward <player> <lottery name> <x tickets> - reward a player tickets", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.REWARD))
			list.add("/%s reward <player> <lottery name> <x tickets> - reward a player tickets");
	}
	
	public boolean hasValues() {
		return true;
	}
}
