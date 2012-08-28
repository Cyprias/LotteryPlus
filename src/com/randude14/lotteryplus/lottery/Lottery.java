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
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.Utils;
import com.randude14.lotteryplus.WinnersManager;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.util.FormatOptions;
import com.randude14.lotteryplus.util.SignFormatter;
import com.randude14.lotteryplus.util.TimeConstants;

public class Lottery implements FormatOptions, TimeConstants, Runnable {
	private final LotteryTimer timer;
	private final List<Reward> rewards;
	private final List<Sign> signs;
	private final String lotteryName;
	private final Random rand;
	private final SignFormatter formatter;
	private LotteryOptions options;
	private int drawId;

	public Lottery(String name) {
		this.rewards = new ArrayList<Reward>();
		this.signs = Collections.synchronizedList(new ArrayList<Sign>());
		this.lotteryName = name;
		this.timer = new LotteryTimer(this);
		this.rand = new Random();
		this.formatter = new LotterySignFormatter(this);
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
	
	public void onTick() {
		timer.onTick();
		if(timer.isOver()) {
			this.draw();
		}
	}
	
	public String format(String mess) {
		String winner = options.get("winner", "");
		return mess.replace(FORMAT_REWARD, formatReward())
				.replace(FORMAT_TIME, timer.format())
				.replace(FORMAT_NAME, lotteryName)
				.replace(FORMAT_WINNER, (!winner.isEmpty()) ? winner : "no winner yet")
				.replace(FORMAT_TICKET_COST, Utils.format(options.get(Config.DEFAULT_TICKET_COST)))
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
		setOptions(options, false);
	}
	
	public boolean addToPot(CommandSender sender, double add) {
		if(isItemOnly()) {
			ChatUtils.error(sender, "%s does not have a pot.", lotteryName);
			return false;
		}
		double pot = options.get(Config.DEFAULT_POT);
		options.set(Config.DEFAULT_POT, pot + add);
		ChatUtils.send(sender, ChatColor.GOLD, "%s %shas been added to %s%s's %spot.", Utils.format(add), ChatColor.YELLOW, ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		return true;
	}

	public void setOptions(LotteryOptions options, boolean reloadSigns) throws InvalidLotteryException {
		//CHECK FOR NEGATIVE OPTIONS
		this.isPositive("Pot cannot be negative", options.get(Config.DEFAULT_POT));
		this.isPositive("Ticket Cost cannot be negative", options.get(Config.DEFAULT_TICKET_COST));
		this.isPositive("Pot cannot be negative", options.get(Config.DEFAULT_TIME));
		this.isPositive("Min Players cannot be negative", options.get(Config.DEFAULT_MIN_PLAYERS));
		
		this.options = options;
		rewards.clear();
		
		if(reloadSigns) {
			synchronized(signs) {
				signs.clear();
				if(options.contains("signs")) {
					String line = options.get("signs", "");
					for(String str : line.split("\\s+")) {
						Location loc = Utils.parseToLocation(str);
						if(loc != null && Plugin.isSign(loc))
							signs.add((Sign) loc.getBlock().getState());
					}
				}
			}
		}
		
		//SET SEED FOR RANDOM
		rand.setSeed(Utils.loadSeed(options.get(Config.DEFAULT_SEED)));
		
		//LOAD ITEM REWARDS
		String read = options.get(Config.DEFAULT_ITEM_REWARDS);
		if(!(read == null || read.equals(""))) {
			for(String line : read.split("\\s+")) {
				ItemStack item = Utils.loadItemStack(line);
				if(item != null) {
					rewards.add(new ItemReward(item));
				}
			}
		}
		
		//LOAD TIME
		timer.setRunning(true);
		if(options.contains("save-time") && options.contains("reset-time")) {
			long saveTime = options.get("save-time", 0L);
			timer.setTime(saveTime);
			long resetTime = options.get("reset-time", 0L);
			timer.setResetTime(resetTime);
		} else {
			long time = (long)Math.floor(((double)options.get(Config.DEFAULT_TIME)) * HOUR);
			timer.setTime(time);
			timer.setResetTime(time);
		}
	}
	
	private void isPositive(String message, double d) {
		if(d <= 0.0) {
			throw new InvalidLotteryException(message + ": " + d);
		}
	}
	
	public void save() {
		options.set("save-time", (Long)timer.getTime());
		options.set("reset-time", (Long)timer.getResetTime());
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
			ChatUtils.errorRaw(player, "You cannot buy this many tickets.");
			return false;
		}
		Economy econ = Plugin.getEconomy();
		String name = player.getName();
		if (!econ.hasAccount(name)) {
			ChatUtils.errorRaw(player, "You do not have an account with your server's economy.");
			return false;
		}
		double ticketCost = options.get(Config.DEFAULT_TICKET_COST);
		double sub = ticketCost * (double) tickets;
		if (!econ.has(name, sub)) {
			ChatUtils.errorRaw(player, "You do not have enough money to buy this amount of tickets.");
			return false;
		}
		econ.withdrawPlayer(name, sub);
		int num = options.get("players." + name, 0);
		options.set("players." + name, num + tickets);
		double ticketTax = options.get(Config.DEFAULT_TICKET_TAX);
		double add = ticketCost - (ticketCost * (ticketTax / 100));
		double d = add * (double) tickets;
		ChatUtils.sendRaw(player, ChatColor.YELLOW,
				"You have bought %s%d ticket(s) %sfor %s%s.", ChatColor.GOLD,
				tickets, ChatColor.YELLOW, ChatColor.GOLD, lotteryName);
		ChatUtils
				.sendRaw(player, ChatColor.GOLD,
						"%s %shas been added to %s%s's %spot.",
						Utils.format(d), ChatColor.YELLOW, ChatColor.GOLD,
						lotteryName, ChatColor.YELLOW);
		options.set(Config.DEFAULT_POT, options.get(Config.DEFAULT_POT) + d);
		long cooldown = options.get(Config.DEFAULT_COOLDOWN);
		long time = timer.getTime() - cooldown;
		timer.setTime(time);
		return true;
	}
	
	public void rewardPlayer(String player, int tickets) {
		int num = options.get("players." + player, 0);
		options.set("players." + player, num + tickets);
		Player p = Bukkit.getPlayer(player);
		if(p != null) {
			ChatUtils.send(p, ChatColor.YELLOW, "You have been rewarded %s%d ticket(s)%s for %s%s.", ChatColor.GOLD, tickets, ChatColor.YELLOW, ChatColor.GOLD, lotteryName);
		}
	}
	
	public boolean isOver() {
		int maxPlayers = options.get(Config.DEFAULT_MAX_PLAYERS);
		int maxTickets = options.get(Config.DEFAULT_MAX_TICKETS);
		List<String> players = getPlayers();
		if(players.size() < maxPlayers) {
			return false;
		}
		for(String player : players) {
			int num = options.get("players." + player, 0);
			if(num < maxTickets)
				return false;
		}
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
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Time Left: " + timer.format());
		if (!isItemOnly()) {
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Pot: " + Utils.format(options.get(Config.DEFAULT_POT)));
		}
		for (Reward reward : rewards) {
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- " + reward.getInfo());
		}
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Ticket Cost: " + Utils.format(options.get(Config.DEFAULT_TICKET_COST)));
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Ticket Tax: " + Utils.format(options.get(Config.DEFAULT_TICKET_TAX)));
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Pot Tax: " + Utils.format(options.get(Config.DEFAULT_POT_TAX)));
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Players Entered: " + getPlayersEntered());
		if(sender instanceof Player)
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Tickets Bought: " + getTicketsBought(sender.getName()));
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

	//sender: sender that initiated force draw, may be null if drawing was done 'naturally'
	public synchronized void draw(CommandSender sender) {
		if (options.get("drawing", false)) {
			return;
		}
		if (sender == null) {
			ChatUtils.broadcast("%s%s %sis ending, and the winner is...",
					ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		} else {
			ChatUtils.broadcast(
					"%s%s %sis force drawing %s%s, %sand the winner is...",
					ChatColor.GOLD, sender.getName(), ChatColor.YELLOW,
					ChatColor.GOLD, lotteryName, ChatColor.YELLOW);
		}
		drawId = Plugin.scheduleSyncDelayedTask(this, SERVER_SECOND * 3);
		timer.setRunning(false);
		updateSigns();
		options.set("drawing", true);
	}
	
	public synchronized void cancelDrawing() {
		Plugin.cancelTask(drawId);
	}
	
	private void clearPlayers() {
		List<String> keys = new ArrayList<String>(options.keySet());
		for(int cntr = 0;cntr < keys.size();cntr++) {
			String key = keys.get(cntr);
			if(key.startsWith("players")) {
				options.remove(key);
			}
		}
	}

	public void run() {
		drawId = -1;
		List<String> players = this.getPlayers();
		int len = players.size();
		if (len < options.get(Config.DEFAULT_MIN_PLAYERS) || len < 1) {
			ChatUtils
					.broadcast("No one! There were not enough players entered. Restarting lottery.");
			readResetData();
			options.set("drawing", false);
			return;
		}
		Collections.shuffle(players, rand);
		String winner = players.get(rand.nextInt(players.size()));
		options.set("winner", winner);
		ChatUtils.broadcast("%s%s!", ChatColor.GOLD, winner);
		StringBuilder logWinner = new StringBuilder(lotteryName + ": " + winner);
		if (!this.isItemOnly()) {
			double pot = options.get(Config.DEFAULT_POT);
			double potTax = options.get(Config.DEFAULT_POT_TAX);
			double winnings = pot - (pot * (potTax / 100));
			rewards.add(0, new PotReward(winnings));
		}
		for (Reward reward : rewards) {
			logWinner.append(", ");
			logWinner.append(reward);
		}
		clearPlayers();
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
		if(Config.getProperty(Config.DEFAULT_REPEAT)) {
			LotteryManager.reloadLottery(lotteryName);
		} else {
			LotteryManager.unloadLottery(lotteryName);
		}
	}

	private void readResetData() {
		long time = options.get(Config.DEFAULT_RESET_ADD_TIME);		
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
		this.timer.reset();
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String toString() {
		return lotteryName;
	}
}
