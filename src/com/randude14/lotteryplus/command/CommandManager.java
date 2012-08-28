package com.randude14.lotteryplus.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Plugin;

public class CommandManager implements CommandExecutor, Listable {
	private static final Plugin plugin = Plugin.getInstance();
	private final Map<String, Command> commands = new TreeMap<String, Command>(String.CASE_INSENSITIVE_ORDER);

	public CommandManager() {

	}

	public CommandManager registerCommand(String label, Command command) {
		commands.put(label.toLowerCase(), command);
		return this;
	}

	public boolean onCommand(CommandSender sender,
			org.bukkit.command.Command cmd, String label, String[] args) {
		boolean help = false;
		if(args.length == 0 || args[0].equals("?")) {
			this.getCommands(sender, this, cmd);
			help = true;
		}
		if(!help) {
			try {
				int page = Integer.parseInt(args[0]);
				this.getCommands(sender, this, cmd, page);
				help = true;
			} catch (Exception ex) {
			}
		}
		if(help) {
			return true;
		}
		Command command = commands.get(args[0].toLowerCase());
		if (command != null) {
			CommandAccess access = command.getAccess();
			if (!access.hasAccess(sender)) {
				ChatUtils.error(sender, "You do not have access to this command.");
			} else {
				try {
					args = (String[]) ArrayUtils.remove(args, 0);
					if(args.length == 0) {
						if(!command.hasValues()) {
							return command.execute(sender, cmd, args);
						}
						this.getCommands(sender, command, cmd);
						return true;
					} else {
						if(args[0].equals("?")) {
							this.getCommands(sender, command, cmd);
							return true;
						}
						try {
							int page = Integer.parseInt(args[0]);
							this.getCommands(sender, command, cmd, page);
							return true;
						} catch (Exception ex) {
							try {
								return command.execute(sender, cmd, args);
							} catch (Exception ex1) {
								ChatUtils.error(sender,
										"Exception caught while executing this command.");
								ex.printStackTrace();
							}
						}
					}
				} catch (Exception ex) {
					ChatUtils.error(sender,
							"Exception caught while executing this command.");
					ex.printStackTrace();
				}
			}
		} else {
			ChatUtils.error(sender, "Did not recognize '%s' as a command.", args[0]);
		}
		return false;
	}
	
    public void listCommands(CommandSender sender, List<String> list) {
		for(Command command : commands.values()) {
			if(command.getAccess().hasAccess(sender))
				command.listCommands(sender, list);
		}
	}
	
    protected void getCommands(CommandSender sender, Listable listable, org.bukkit.command.Command cmd) {
		getCommands(sender, listable, cmd, 1);
	}

	protected void getCommands(CommandSender sender, Listable listable, org.bukkit.command.Command cmd, int page) {
		List<String> list = new ArrayList<String>();
		listable.listCommands(sender, list);
		int len = list.size();
		int max = (len / 10) + 1;
		if (len % 10 == 0)
			max--;
		if (page > max)
			page = max;
		if (page < 1)
			page = 1;
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "--------[%s%s Page (%d/%d)%s]--------", ChatColor.GOLD, plugin.getName(), page, max, ChatColor.YELLOW);
		for (int cntr = (page * 10) - 10, stop = cntr + 10; cntr < stop && cntr < len; cntr++) {
			ChatUtils.sendCommandHelp(sender, (cntr+1) + ". ", list.get(cntr), cmd);
		}
	}
}
