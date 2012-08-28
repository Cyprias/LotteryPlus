package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;

public class ReloadCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.RELOAD)) {
			return false;
		}
		if(args.length < 1) {
			return ChatUtils.sendCommandHelp(sender, Permission.RELOAD, "/%s reload <lottery name> - reload a lottery", cmd);
		}
		LotteryManager.reloadLottery(sender, args[0]);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.RELOAD, "/%s reload <lottery name> - reload a lottery", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.RELOAD))
			list.add("/%s reload <lottery name> - reload a lottery");
	}
	
	public boolean hasValues() {
		return true;
	}
}
