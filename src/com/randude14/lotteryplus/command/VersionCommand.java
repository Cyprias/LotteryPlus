package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;

public class VersionCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.DRAW)) {
			return false;
		}
		ChatUtils.send(sender, ChatColor.YELLOW, "Version: %s%s", ChatColor.GOLD, Plugin.getVersion());
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.DRAW, "/%s version - get plugin version", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.DRAW))
			list.add("/%s version - get plugin version");
	}
	
	public boolean hasValues() {
		return false;
	}
}
