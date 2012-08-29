package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;

public class ListCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.LIST)) {
			return false;
		}
		int page = 1;
		if(args.length > 0) {
			try {
				page = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				ChatUtils.error(sender, "Invalid int.");
				return false;
			}
		}
		LotteryManager.listLotteries(sender, page);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.LIST, "/%s list <page> - list lotteries", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.LIST))
			list.add("/%s list <page> - list lotteries");
	}
	
	public boolean hasValues() {
		return false;
	}
}
