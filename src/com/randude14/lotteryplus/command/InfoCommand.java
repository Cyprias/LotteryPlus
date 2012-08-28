package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.lottery.Lottery;

public class InfoCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.INFO)) {
			return false;
		}
		if(args.length > 1) {
			ChatUtils.sendCommandHelp(sender, Permission.INFO, "/%s info <lottery name> - get info about a lottery", cmd);
			return true;
		}
		Lottery lottery = LotteryManager.getLottery(args[0]);
		if(lottery == null) {
			ChatUtils.error(sender, "%s does not exist", args[0]);
			return false;
		}
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "--------[%sLottery Info: %s%s]--------", ChatColor.GOLD, lottery.getName(), ChatColor.YELLOW);
		lottery.sendInfo(sender);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.INFO, "/%s info <lottery name> - get info about a lottery", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.INFO))
			list.add("/%s info <lottery name> - get info about a lottery");
	}
	
	public boolean hasValues() {
		return true;
	}
}
