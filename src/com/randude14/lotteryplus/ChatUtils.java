package com.randude14.lotteryplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {
	private static final Plugin plugin = Plugin.getInstance();
	
	public static void broadcast(String format, Object... args) {
		broadcast(String.format(format, args));
	}
	
	public static void broadcast(String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\\n");
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(getChatPrefix() + messages);
		}
		Bukkit.getConsoleSender().sendMessage(messages);
	}
	
	public static void broadcastRaw(String format, Object... args) {
		broadcastRaw(String.format(format, args));
	}
	
	public static void broadcastRaw(String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\\n");
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(messages);
		}
		Bukkit.getConsoleSender().sendMessage(messages);
	}
	
	public static void send(CommandSender sender, String message) {
		message = replaceColorCodes(message);
		String[] messages = message.split("\\n");
		sender.sendMessage(messages);
	}
	
	public static void send(CommandSender sender, ChatColor color, String message) {
		sender.sendMessage(color + getChatPrefix() + message);
	}

	public static void send(CommandSender sender, ChatColor color, String format,
			Object... args) {
		sender.sendMessage(color + getChatPrefix()
				+ String.format(format, args));
	}

	public static void sendRaw(CommandSender sender, ChatColor color, String message) {
		sender.sendMessage(color + message);
	}

	public static void sendRaw(CommandSender sender, ChatColor color, String format,
			Object... args) {
		sender.sendMessage(color + String.format(format, args));
	}

	public static boolean sendCommandHelp(CommandSender sender, Permission permission,
			String line, org.bukkit.command.Command cmd) {
		if (!Plugin.hasPermission(sender, permission))
			return false;
		sendRaw(sender, ChatColor.GOLD, "- " + line, cmd.getLabel());
		return true;
	}

	public static void error(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.RED + getChatPrefix() + message);
	}

	public static void error(CommandSender sender, String format, Object... args) {
		sender.sendMessage(ChatColor.RED + getChatPrefix()
				+ String.format(format, args));
	}

	public static void errorRaw(CommandSender sender, String message) {
		sender.sendMessage(ChatColor.RED + message);
	}

	public static void errorRaw(CommandSender sender, String format, Object... args) {
		sender.sendMessage(ChatColor.RED + String.format(format, args));
	}

	public static final String getChatPrefix() {
		return String.format(ChatColor.YELLOW + "[%s] - ", plugin.getName());
	}
	
	public static final String replaceColorCodes(String mess) {
		return ChatColor.translateAlternateColorCodes('&', mess);
	}
}
