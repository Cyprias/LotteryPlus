package com.randude14.lotteryplus.lottery;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

@SuppressWarnings("serial")
public class LotteryLocation implements Serializable {
	private double x;
	private double y;
	private double z;
	private String world;
	
	public LotteryLocation(Location loc) {
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		world = loc.getWorld().getName();
	}
	
	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

}
