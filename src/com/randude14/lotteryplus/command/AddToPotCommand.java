package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.lottery.Lottery;

public class AddToPotCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.ADD_TO_POT)) {
			return false;
		}
		if(args.length < 2) {
			return ChatUtils.sendCommandHelp(sender, Permission.ADD_TO_POT, "/%s [addtopot/atp] <lottery name> <money> - add money to a lotteries pot", cmd);
		}
		Lottery lottery = LotteryManager.getLottery(args[0]);
		if(lottery == null) {
			ChatUtils.error(sender, "%s does not exist.", args[0]);
			return false;
		}
		try {
			double add = Double.parseDouble(args[1]);
			return lottery.addToPot(sender, add);
		} catch (Exception ex) {
			ChatUtils.error(sender, "Invalid money.");
		}
		return false;
	}

	public CommandAccess getAccess() {
		return CommandAccess.BOTH;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.ADD_TO_POT, "/%s [addtopot/atp] <lottery name> <money> - add money to a lotteries pot", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.ADD_TO_POT))
			list.add("/%s [addtopot/atp] <lottery name> <money> - add money to a lotteries pot");
	}
	
	public boolean hasValues() {
		return true;
	}
}
