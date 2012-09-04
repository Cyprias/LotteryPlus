package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;

public class CreateCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.CREATE)) {
			return false;
		}
		if(args.length > 1) {
			ChatUtils.sendCommandHelp(sender, Perm.CREATE, "/%s create <lottery name> - create a lottery section in the 'lotteries.yml'.", cmd);
			return true;
		}
		return LotteryManager.createLotterySection(sender, args[0]);
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.CREATE, "/%s create <lottery name> - create a lottery section in the 'lotteries.yml'.", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.CREATE))
			list.add("/%s create <lottery name> - create a lottery section in the 'lotteries.yml'.");
	}
	
	public boolean hasValues() {
		return true;
	}
}
