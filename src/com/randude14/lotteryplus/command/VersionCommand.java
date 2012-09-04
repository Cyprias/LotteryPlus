package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;

public class VersionCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.DRAW)) {
			return false;
		}
		ChatUtils.send(sender, ChatColor.GOLD, "Version: %s", Plugin.getVersion());
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.DRAW, "/%s version - get plugin version", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.DRAW))
			list.add("/%s version - get plugin version");
	}
	
	public boolean hasValues() {
		return false;
	}
}
