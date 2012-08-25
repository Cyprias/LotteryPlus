package com.randude14.lotteryplus.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface Command {
	
	boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args);
	
	CommandAccess getAccess();
	
	void getCommands(CommandSender sender, org.bukkit.command.Command cmd);
	
	void listCommands(CommandSender sender, List<String> list);

}
