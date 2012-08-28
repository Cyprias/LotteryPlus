package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.WinnersManager;

public class WinnersCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.WINNERS)) {
			return false;
		}
		WinnersManager.listWinners(sender);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.WINNERS, "/%s winners - view recent winners of lotteries", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.WINNERS))
			list.add("/%s winners - view recent winners of lotteries");
	}
	
	public boolean hasValues() {
		return false;
	}
}
