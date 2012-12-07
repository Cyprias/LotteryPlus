package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.util.TimeConstants;

public class Utils implements TimeConstants {
	
	public static long loadSeed(String line) {
		if(line == null)
			return new Random().nextLong();
		try {
			return Long.parseLong(line);
		} catch (Exception ex) {	
		}
		return (long) line.hashCode();
	}
	
	public static void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {
			Logger.info("exception caught in sleep()");
		}
	}
	
	public static String parseLocation(Location loc) {
		return String.format("%s %d %d %d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public static Location parseToLocation(String str) {
		String[] lines = str.split("\\s");
		World world = Bukkit.getWorld(lines[0]);
		if(world == null) return null;
		int x = Integer.parseInt(lines[1]);
		int y = Integer.parseInt(lines[2]);
		int z = Integer.parseInt(lines[3]);
		return new Location(world, x, y, z);
	}
	
	public static String format(double amount) {
		return Config.getString(Config.MONEY_FORMAT).replace("<money>", String.format("%,.2f", amount));
	}
	
	public static ItemStack loadItemStack(String line) {
		if(line == null || line.isEmpty()) {
			return null;
		}
		try {
			Byte data = null;
			int stackSize = 1;
			int colenIndex = line.indexOf(':');
			int sizeIndex = line.indexOf('*');
			if(colenIndex < 0) {
				if(sizeIndex < 0)
					colenIndex = line.length();
				else
					colenIndex = sizeIndex;
			}
			else {
				if(sizeIndex < 0)
					data = Byte.valueOf(line.substring(colenIndex+1));
				else
					data = Byte.valueOf(line.substring(colenIndex+1, sizeIndex));
			}
			int itemId = Integer.parseInt(line.substring(0, colenIndex));
			if(sizeIndex >= 0)
				stackSize = Integer.parseInt(line.substring(sizeIndex+1));
			ItemStack result = new ItemStack(itemId, stackSize, data);
			return result;
		} catch (Exception ex) {
			Logger.warning("Failed to load item stack '%s'.", line);
		}
		return null;
	}
	
	public static List<ItemStack> getItemStacks(String line) {
		List<ItemStack> rewards = new ArrayList<ItemStack>();
		listItemStacks(rewards, line);
		return rewards;
	}
	
	public static void listItemStacks(List<ItemStack> rewards, String line) {
		for(String str : line.split("\\s+")) {
			try {
				ItemStack item = loadItemStack(str);
				if(item != null)
					rewards.add(item);
			} catch (Exception ex) {
				Logger.info("Failed to load %s.", line);
			}
		}
	}
}
