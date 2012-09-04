package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Perm;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.lottery.Lottery;

public class DrawCommand implements Command {

	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args) {
		if(!Plugin.checkPermission(sender, Perm.DRAW)) {
			return false;
		}
		if(args.length < 1) {
			return ChatUtils.sendCommandHelp(sender, Perm.DRAW, "/%s draw <lottery name> - force draw a lottery", cmd);
		}
		Lottery lottery = LotteryManager.getLottery(args[0]);
		if(lottery == null) {
			ChatUtils.error(sender, "%s does not exist.", args[0]);
			return false;
		}
		lottery.draw(sender);
		return true;
	}

	public CommandAccess getAccess() {
		return CommandAccess.CONSOLE;
	}

	public void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		ChatUtils.sendCommandHelp(sender, Perm.DRAW, "/%s draw <lottery name> - force draw a lottery", cmd);
	}

	public void listCommands(CommandSender sender, List<String> list) {
		if(Plugin.hasPermission(sender, Perm.DRAW))
			list.add("/%s draw <lottery name> - force draw a lottery");
	}
	
	public boolean hasValues() {
		return true;
	}
}
