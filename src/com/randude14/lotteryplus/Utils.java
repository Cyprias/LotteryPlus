package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import com.randude14.lotteryplus.configuration.Config;

public class Utils {
	private static final Random rand = new Random();
	
	public static long loadSeed(String line) {
		if(line == null)
			return rand.nextLong();
		try {
			return Long.parseLong(line);
		} catch (Exception ex) {	
		}
		return (long) line.hashCode();
	}
	
	public static String format(double d) {
		String format = Config.getString(Config.MONEY_FORMAT);
		return format.replace("<money>", String.format("%,.2f", d));
	}
	
	public static int toInt(String line) {
		return toInt(line, 1);
	}
	
	public static int toInt(String line, int def) {
		try {
			return Integer.parseInt(line);
		} catch (Exception ex) {
		}
		return def;
	}
	
	public static void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {
			Logger.info("exceptoin caught in sleep()");
		}
	}
	
	public static String parseToString(Location loc) {
		return String.format("%s.%d.%d.%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public static Location parseToLocation(String line) {
		String[] read = line.split("\\.", 4);
		try {
			World world = Bukkit.getWorld(read[0]);
			if(world == null) {
				Logger.info("could not find world '%s' when parsing '%s'.", read[0], line);
				return null;
			}
			double x = Double.parseDouble(read[1]);
			double y = Double.parseDouble(read[2]);
			double z = Double.parseDouble(read[3]);
			return new Location(world, x, y, z);
		} catch (Exception ex) {
			Logger.info("exception caught while parsing '%s' to a location.", line);
		}
		return null;
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
					data = Byte.parseByte(line.substring(colenIndex+1));
				else
					data = Byte.parseByte(line.substring(colenIndex+1, sizeIndex));
			}
			int itemId = Integer.parseInt(line.substring(0, colenIndex));
			if(sizeIndex >= 0)
				stackSize = Integer.parseInt(line.substring(sizeIndex+1));
			if(data == null)
				return new ItemStack(itemId, stackSize);
			else
				return new ItemStack(itemId, stackSize, data);
		} catch (Exception ex) {
			Logger.warning("Failed to load item stack '%s'.", line);
		}
		return null;
	}
	
	public static List<ItemStack> loadItemStacks(List<String> list) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(String line : list) {
			try {
				items.add(loadItemStack(line));
			} catch (Exception ex) {
				Logger.info("Failed to load %s." + line);
			}
		}
		return items;
	}
}
