package com.randude14.lotteryplus.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Plugin;

public class CommandManager implements CommandExecutor {
	private final Map<String, Command> commands = new HashMap<String, Command>();

	public CommandManager() {

	}

	public CommandManager registerCommand(String label, Command command) {
		commands.put(label.toLowerCase(), command);
		return this;
	}

	public boolean onCommand(CommandSender sender,
			org.bukkit.command.Command cmd, String label, String[] args) {
		if(args.length == 0) {
			this.getCommands(sender, cmd);
		} else {
			int page;
			try {
				page = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				page = -1;
			}
			if(page >= 1) {
				this.getCommands(sender, cmd, page);
				return true;
			}
		}
		Command command = commands.get(args[0].toLowerCase());
		if (command != null) {
			CommandAccess access = command.getAccess();
			if (!access.hasAccess(sender)) {
				ChatUtils.error(sender, "You do not have access to this command.");
			} else {
				try {
					args = (String[]) ArrayUtils.remove(args, 0);
					if(args.length == 0 || args[0].equals("?")) {
						command.getCommands(sender, cmd);
						return true;
					}
					return command.execute(sender, cmd, args);
				} catch (Exception ex) {
					ChatUtils.error(sender,
							"Exception caught while executing this command.");
					ex.printStackTrace();
				}
			}
		}
		return false;
	}
	
	private void getCommands(CommandSender sender, org.bukkit.command.Command cmd) {
		getCommands(sender, cmd, 1);
	}

	private void getCommands(CommandSender sender,
			org.bukkit.command.Command cmd, int page) {
		List<String> list = new ArrayList<String>();
		for (Command command : commands.values()) {
			CommandAccess access = command.getAccess();
			if (access.hasAccess(sender))
				command.listCommands(sender, list);
		}
		listCommands(sender, cmd, list, page);
	}
	
	private void listCommands(CommandSender sender, org.bukkit.command.Command cmd, List<String> list, int page) {
		ChatUtils.send(sender, ChatColor.YELLOW, "--------[%s%s%s]--------",
				ChatColor.GOLD, Plugin.getInstance().getName(),
				ChatColor.YELLOW);
		int start = page * 10;
		for(int cntr = start;cntr < start+10;cntr++) {
			ChatUtils.sendCommandHelp(sender, null, list.get(cntr), cmd);
		}
	}
}
