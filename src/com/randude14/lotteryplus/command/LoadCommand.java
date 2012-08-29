package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;

public class LoadCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.LOAD)) {
			return false;
		}
		if(args.length < 1) {
			int numLotteries = LotteryManager.loadLotteries(false);
			if(numLotteries == 1) {
				ChatUtils.send(sender, ChatColor.YELLOW, "1 lottery was loaded.");
			} else {
				ChatUtils.send(sender, ChatColor.YELLOW, "%d lotteries were loaded.", numLotteries);
			}
		} else {
			LotteryManager.loadLottery(sender, args[0]);
		}
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.LOAD, "/%s load <lottery name> - load a lottery from the 'lotteries.yml'", cmd);
		ChatUtils.sendCommandHelp(sender, Permission.LOAD, "/%s load - load unloaded lotteries from the 'lotteries.yml'", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.LOAD)) {
			list.add("/%s load <lottery name> - load a lottery from the 'lotteries.yml'");
			list.add("/%s load - load unloaded lotteries from the 'lotteries.yml'");
		}
	}
	
	public boolean hasValues() {
		return false;
	}
}
