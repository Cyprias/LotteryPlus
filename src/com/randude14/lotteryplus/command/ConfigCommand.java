package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;

public class ConfigCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.DRAW)) {
			return false;
		}
		if(args.length == 0|| !args[0].equals("reload")) {
			getCommands(sender, cmd);
			return true;
		}
		Plugin.reload();
		ChatUtils.send(sender, ChatColor.YELLOW, "Version %s reloaded.", Plugin.getVersion());
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.DRAW, "/%s config reload - reload config", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.DRAW))
			list.add("/%s config reload - reload config");
	}
	
	public boolean hasValues() {
		return true;
	}
}
