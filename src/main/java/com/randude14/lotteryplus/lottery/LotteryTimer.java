package com.randude14.lotteryplus.lottery;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.util.TimeConstants;

public class LotteryTimer extends Thread implements TimeConstants {
	private final Lottery lottery;
	private boolean running;
	private long time;
	private final long reset;

	protected LotteryTimer(Lottery lottery, long time) {
		this(lottery, time, time, false);
	}

	private LotteryTimer(Lottery lottery, long time, long reset, boolean flag) {
		super("Lottery Timer " + lottery.getName());
		this.lottery = lottery;
		this.time = time;
		this.reset = reset;
		running = true;
	}

	public Lottery getLottery() {
		return lottery;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}
	
	public void reset() {
		time = reset;
	}

	public void setRunning(boolean flag) {
		this.running = flag;
	}

	public void run() {

		while (true) {

			if (running) {
				time--;
				lottery.updateSigns();

				if (isOver()) {
					Plugin plugin = lottery.getPlugin();
					plugin.getServer().broadcastMessage(
							ChatColor.YELLOW.toString()
									+ "[Lottery+] - Lottery "
									+ ChatColor.GOLD.toString()
									+ lottery.getName()
									+ ChatColor.YELLOW.toString()
									+ " is ending. and the winner is...");
					plugin.getScheduler().scheduleSyncDelayedTask(plugin,
							lottery, SERVER_SECOND * 3);
					lottery.draw();
					running = false;
				}

			}

			try {
				Thread.sleep(1000);
			} catch (Exception ex) {
			}

		}

	}

	public boolean isOver() {
		return time < 1;
	}

	public String format() {
		long sec = (time) % 60;
		long min = (time / 60) % 60;
		long hours = (time / (60 * 60)) % 24;
		long days = (time / (60 * 60 * 24)) % 7;
		long weeks = (time / (60 * 60 * 24 * 7)) % 52;
		String display = String.format("%02d:%02d:%02d:%02d:%02d", weeks, days, hours, min,
				sec);
		return display;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> serialMap = new HashMap<String, Object>();
		serialMap.put("time", time);
		serialMap.put("reset", reset);
		serialMap.put("running", running);
		return serialMap;
	}

	protected static final LotteryTimer deserialize(
			Map<String, Object> serialMap, Lottery lottery) {
		long time = (Long) serialMap.get("time");
		long reset = (Long) serialMap.get("reset");
		boolean running = (Boolean) serialMap.get("running");
		return new LotteryTimer(lottery, time, reset, running);
	}

}
