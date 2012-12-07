package com.randude14.lotteryplus.lottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.ClaimManager;
import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.Utils;
import com.randude14.lotteryplus.WinnersManager;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.register.economy.Economy;
import com.randude14.lotteryplus.register.economy.MaterialEconomy;
import com.randude14.lotteryplus.register.economy.VaultEconomy;
import com.randude14.lotteryplus.util.FormatOptions;
import com.randude14.lotteryplus.util.TimeConstants;

public class Lottery implements FormatOptions, TimeConstants, Runnable {
	private final LotteryTimer timer;
	private final Map<String, Long> cooldowns;
	private final List<Reward> rewards;
	private final List<Sign> signs;
	private final String lotteryName;
	private final Random rand;
	private LotteryOptions options;
	private Economy econ;
	private boolean success;
	private int drawId;

	public Lottery(String name) {
		this.cooldowns = new HashMap<String, Long>();
		this.rewards = new ArrayList<Reward>();
		this.signs = new ArrayList<Sign>();
		this.lotteryName = name;
		this.timer = new LotteryTimer(this);
		this.rand = new Random();
		success = false;
	}

	public LotteryOptions getOptions() {
		return options;
	}

	public final String getName() {
		return lotteryName;
	}

	public boolean isDrawing() {
		return options.getBoolean("drawing", false);
	}

	public boolean isRunning() {
		return timer.isRunning();
	}

	public boolean isItemOnly() {
		int num = 0;
		for (Reward reward : rewards) {
			if (reward instanceof ItemReward)
				num++;
		}
		return options.getBoolean(Config.DEFAULT_ITEM_ONLY) && num > 0;
	}
	
	public Economy getEconomy() {
		return econ;
	}

	//called every second
	public void onTick() {
		timer.onTick();
		if (timer.isOver()) {
			this.draw();
			return;
		}
		printWarningTimes();
		updateSigns();
		updateCooldowns();
	}
	
	private void printWarningTimes() {
		String line = options.getString(Config.DEFAULT_WARNING_TIMES);
		if(line != null && !line.isEmpty()) {
			for(String timeStr : line.split("\\s+")) {
				int len = timeStr.length();
				if(len == 0) {
					continue;
				}
				try {
					long time = Long.parseLong(timeStr.substring(0, len-1));
					String timeMess = time + " ";
					char c = Character.toLowerCase(timeStr.charAt(len-1));
					switch(c) {
					case 'w':
						time = WEEK * time;
						timeMess += "week(s)";
					    break;
					case 'd':
						time = DAY * time;
						timeMess += "day(s)";
					    break;
					case 'h':
						time = HOUR * time;
						timeMess += "hour(s)";
					    break;
					case 'm':
						time = MINUTE * time;
						timeMess += "minute(s)";
					    break;
					default:
						//no need to do anything with time, already in seconds
						timeMess += "second(s)";
						break;
					}
					if(timer.getTime() == time) {
						String message = Config.getString(Config.WARNING_MESSAGE).replace("<name>", lotteryName).replace("<time>", timeMess);
						ChatUtils.broadcastRaw(message);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void updateSigns() {
		updateSigns(false);
	}
	
	private void updateSigns(boolean over) {
		String line1 = ChatUtils.replaceColorCodes(Config.getString(Config.SIGN_TAG));
		String line2, line3, line4;
		if(over) {
			line2 = ChatUtils.replaceColorCodes(format(Config.getString(Config.OVER_SIGN_LINE_TWO)));
			line3 = ChatUtils.replaceColorCodes(format(Config.getString(Config.OVER_SIGN_LINE_THREE)));
			line4 = ChatUtils.replaceColorCodes(format(Config.getString(Config.OVER_SIGN_LINE_FOUR)));
		} else {
			if(isDrawing()) {
				line2 = ChatUtils.replaceColorCodes(format(Config.getString(Config.DRAWING_SIGN_LINE_TWO)));
				line3 = ChatUtils.replaceColorCodes(format(Config.getString(Config.DRAWING_SIGN_LINE_THREE)));
				line4 = ChatUtils.replaceColorCodes(format(Config.getString(Config.DRAWING_SIGN_LINE_FOUR)));
			} else {
				line2 = ChatUtils.replaceColorCodes(format(Config.getString(Config.UPDATE_SIGN_LINE_TWO)));
				line3 = ChatUtils.replaceColorCodes(format(Config.getString(Config.UPDATE_SIGN_LINE_THREE)));
				line4 = ChatUtils.replaceColorCodes(format(Config.getString(Config.UPDATE_SIGN_LINE_FOUR)));
			}
		}
		for(Sign sign : signs) {
			sign.setLine(0, line1);
			sign.setLine(1, line2);
			sign.setLine(2, line3);
			sign.setLine(3, line4);
			sign.update(true);
		}
	}
	
	private void updateCooldowns() {
		Iterator<String> it = cooldowns.keySet().iterator();
		while(it.hasNext()) {
			long cooldown = cooldowns.get(it.next());
			if(cooldown-- <= 0) {
				it.remove();
			}
		}
	}

	public String format(String mess) {
		String winner = options.getString("winner", "");
		return mess
				.replace(FORMAT_REWARD, formatReward())
				.replace(FORMAT_TIME, timer.format())
				.replace(FORMAT_NAME, lotteryName)
				.replace(FORMAT_WINNER,
						(!winner.isEmpty()) ? winner : "no winner yet")
				.replace(
						FORMAT_TICKET_COST,
						econ.format(options
								.getDouble(Config.DEFAULT_TICKET_COST)))
				.replace(
						FORMAT_TICKET_TAX,
						String.format("%,.2f",
								options.getDouble(Config.DEFAULT_TICKET_TAX)))
				.replace(
						FORMAT_POT_TAX,
						String.format("%,.2f",
								options.getDouble(Config.DEFAULT_POT_TAX)));
	}

	private String formatReward() {
		if (!isItemOnly())
			return econ.format(options.getDouble(Config.DEFAULT_POT));
		int num = 0;
		for (int cntr = 0; cntr < rewards.size(); cntr++) {
			if (rewards instanceof ItemReward)
				num++;
		}
		return num + " item(s)";
	}

	public synchronized boolean addToPot(CommandSender sender, double add) {
		if (isItemOnly()) {
			ChatUtils.error(sender, "%s does not have a pot.", lotteryName);
			return false;
		}
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(!econ.hasAccount(player)) {
				ChatUtils.error(player, "You do not have an account with your server's economy.");
				return false;
			}
			if(!econ.hasEnough(player, add)) {
				ChatUtils.error(player, "You do not have enough %s.", econ.format(add));
				return false;
			}
			econ.withdraw(player, add);
		}
		double pot = options.getDouble(Config.DEFAULT_POT);
		options.set(Config.DEFAULT_POT, pot + add);
		ChatUtils
				.send(sender, ChatColor.GOLD,
						"%s %shas been added to %s%s's %spot.",
						econ.format(add), ChatColor.YELLOW, ChatColor.GOLD,
						lotteryName, ChatColor.YELLOW);
		return true;
	}

	public void setOptions(LotteryOptions options) throws Exception {
		setOptions(options, false);
	}

	public void setOptions(LotteryOptions options, boolean force)
			throws Exception {
		try {
			// CHECK FOR NEGATIVE OPTIONS
			double time = options.getDouble(Config.DEFAULT_TIME);
			double pot = options.getDouble(Config.DEFAULT_POT);
			double ticketCost = options.getDouble(Config.DEFAULT_TICKET_COST);
			Validate.isTrue(time >= 0, "Time cannot be negative: " + time);
			Validate.isTrue(pot >= 0.0, "Pot cannot be negative: " + pot);
			Validate.isTrue(ticketCost >= 0.0, "Ticket Cost cannot be negative: "
					+ ticketCost);

			if (force) {
				rewards.clear();
			} else {
				transfer(this.options, options);
				signs.clear();
				int cntr = 1;
				while(options.contains("sign" + cntr)) {
					String str = options.getString("sign" + cntr);
					Location loc = Utils.parseToLocation(str);
					if(loc != null) {
						Block block = loc.getBlock();
						if(Plugin.isSign(block)) {
							Sign sign = (Sign) block.getState();
							signs.add(sign);
						} else {
							Logger.info("'%s' is no longer a sign." + str);
						}
					} else {
						Logger.info("'%s' is a corrup location." + str);
					}
					cntr++;
				}
			}

			this.options = options;
			
			//ECONOMY
			econ = null;
			if(options.getBoolean(Config.DEFAULT_USE_VAULT)) {
				if(VaultEconomy.isVaultInstalled()) {
					econ = new VaultEconomy();
				}
			} else {
				int materialID = Config.getInt(Config.DEFAULT_MATERIAL_ID);
				String name = Config.getString(Config.DEFAULT_MATERIAL_NAME);
				econ = new MaterialEconomy(materialID, name);
			}
			
			if(econ == null) {
				throw new NullPointerException("Failed to load an economy.");
			}

			// SET SEED FOR RANDOM
			rand.setSeed(Utils.loadSeed(options.getString(Config.DEFAULT_SEED)));

			// LOAD ITEM REWARDS
			String read = options.getString(Config.DEFAULT_ITEM_REWARDS);
			if (!(read == null || read.equals(""))) {
				for(ItemStack item : Utils.getItemStacks(read)) {
					rewards.add(new ItemReward(item));
				}
			}

			// LOAD TIME
			timer.setRunning(true);
			if (options.contains("save-time") && options.contains("reset-time")) {
				long saveTime = options.getLong("save-time", 0L);
				timer.setTime(saveTime);
				long resetTime = options.getLong("reset-time", 0L);
				timer.setResetTime(resetTime);
			} else {
				long t = (long) Math.floor(time * (double)HOUR);
				timer.setTime(t);
				timer.setResetTime(t);
			}
		} catch (Exception ex) {
			throw new InvalidLotteryException("Failed to load options", ex);
		}
	}

	private void transfer(LotteryOptions oldOptions, LotteryOptions newOptions) {
		if (oldOptions == null || success) {
			rewards.clear();
			return;
		}
		if (!oldOptions.getBoolean(Config.DEFAULT_CLEAR_POT)) {
			double pot = newOptions.getDouble(Config.DEFAULT_POT);
			newOptions.set(Config.DEFAULT_POT,
					pot + oldOptions.getDouble(Config.DEFAULT_POT));
		}
		if (oldOptions.getBoolean(Config.DEFAULT_CLEAR_REWARDS)) {
			rewards.clear();
		}
		if(oldOptions.getBoolean(Config.DEFAULT_KEEP_TICKETS)) {
			for(String player : getPlayers()) {
				newOptions.set("players." + player, oldOptions.getInt("players." + player, 0));
			}
		}
	}

	public void save() {
		options.set("save-time", timer.getTime());
		options.set("reset-time", timer.getResetTime());
		options.remove("drawing");
		int cntr = 1;
		for(Sign sign : signs) {
			options.set("sign" + cntr++, Utils.parseLocation(sign.getLocation()));
		}
	}
	
	public void registerSign(Sign sign) {
		signs.add(sign);
	}
	
	public boolean hasRegisteredSign(Block block) {
		for(Sign s : signs) {
			if(Plugin.locsInBounds(block.getLocation(), s.getLocation())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasRegisteredSign(Sign sign) {
		for(Sign s : signs) {
			if(Plugin.locsInBounds(sign.getLocation(), s.getLocation())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean unregisterSign(Sign sign) {
		for(int cntr = 0;cntr < signs.size();cntr++) {
			Sign s = signs.get(cntr);
			if(Plugin.locsInBounds(sign.getLocation(), s.getLocation())) {
				signs.remove(cntr);
				return true;
			}
		}
		return false;
	}
	
	public void broadcast(String player, int tickets) {
		String message = Config.getString(Config.BUY_MESSAGE);
		message = message.replace("<player>", player)
				.replace("<tickets>", "" + tickets)
				.replace("<lottery>", lotteryName);
		ChatUtils.broadcast(message);
	}
	
	private boolean canBuy(Player player, int tickets) {
		if (isDrawing() && !Config.getBoolean(Config.BUY_DURING_DRAW)) {
			ChatUtils.errorRaw(player, "Cannot buy tickets during a drawing.");
			return false;
		}
		int ticketLimit = options.getInt(Config.DEFAULT_TICKET_LIMIT);
		if (ticketLimit > 0 && getPlayers().size() >= ticketLimit) {
			ChatUtils.errorRaw(player, "All tickets have been sold.");
			return false;
		}
		String name = player.getName();
		int maxTickets = options.getInt(Config.DEFAULT_MAX_TICKETS);
		int maxPlayers = options.getInt(Config.DEFAULT_MAX_PLAYERS);
		int playersEntered = getPlayersEntered();
		int num = getTicketsBought(name);
		if (maxTickets > 0) {
			if (num >= maxTickets) {
				ChatUtils.errorRaw(player, "You cannot buy anymore tickets.");
				return false;
			} else if (num + tickets > maxTickets) {
				ChatUtils.errorRaw(player, "You cannot buy this many tickets.");
				return false;
			}
		}
		if(maxPlayers > 0) {
			if (playersEntered >= maxPlayers) {
				ChatUtils
						.errorRaw(player, "Cannot fit anymore people in this lottery");
				return false;
			}
		}
		if(cooldowns.containsKey(name)) {
			ChatUtils.error(player, "You must wait %d seconds before purchasing another ticket.", cooldowns.get(name));
			return false;
		}
		return true;
	}

	public synchronized boolean buyTickets(Player player, int tickets) {
		String name = player.getName();
		if (!canBuy(player, tickets)) {
			return false;
		}
		if (!econ.hasAccount(name)) {
			ChatUtils.errorRaw(player,
					"You do not have an account with your server's economy.");
			return false;
		}
		String taxAccount = options.getString(Config.DEFAULT_TAX_ACCOUNT);
		double ticketCost = options.getDouble(Config.DEFAULT_TICKET_COST);
		double total = ticketCost * (double) tickets;
		double ticketTax = options.getDouble(Config.DEFAULT_TICKET_TAX);
		double add = ticketCost - (ticketCost * (ticketTax / 100));
		double d = add * (double) tickets;
		double taxes = total - d;
		if (!econ.hasEnough(name, total)) {
			ChatUtils
					.errorRaw(player,
							"You do not have enough money to buy this amount of tickets.");
			return false;
		}
		econ.withdraw(name, total);
		if(taxAccount != null && econ.hasAccount(taxAccount)) {
			econ.deposit(taxAccount, taxes);
		}
		int num = options.getInt("players." + name, 0);
		options.set("players." + name, num + tickets);
		ChatUtils.sendRaw(player, ChatColor.YELLOW,
				"You have bought %s%d ticket(s) %sfor %s%s.", ChatColor.GOLD,
				tickets, ChatColor.YELLOW, ChatColor.GOLD, lotteryName);
		if (!isItemOnly()) {
			ChatUtils.sendRaw(player, ChatColor.GOLD,
					"%s %shas been added to %s%s's %spot.", econ.format(d),
					ChatColor.YELLOW, ChatColor.GOLD, lotteryName,
					ChatColor.YELLOW);
			options.set(Config.DEFAULT_POT,
					options.getDouble(Config.DEFAULT_POT) + d);
		}
		long cooldown = options.getLong(Config.DEFAULT_COOLDOWN);
		long time = timer.getTime() - cooldown;
		timer.setTime(time);
		return true;
	}

	public synchronized boolean rewardPlayer(CommandSender rewarder, String player,
			int tickets) {
		int ticketLimit = options.getInt(Config.DEFAULT_TICKET_LIMIT);
		int num = options.getInt("players." + player, 0);
		if (ticketLimit > 0) {
			int players = getPlayers().size();
			if (players >= ticketLimit) {
				ChatUtils.error(rewarder, "All tickets are sold out for %s.",
						lotteryName);
				return false;
			}
			if (tickets + num > ticketLimit) {
				ChatUtils
						.error(rewarder,
								"Cannot reward this many tickets");
				return false;
			}
		}
		options.set("players." + player, num + tickets);
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			ChatUtils.send(p, ChatColor.YELLOW,
					"You have been rewarded %s%d ticket(s)%s for %s%s.",
					ChatColor.GOLD, tickets, ChatColor.YELLOW, ChatColor.GOLD,
					lotteryName);
		}
		return true;
	}

	public boolean isOver() {
		int ticketLimit = options.getInt(Config.DEFAULT_TICKET_LIMIT);
		if(ticketLimit <= 0) {
			return false;
		}
		int players = getPlayers().size();
		if(players < ticketLimit) {
			return false;
		}
		int minPlayers = options.getInt(Config.DEFAULT_MIN_PLAYERS);
		if(minPlayers <= 0) return false;
		int entered = getPlayersEntered();
		return entered >= minPlayers && entered >= 1;
	}

	public void sendInfo(CommandSender sender) {
		ChatUtils.sendRaw(sender, ChatColor.YELLOW,
				"- Time Left: " + timer.format());
		ChatUtils
				.sendRaw(sender, ChatColor.YELLOW, "- Drawing: " + isDrawing());
		if (!isItemOnly()) {
			ChatUtils.sendRaw(
					sender,
					ChatColor.YELLOW,
					"- Pot: "
							+ econ.format(options
									.getDouble(Config.DEFAULT_POT)));
		}
		for (Reward reward : rewards) {
			ChatUtils
					.sendRaw(sender, ChatColor.YELLOW, "- " + reward.getInfo());
		}
		ChatUtils.sendRaw(
				sender,
				ChatColor.YELLOW,
				"- Ticket Cost: "
						+ econ.format(options
								.getDouble(Config.DEFAULT_TICKET_COST)));
		ChatUtils.sendRaw(
				sender,
				ChatColor.YELLOW,
				"- Ticket Tax: " + String.format("%,.2f", options.getDouble(Config.DEFAULT_TICKET_TAX)));
		ChatUtils.sendRaw(
				sender,
				ChatColor.YELLOW,
				"- Pot Tax: " + String.format("%,.2f", options.getDouble(Config.DEFAULT_POT_TAX)));
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Players Entered: "
				+ getPlayersEntered());
		ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Tickets Left: "
				+ formatTicketsLeft());
		if (sender instanceof Player)
			ChatUtils.sendRaw(sender, ChatColor.YELLOW, "- Tickets Bought: "
					+ getTicketsBought(sender.getName()));
	}

	private String formatTicketsLeft() {
		int ticketLimit = options.getInt(Config.DEFAULT_TICKET_LIMIT);
		if (ticketLimit <= 0)
			return "no limit";
		int left = ticketLimit - getPlayers().size();
		return (left > 0) ? "" + left : "none";
	}

	public int getPlayersEntered() {
		Set<String> players = new HashSet<String>();
		for (String key : options.keySet()) {
			if (key.startsWith("players.")) {
				int index = key.indexOf('.');
				String player = key.substring(index + 1);
				int num = options.getInt(key, 0);
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
			if (key.startsWith("players.")) {
				int index = key.indexOf('.');
				String player = key.substring(index + 1);
				int num = options.getInt(key, 0);
				for (int cntr = 0; cntr < num; cntr++) {
					players.add(player);
				}
			}
		}
		return players;
	}

	public int getTicketsBought(String name) {
		return options.getInt("players." + name, 0);
	}

	public long getTime() {
		return timer.getTime();
	}
	
	public void setTime(long newTime) {
		timer.setTime(newTime);
		timer.setResetTime(newTime);
	}
	
	public void draw() {
		draw(null);
	}

	// sender: sender that initiated force draw, may be null if drawing was done
	// 'naturally'
	public synchronized void draw(CommandSender sender) {
		if (options.getBoolean("drawing", false)) {
			if (sender != null) {
				ChatUtils.error(sender, "%s is already drawing.", lotteryName);
			}
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
		long delay = Config.getLong(Config.DRAW_DELAY);
		drawId = Plugin.scheduleAsyncDelayedTask(this, delay * SERVER_SECOND);
		timer.setRunning(false);
		options.set("drawing", true);
		new SignUpdateTask(this);
	}

	public synchronized void cancelDrawing() {
		Plugin.cancelTask(drawId);
	}

	private void clearPlayers() {
		List<String> keys = new ArrayList<String>(options.keySet());
		for (int cntr = 0; cntr < keys.size(); cntr++) {
			String key = keys.get(cntr);
			if (key.startsWith("players.")) {
				options.remove(key);
			}
		}
	}

	public void run() {
		try {
			drawId = -1;
			List<String> players = this.getPlayers();
			int len = players.size();
			if (len < options.getInt(Config.DEFAULT_MIN_PLAYERS) || len < 1) {
				ChatUtils
						.broadcast("No one! There were not enough players entered. Restarting lottery.");
				readResetData();
				options.set("drawing", false);
				return;
			}
			String winner = pickRandomPlayer(rand, players,
					options.getInt(Config.DEFAULT_TICKET_LIMIT));
			if (winner == null) {
				ChatUtils
						.broadcast("No one won the lottery. Better luck next time!");
				options.set("drawing", false);
				success = false;
				LotteryManager.reloadLottery(lotteryName);
				return;
			}
			options.set("winner", winner);
			ChatUtils.broadcast("%s%s!", ChatColor.GOLD, winner);
			if (!this.isItemOnly()) {
				double pot = options.getDouble(Config.DEFAULT_POT);
				double potTax = options.getDouble(Config.DEFAULT_POT_TAX);
				double winnings = pot - (pot * (potTax / 100));
				rewards.add(0, new PotReward(econ, winnings));
			}
			StringBuilder logWinner = new StringBuilder(lotteryName + ": " + winner);
			for (Reward reward : rewards) {
				logWinner.append(", ");
				logWinner.append("[" + reward.getInfo() + "]");
			}
			clearPlayers();
			WinnersManager.logWinner(logWinner.toString());
			Player pWinner = Bukkit.getPlayer(winner);

			if (pWinner != null) {
				handleRewards(rewards, pWinner);
			} else {
				ClaimManager.addClaim(winner, lotteryName, rewards);
			}
			drawId = -1;
			options.set("drawing", false);
			success = true;
			if (options.getBoolean(Config.DEFAULT_REPEAT)) {
				LotteryManager.reloadLottery(lotteryName);
			} else {
				LotteryManager.unloadLottery(lotteryName);
				new SignUpdateTask(this, true);
			}
		} catch (Exception ex) {
			Logger.warning("Error occurred while drawing %s.", lotteryName);
			options.set("drawing", false);
			success = false;
			ex.printStackTrace();
		}
	}

	private static String pickRandomPlayer(Random rand, List<String> players,
			int ticketLimit) {
		if (ticketLimit <= 0) {
			return players.get(rand.nextInt(players.size()));
		} else {
			List<Integer> spots = new ArrayList<Integer>();
			for(int i = 0;i < ticketLimit;i++)
				spots.add(i);
			Map<Integer, String> map = new HashMap<Integer, String>();
			while(!players.isEmpty()) {
				int index = spots.remove(rand.nextInt(spots.size()));
				String player = players.remove(rand.nextInt(players.size()));
				map.put(index, player);
			}
			int winningNumber = rand.nextInt(ticketLimit);
			return map.get(winningNumber);
		}
	}

	public static void handleRewards(List<Reward> rewards, Player player) {
		for (Reward reward : rewards) {
			reward.rewardPlayer(player);
		}
	}

	private void readResetData() {
		long time = options.getLong(Config.DEFAULT_RESET_ADD_TIME);
		this.timer.setResetTime(timer.getResetTime() + time);
		options.set(
				Config.DEFAULT_TICKET_COST,
				options.getDouble(Config.DEFAULT_TICKET_COST)
						+ options
								.getDouble(Config.DEFAULT_RESET_ADD_TICKET_COST));
		options.set(Config.DEFAULT_POT, options.getDouble(Config.DEFAULT_POT)
				+ options.getDouble(Config.DEFAULT_RESET_ADD_POT));
		options.set(
				Config.DEFAULT_COOLDOWN,
				options.getLong(Config.DEFAULT_COOLDOWN)
						+ options.getLong(Config.DEFAULT_RESET_ADD_COOLDOWN));
		options.set(
				Config.DEFAULT_MAX_TICKETS,
				options.getInt(Config.DEFAULT_MAX_TICKETS)
						+ options.getInt(Config.DEFAULT_RESET_ADD_MAX_TICKETS));
		options.set(
				Config.DEFAULT_MAX_PLAYERS,
				options.getInt(Config.DEFAULT_MAX_PLAYERS)
						+ options.getInt(Config.DEFAULT_RESET_ADD_MAX_PLAYERS));
		options.set(
				Config.DEFAULT_MIN_PLAYERS,
				options.getInt(Config.DEFAULT_MIN_PLAYERS)
						+ options.getInt(Config.DEFAULT_RESET_ADD_MIN_PLAYERS));
		options.set(
				Config.DEFAULT_TICKET_TAX,
				options.getDouble(Config.DEFAULT_TICKET_TAX)
						+ options
								.getDouble(Config.DEFAULT_RESET_ADD_TICKET_TAX));
		options.set(
				Config.DEFAULT_POT_TAX,
				options.getDouble(Config.DEFAULT_POT_TAX)
						+ options.getDouble(Config.DEFAULT_RESET_ADD_POT_TAX));
		String read = options.getString(Config.DEFAULT_RESET_ADD_ITEM_REWARDS);
		if(read != null && !read.isEmpty()) {
			for(ItemStack item : Utils.getItemStacks(read)) {
				rewards.add(new ItemReward(item));
			}
		}
		this.timer.reset();
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public String toString() {
		return lotteryName;
	}
	
	private static class SignUpdateTask implements Runnable {
		private final Lottery lottery;
		private final boolean over;
		
		public SignUpdateTask(final Lottery lottery) {
			this(lottery, false);
		}
		
		public SignUpdateTask(final Lottery lottery, final boolean over) {
			this.lottery = lottery;
			this.over = over;
			Plugin.scheduleAsyncDelayedTask(this, 0);
		}
		
		public void run() {
			lottery.updateSigns(over);
		}
	}
}
