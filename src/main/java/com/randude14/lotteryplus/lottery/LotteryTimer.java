package com.randude14.lotteryplus.lottery;

import org.bukkit.ChatColor;

import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.util.TimeConstants;

public class LotteryTimer implements TimeConstants {
	private final Lottery lottery;
	private boolean running;
	private long time;
	private long reset;

	protected LotteryTimer(Lottery lottery) {
		this.lottery = lottery;
	}

	public Lottery getLottery() {
		return lottery;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setResetTime(long reset) {
		this.reset = reset;
	}

	public long getTime() {
		return time;
	}

	public void reset() {
		time = reset;
	}

	public void start() {
		setRunning(true);
	}

	public void stop() {
		setRunning(false);
	}

	public void setRunning(boolean flag) {
		this.running = flag;
	}

	public long getResetTime() {
		return reset;
	}

	public boolean isRunning() {
		return running;
	}

	public void countdown() {
		
		if(!running && !isOver()) {
			running = true;
		}

		if (running) {
			time--;
			lottery.updateSigns();
			if (isOver()) {
				Plugin plugin = lottery.getPlugin();
				plugin.getServer().broadcastMessage(
						ChatColor.YELLOW.toString() + "[" + plugin.getName()
								+ "] - Lottery " + ChatColor.GOLD.toString()
								+ lottery.getName() + ChatColor.YELLOW.toString()
								+ " is ending. and the winner is...");
				plugin.getScheduler().scheduleSyncDelayedTask(plugin, lottery,
						SERVER_SECOND * 3);
				lottery.setDrawing(true);
				lottery.updateSigns();
				running = false;
			}
			
		}

	}

	public boolean isOver() {
		return time < 1;
	}

	public String format() {
		long sec = (time) % 60;
		long min = (time / MINUTE) % 60;
		long hours = (time / HOUR) % 24;
		long days = (time / DAY) % 7;
		long weeks = (time / WEEK) % 52;
		String display = String.format("%02d:%02d:%02d:%02d:%02d", weeks, days,
				hours, min, sec);
		return display;
	}

}
