package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Permission;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.lottery.Lottery;

public class BuyCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Permission.BUY)) {
			return false;
		}
		if(args.length < 2) {
			return ChatUtils.sendCommandHelp(sender, Permission.BUY, "/%s buy <lottery name> <x tickets> - buy tickets for a lottery", cmd);
		}
		Lottery lottery = LotteryManager.getLottery(args[0].toLowerCase());
		int tickets;
		try {
			tickets = Integer.parseInt(args[1]);
		} catch (Exception ex) {
			ChatUtils.error(sender, "'%s' is not an int.", args[1]);
			return false;
		}
		if(lottery.buyTickets((Player) sender, tickets)) {
			String message = Config.getProperty(Config.BUY_MESSAGE)
					.replace("<player>", sender.getName())
					.replace("<tickets>", "" + tickets)
					.replace("<lottery>", lottery.getName());
			ChatUtils.broadcast(message);
		}
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.PLAYER;
	}
	
	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Permission.BUY))
			list.add("/%s buy <lottery name> <x tickets> - buy tickets for a lottery");
	}
	
	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Permission.BUY, "/%s buy <lottery name> <x tickets> - buy tickets for a lottery", cmd);
	}
}