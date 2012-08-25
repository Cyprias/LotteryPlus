package com.randude14.lotteryplus.lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.ClaimManager;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.Utils;
import com.randude14.lotteryplus.WinnersManager;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.util.FormatOptions;
import com.randude14.lotteryplus.util.SignFormatter;
import com.randude14.lotteryplus.util.TimeConstants;

public class Lottery implements FormatOptions, TimeConstants, Runnable {
	private static final Plugin plugin = Plugin.getInstance();
	private final LotteryTimer timer;
	private final List<Reward> rewards;
	private final List<Sign> signs;
	private final String lotteryName;
	private final Random rand;
	private LotteryOptions options;
	private SignFormatter formatter;

	public Lottery(String name) {
		this.rewards = new ArrayList<Reward>();
		this.signs = Collections.synchronizedList(new ArrayList<Sign>());
		this.lotteryName = name;
		this.timer = new LotteryTimer(this);
		this.rand = new Random();
	}

	public LotteryOptions getOptions() {
		return options;
	}

	public final String getName() {
		return lotteryName;
	}

	public boolean isDrawing() {
		return options.get("drawing", false);
	}
	public boolean isRunning() {
		return timer.isRunning();
	}

	public boolean isItemOnly() {
		int num = 0;
		for(Reward reward : rewards) {
			if(reward instanceof ItemReward)
				num++;
		}
		return options.get(Config.DEFAULT_ITEM_ONLY) && num > 0;
	} 
	
	public String format(String mess) {
		String winner = options.get("winner", null);
		return mess.replace(FORMAT_REWARD, formatReward())
				.replace(FORMAT_TIME, timer.format())
				.replace(FORMAT_NAME, lotteryName)
				.replace(FORMAT_WINNER, (winner != null) ? winner : "no winner yet")
				.replace(FORMAT_TICKET_COST, String.format("%,.2f", options.get(Config.DEFAULT_TICKET_COST)))
				.replace(FORMAT_TICKET_TAX, String.format("%,.2f", options.get(Config.DEFAULT_TICKET_TAX)))
				.replace(FORMAT_POT_TAX, String.format("%,.2f", options.get(Config.DEFAULT_POT_TAX)));
	}
	
	private String formatReward() {
		if(!isItemOnly())
			return Utils.format(options.get(Config.DEFAULT_POT));
		int num = 0;
		for(int cntr = 0;cntr < rewards.size();cntr++) {
			if(rewards instanceof ItemReward)
				 num++;
		}
		return num + " item(s)";
	}

	public void setOptions(LotteryOptions options) {
		this.options = options;
		rewards.clear();
		rand.setSeed(Utils.loadSeed(options.get(Config.DEFAULT_SEED)));
		String read = options.get(Config.DEFAULT_ITEM_REWARDS);
		if(read == null || read.equals("")) {
			for(String line : read.split("\\s+")) {
				ItemStack item = Utils.loadItemStack(line);
				if(item != null)
					rewards.add(new ItemReward(item));
			}
		}
		timer.setRunning(true);
		if(options.contains("save-time") && options.contains("reset-time")) {
			long saveTime = options.get("save-time", 0L);
			timer.setTime(saveTime);
			long resetTime = options.get("reset-time", 0L);
			timer.setResetTime(resetTime);
		} else {
			long time = options.get(Config.DEFAULT_TIME);
			timer.setTime(time);
			timer.setResetTime(time);
		}
		if(options.contains("signs")) {
			String line = options.get("signs", "");
			for(String str : line.split("\\s+")) {
				Location loc = Utils.parseToLocation(str);
				if(loc != null && Plugin.isSign(loc))
					signs.add((Sign) loc.getBlock().getState());
			}
		}
	}
	
	public void save() {
		options.set("save-time", timer.getTime());
		options.set("reset-time", timer.getResetTime());
		if(!signs.isEmpty()) {
			String line = "";
			int len = signs.size();
			for(int cntr = 0;cntr < len;cntr++) {
				line += Utils.parseToString(signs.get(cntr).getLocation());
				if(cntr < len-1)
					line += " ";
			}
			options.set("signs", line);
		}
	}

	public boolean canBuy(Player player, int tickets) {
		return this.canBuy(player.getName(), tickets);
	}

	public boolean canBuy(String player, int tickets) {
		int maxTickets = options.get(Config.DEFAULT_MAX_TICKETS, 0);
		if (maxTickets <= 0)
			return true;
		int num = options.get("players." + player, 0);
		return num + tickets <= maxTickets;
	}

	public boolean buyTickets(Player player, int tickets) {
		if (!canBuy(player, tickets)) {
			ChatUtils.error(player, "You cannot buy this many tickets.");
			return false;
		}
		Economy econ = Plugin.getEconomy();
		String name = player.getName();
		if (!econ.hasAccount(name)) {
			ChatUtils.error(player,
					"You do not have an account with your server's economy.");
			return false;
		}
		double ticketCost = options.get(Config.DEFAULT_TICKET_COST);
		double sub = ticketCost * (double) tickets;
		if (!econ.has(name, sub)) {
			ChatUtils
					.error(player,
							"You do not have enough money to buy this amount of tickets.");
			return false;
		}
		econ.withdrawPlayer(name, sub);
		int num = options.get("players." + name, 0);
		options.set("players." + name, num + tickets);
		double ticketTax = options.get(Config.DEFAULT_TICKET_TAX);
		double add = ticketCost - (ticketCost * (ticketTax / 100));
		double d = add * (double) tickets;
		ChatUtils.send(player, ChatColor.YELLOW,
				"You have bought %s%d tickets %sfor %s%s.", ChatColor.GOLD,
				tickets, ChatColor.YELLOW, ChatColor.GOLD);
		ChatUtils
				.send(player, ChatColor.GOLD,
						"%s %shas been added to %s%s's %spot.",
						Utils.format(d), ChatColor.YELLOW, ChatColor.GOLD,
						lotteryName, ChatColor.YELLOW);
		options.set(Config.DEFAULT_POT, options.get(Config.DEFAULT_POT) + d);
		return true;
	}

	public Sign getSignAt(Location location) {
		synchronized(signs) {
			for (Sign lotterySign : signs) {
				if (Plugin.locsInBounds(location, lotterySign.getBlock()
						.getLocation())) {
					return lotterySign;
				}
			}
			return null;
		}
	}

	public boolean unregisterSign(Location location) {
		synchronized(signs) {
			for (int cntr = 0; cntr < signs.size(); cntr++) {
				Sign sign = signs.get(cntr);
				if (Plugin.locsInBounds(location, sign.getLocation())) {
					signs.remove(cntr);
					sign.setLine(0, "");
					sign.setLine(1, "");
					sign.setLine(2, "");
					sign.setLine(3, "");
					sign.update();
					return true;
				}
			}
			return false;
		}
	}
	
	public void registerSign(Sign sign) {	
		synchronized(signs) {
			signs.add(sign);		
			updateSigns();
		}	
	}

	public void updateSigns() {
		synchronized(signs) {
			for (Sign sign : signs) {
				if (!sign.getChunk().isLoaded()) {
					continue;
				}
				formatter.format(sign);
				sign.update(true);
			}
		}
	}

	public boolean signAtLocation(Location location) {
		synchronized(signs) {
			for (Sign sign : signs) {
				if (Plugin.locsInBounds(sign.getLocation(), location)) {
					return true;
				}
			}
			return false;
		}
	}

	public void sendInfo(CommandSender sender) {
			ChatUtils.send(sender, "Time Left: " + timer.format());
		if (!isItemOnly()) {
			ChatUtils.send(sender, "Pot: " + Utils.format(options.get(Config.DEFAULT_POT)));
		}
		for (Reward reward : rewards) {
			ChatUtils.send(sender, reward.getInfo());
		}
		ChatUtils.send(sender, "Ticket Cost: " + Utils.format(options.get(Config.DEFAULT_TICKET_COST)));
		ChatUtils.send(sender, "Ticket Tax: " + Utils.format(options.get(Config.DEFAULT_TICKET_TAX)));
		ChatUtils.send(sender, "Pot Tax: " + Utils.format(options.get(Config.DEFAULT_POT_TAX)));
		ChatUtils.send(sender, "Players Entered: " + getPlayersEntered());
		if(sender instanceof Player)
			ChatUtils.send(sender, "Tickets Bought: " + getTicketsBought(sender.getName()));
	}
	
	public void sendTimeLeftInfo(CommandSender sender) {
		String message = String.format("%s: %s", timer.format());
		if(sender instanceof Player)
			message += ", " + String.format("%d/%d Tickets Left To Buy");
		ChatUtils.send(sender, ChatColor.YELLOW, message);
	}
	
	public int getPlayersEntered() {
		Set<String> players = new HashSet<String>();
		for (String key : options.keySet()) {
			if (key.startsWith("players")) {
				int index = key.indexOf('.');
				String player = key.substring(index + 1);
				int num = options.get(key, 0);
				for (int cntr = 0; cntr < num; cntr++) {
					players.add(player);
				}
			}
		}
		return players.size();
	}

	public List<String> getPlayers() {
		List<String> players = new ArrayList<String>();
		for (String key : options.keySet()) {
			if (key.startsWith("players")) {
				int index = key.indexOf('.');
				String player = key.substring(index + 1);
				int num = options.get(key, 0);
				for (int cntr = 0; cntr < num; cntr++) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public int getTicketsBought(String name) {
		return options.get("players." + name, 0);
	}

	public long getTime() {
		return timer.getTime();
	}

	public void draw() {
		draw(null);
	}

	public void draw(CommandSender sender) {
		if (options.get("drawing", false)) {
			return;
		}
		if (sender == null) {
			ChatUtils.broadcast("%s %sis ending, and the winner is...",
					ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		} else {
			ChatUtils.broadcast(
					"%s%s %sis force drawing %s%s, %sand the winner is...",
					ChatColor.GOLD, sender.getName(), ChatColor.YELLOW,
					ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		}
		Plugin.scheduleSyncDelayedTask(this, SERVER_SECOND * 3);
		timer.setRunning(false);
		updateSigns();
		options.set("drawing", true);
	}

	public void run() {
		List<String> players = this.getPlayers();
		int len = players.size();
		if (len < options.get(Config.DEFAULT_MIN_PLAYERS) || len < 1) {
			ChatUtils
					.broadcast("no one! there were not enough players entered. restarting lottery.");
			readResetData();
			options.set("drawing", false);
			return;
		}

		Collections.shuffle(players, rand);
		String winner = players.get(rand.nextInt(players.size()));
		options.set("winner", winner);
		double pot = options.get(Config.DEFAULT_POT);
		double potTax = options.get(Config.DEFAULT_POT_TAX);
		double winnings = pot - (pot * (potTax / 100));

		ChatUtils.broadcast("%s%s!", ChatColor.GOLD, winner);

		StringBuilder logWinner = new StringBuilder(lotteryName + ": " + winner);
		if (!this.isItemOnly())
			rewards.add(0, new PotReward(winnings));
		int rewardsLen = rewards.size();
		for (int cntr = 0; cntr < rewardsLen; cntr++) {
			logWinner.append(", ");
			logWinner.append(rewards.get(cntr).getInfo());
		}

		WinnersManager.logWinner(logWinner.toString());
		Player pWinner = Bukkit.getPlayer(winner);

		if (pWinner != null) {
			for (Reward reward : rewards) {
				reward.rewardPlayer(pWinner);
			}
		}

		else {
			ClaimManager.addClaim(winner, lotteryName, rewards);
		}
	}

	private void readResetData() {
		long time = options.get("reset-add-time", 0L);		
		this.timer.setResetTime(timer.getResetTime() + time);		
		options.set(Config.DEFAULT_TICKET_COST, options.get(Config.DEFAULT_TICKET_COST) + options.get(Config.DEFAULT_RESET_ADD_TICKET_COST));	
		options.set(Config.DEFAULT_POT, options.get(Config.DEFAULT_POT) + options.get(Config.DEFAULT_RESET_ADD_POT));	
		options.set(Config.DEFAULT_COOLDOWN, options.get(Config.DEFAULT_COOLDOWN) + options.get(Config.DEFAULT_RESET_ADD_COOLDOWN));
		options.set(Config.DEFAULT_MAX_TICKETS, options.get(Config.DEFAULT_MAX_TICKETS) + options.get(Config.DEFAULT_RESET_ADD_MAX_TICKETS));	
		options.set(Config.DEFAULT_MAX_PLAYERS, options.get(Config.DEFAULT_MAX_PLAYERS) + options.get(Config.DEFAULT_RESET_ADD_MAX_PLAYERS));			
		options.set(Config.DEFAULT_MIN_PLAYERS, options.get(Config.DEFAULT_MIN_PLAYERS) + options.get(Config.DEFAULT_RESET_ADD_MIN_PLAYERS));	
		options.set(Config.DEFAULT_TICKET_TAX, options.get(Config.DEFAULT_TICKET_TAX) + options.get(Config.DEFAULT_RESET_ADD_TICKET_TAX));	
		options.set(Config.DEFAULT_POT_TAX, options.get(Config.DEFAULT_POT_TAX) + options.get(Config.DEFAULT_RESET_ADD_POT_TAX));	
		String read = options.get(Config.DEFAULT_RESET_ADD_ITEM_REWARDS);
		for(String line : read.split("\\s+")) {
			ItemStack item = Utils.loadItemStack(line);
			if(item != null)
				rewards.add(new ItemReward(item));
		}
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String toString() {
		return lotteryName;
	}
}
