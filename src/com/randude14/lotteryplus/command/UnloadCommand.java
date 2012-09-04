package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;

public class UnloadCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.UNLOAD)) {
			return false;
		}
		if(args.length < 1) {
			ChatUtils.sendCommandHelp(sender, "/%s unload <lottery name> - unload a lottery", cmd);
			return true;
		}
		LotteryManager.unloadLottery(sender, args[0]);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.UNLOAD, "/%s unload <lottery name> - unload a lottery", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.UNLOAD))
			list.add("/%s unload <lottery name> - unload a lottery");
	}
	
	public boolean hasValues() {
		return false;
	}
}
