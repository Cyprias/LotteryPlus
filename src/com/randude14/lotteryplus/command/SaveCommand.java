package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;

public class SaveCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.FORCE_SAVE)) {
			return false;
		}
		LotteryManager.saveLotteries();
		ChatUtils.send(sender, ChatColor.YELLOW, "Lotteries have been saved.");
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.FORCE_SAVE, "/%s save - force save lotteries.", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.FORCE_SAVE))
			list.add("/%s save - force save lotteries.");
	}
	
	public boolean hasValues() {
		return false;
	}
}
