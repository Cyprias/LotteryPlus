package com.randude14.lotteryplus.lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.util.TimeConstants;

public class Lottery implements TimeConstants, Runnable {
	private static final long INTERVAL = HOUR;
	private final Plugin plugin;
	private final LotteryTimer timer;
	private final List<ItemStack> itemRewards;
	private final List<LotterySign> signs;
	private final List<String> players;
	private final String name;
	private boolean repeat;
	private double ticketCost;
	private double pot;
	private boolean itemOnly;
	private int maxTickets;
	private int maxPlayers;
	private int minPlayers;

	public Lottery(Plugin plugin, String name, long time, double pot,
			double ticketCost, boolean repeat, boolean itemOnly, int mt,
			int maxp, int minp) {
		this.plugin = plugin;
		this.name = name;
		this.pot = pot;
		this.ticketCost = ticketCost;
		this.itemRewards = new ArrayList<ItemStack>();
		this.players = new ArrayList<String>();
		this.signs = new ArrayList<LotterySign>();
		this.timer = new LotteryTimer(this, time * INTERVAL);
		this.repeat = repeat;
		this.itemOnly = itemOnly;
		this.maxTickets = mt;
		this.maxPlayers = maxp;
		this.minPlayers = minp;
		this.init();
	}

	private Lottery(Plugin plugin, Map<String, Object> timerMap,
			List<LotteryLocation> locs, List<String> players, String name,
			boolean repeat, boolean itemOnly, double ticketCost, double pot,
			int mt, int maxp, int minp) {
		this.plugin = plugin;
		this.timer = LotteryTimer.deserialize(timerMap, this);
		signs = new ArrayList<LotterySign>();

		for (LotteryLocation loc : locs) {
			LotterySign sign = LotterySign.deserialize(this, loc);
			signs.add(sign);
		}

		this.itemRewards = new ArrayList<ItemStack>();
		this.players = players;
		this.name = name;
		this.repeat = repeat;
		this.itemOnly = itemOnly;
		this.maxTickets = mt;
		this.maxPlayers = maxp;
		this.minPlayers = minp;
		this.ticketCost = ticketCost;
		this.pot = pot;
		this.init();
	}

	private void init() {
		timer.start();
		updateSigns();
	}

	public void addItemReward(ItemStack item) {
		itemRewards.add(item);
	}

	public Lottery setRepeat(boolean b) {
		repeat = b;
		return this;
	}

	public Lottery setItemOnly(boolean itemOnly) {
		this.itemOnly = itemOnly;
		return this;
	}

	public Lottery setPot(double pot) {
		this.pot = pot;
		return this;
	}

	public Lottery setTicketCost(double ticketCost) {
		this.ticketCost = ticketCost;
		return this;
	}

	public Lottery setTimer(long time) {
		timer.setTime(time * INTERVAL);
		return this;
	}

	public Lottery setMaxTickets(int mt) {
		maxTickets = mt;
		return this;
	}

	public Lottery setMaxPlayers(int maxp) {
		maxPlayers = maxp;
		return this;
	}

	public Lottery setMinPlayers(int minp) {
		minPlayers = minp;
		return this;
	}

	public boolean isItemOnly() {
		return itemOnly && !itemRewards.isEmpty();
	}

	public int getMaxTickets() {
		return maxTickets;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public int getMinPlayers() {
		return minPlayers;
	}

	public List<ItemStack> getItemRewards() {
		return itemRewards;
	}

	public void registerSign(Sign sign) {
		LotterySign lotterySign = new LotterySign(this, sign);
		signs.add(lotterySign);
		lotterySign.update();
	}

	public LotterySign getSignAt(Location location) {

		for (LotterySign lotterySign : signs) {

			if (plugin.locsInBounds(location, lotterySign.getLocation())) {
				return lotterySign;
			}

		}

		return null;
	}

	public boolean unregisterSign(Location location) {

		for (int cntr = 0; cntr < signs.size(); cntr++) {
			LotterySign lotterySign = signs.get(cntr);

			if (plugin.locsInBounds(location, lotterySign.getLocation())) {
				signs.remove(cntr);
				Sign sign = lotterySign.getSign();
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

	public void updateSigns() {

		for (LotterySign sign : signs) {
			sign.update();
		}

	}

	public boolean signAtLocation(Location location) {

		for (LotterySign sign : this.signs) {

			if (plugin.locsInBounds(sign.getLocation(), location)) {
				return true;
			}

		}

		return false;
	}

	public boolean hasPlayerBoughtTicket(String player) {
		return players.contains(player);
	}

	public void playerBought(String player, int tickets) {

		for (int cntr = 0; cntr < tickets; cntr++) {
			players.add(player);
			pot += (isItemOnly()) ? 0 : ticketCost;
		}

	}

	public void addToPot(double add) {
		pot += add;

		for (String name : getPlayers()) {
			Player player = Bukkit.getPlayer(name);

			if (player != null) {
				plugin.send(player, "Lottery's pot has been raised to "
						+ formatPot());
			}

		}

	}

	public double getTicketCost() {
		return ticketCost;
	}

	public void sendInfo(Player player) {
		plugin.send(player, "Time Left: " + timer.format()
				+ " - WW:DD:HH:MM:SS");
		if (!itemOnly) {
			plugin.send(player, "Pot: " + formatPot());
		}
		for (ItemStack itemReward : itemRewards) {
			plugin.send(player, "Item Reward: " + itemReward.getType().name());
		}
		plugin.send(player, "Ticket Cost: " + formatTicketCost());
		plugin.send(player, "Players Entered: " + playersEntered());
		plugin.send(player,
				"Tickets Bought: " + getTicketsBought(player.getName()));
	}

	public int playersEntered() {
		return getPlayers().size();
	}

	public List<String> getPlayers() {
		List<String> list = new ArrayList<String>();

		for (String player : players) {

			if (!list.contains(player)) {
				list.add(player);
			}

		}

		return list;
	}

	public int getTicketsBought(String name) {
		int tickets = 0;

		for (String player : players) {

			if (player.equalsIgnoreCase(name)) {
				tickets++;
			}

		}

		return tickets;
	}

	public String getName() {
		return name;
	}

	public String formatPot() {
		return String.format("$%,.2f", pot);
	}

	public String formatTicketCost() {
		return String.format("$%,.2f", ticketCost);
	}

	public String formatTimer() {
		return timer.format();
	}

	public void stop() {
		timer.setRunning(false);
	}

	public void draw() {

		for (LotterySign sign : signs) {
			sign.draw();
		}

	}

	private void reset() {
		itemRewards.clear();
		plugin.getLotteryManager().resetLottery(this);
		players.clear();
		timer.setRunning(true);
	}

	public void run() {
		int count = playersEntered();

		if (count < minPlayers || count < 1) {
			plugin.getServer()
					.broadcastMessage(
							ChatColor.YELLOW
									+ "[Lottery+] - no one! there were not enough players entered. restarting lottery.");
			timer.reset();
			timer.setRunning(true);
			return;
		}

		Random random = plugin.getRandom();
		Collections.shuffle(players, random);
		String winner = players.get(random.nextInt(players.size()));

		StringBuilder message = new StringBuilder();
		message.append(ChatColor.YELLOW + "[Lottery+] - " + ChatColor.GOLD
				+ winner + "! " + ChatColor.YELLOW + "Player has won ");

		if (isItemOnly()) {

			if (itemRewards.size() == 1) {
				ItemStack itemReward = itemRewards.get(0);
				message.append("a(n) " + ChatColor.GOLD.toString()
						+ itemReward.getType().name());
			}

			else {
				message.append(ChatColor.GOLD.toString() + itemRewards.size()
						+ " items");
			}

		}

		else {
			message.append(ChatColor.GOLD + formatPot());

			if (!itemRewards.isEmpty()) {

				if (itemRewards.size() == 1) {
					ItemStack itemReward = itemRewards.get(0);
					message.append(ChatColor.YELLOW + " and a(n) "
							+ ChatColor.GOLD.toString()
							+ itemReward.getType().name());
				}

				else {
					message.append(ChatColor.YELLOW + " and "
							+ ChatColor.GOLD.toString() + itemRewards.size()
							+ " items");
				}

			}

		}

		message.append("!");
		plugin.getServer()
				.broadcastMessage(ChatColor.GOLD.toString() + message);
		Player pWinner = Bukkit.getPlayer(winner);
		StringBuilder logWinner = new StringBuilder(name + ": " + winner);

		if (isItemOnly()) {

			if (itemRewards.size() == 1) {
				ItemStack itemReward = itemRewards.get(0);
				message.append(", Item Reward(s) - [" + itemReward.getType().name() + "]");
			}

			else {
				message.append(", Item Reward(s) - [" + itemRewards.size() + " items]");
			}

		}

		else {
			logWinner.append(", Winnings - [" + formatPot() + "]");

			if (!itemRewards.isEmpty()) {

				if (itemRewards.size() == 1) {
					ItemStack itemReward = itemRewards.get(0);
					message.append(", Item Reward(s) - [" + itemReward.getType().name() + "]");
				}

				else {
					message.append(", Item Reward(s) - [" + itemRewards.size() + " items]");
				}

			}

		}

		plugin.addWinner(logWinner.toString());
		Economy econ = plugin.getEconomy();

		if (pWinner != null) {

			if (!isItemOnly()) {
				econ.depositPlayer(winner, pot);
			}

			for (int cntr = 0; cntr < itemRewards.size(); cntr++) {
				ItemStack itemReward = itemRewards.get(cntr);

				if (!pWinner.getInventory().addItem(itemReward).isEmpty()) {
					plugin.error(
							pWinner,
							"you did not have enough room in your inventory! type '/lottery claim' to claim your reward");
					plugin.addClaim(winner, name, itemRewards);
					cntr = itemRewards.size();
				}

				else {
					itemRewards.remove(cntr);
					cntr--;
				}

			}

		}

		else {

			if (isItemOnly()) {
				plugin.addClaim(winner, name, itemRewards);
			}

			else {

				if (itemRewards.isEmpty()) {
					plugin.addClaim(winner, name, pot);
				}

				else {
					plugin.addClaim(winner, name, itemRewards, pot);
				}

			}

		}

		if (repeat) {
			reset();
			return;
		}

		for (LotterySign sign : signs) {
			sign.over(winner);
		}

		plugin.getLotteryManager().removeLottery(name);
	}

	public LotteryTimer getTimer() {
		return timer;
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public String toString() {
		return name;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> serialMap = new HashMap<String, Object>();
		List<LotteryLocation> locs = new ArrayList<LotteryLocation>();

		for (LotterySign sign : signs) {
			locs.add(sign.serialize());
		}

		serialMap.put("signs", locs);
		serialMap.put("ticket cost", ticketCost);
		serialMap.put("pot", pot);
		serialMap.put("players", players);
		serialMap.put("timer", timer.serialize());
		serialMap.put("name", name);
		serialMap.put("repeat", repeat);
		serialMap.put("item only", itemOnly);
		serialMap.put("max tickets", maxTickets);
		serialMap.put("max players", maxPlayers);
		serialMap.put("min players", minPlayers);
		int cntr = 1;
		for (ItemStack itemReward : itemRewards) {
			serialMap.put("item reward " + (cntr++), itemReward.serialize());
		}
		timer.interrupt();
		return serialMap;
	}

	@SuppressWarnings("unchecked")
	public static Lottery deserialize(Map<String, Object> serialMap,
			Plugin plugin) {
		double ticketCost = (Double) serialMap.get("ticket cost");
		double pot = (Double) serialMap.get("pot");
		boolean repeat = (Boolean) serialMap.get("repeat");
		boolean itemOnly = (Boolean) serialMap.get("item only");
		int maxTickets = (Integer) serialMap.get("max tickets");
		int maxPlayers = (Integer) serialMap.get("max players");
		int minPlayers = (Integer) serialMap.get("min players");
		String name = (String) serialMap.get("name");
		List<String> players = (List<String>) serialMap.get("players");
		List<LotteryLocation> locs = (List<LotteryLocation>) serialMap
				.get("signs");
		Map<String, Object> timerMap = (Map<String, Object>) serialMap
				.get("timer");

		Lottery lottery = new Lottery(plugin, timerMap, locs, players, name,
				repeat, itemOnly, ticketCost, pot, maxTickets, maxPlayers,
				minPlayers);

		for (int cntr = 1; true; cntr++) {

			if (serialMap.containsKey("item reward " + cntr)) {
				Map<String, Object> itemMap = (Map<String, Object>) serialMap
						.get("item reward " + cntr);
				ItemStack itemReward = ItemStack.deserialize(itemMap);
				lottery.addItemReward(itemReward);
			}

			else {
				break;
			}

		}

		return lottery;
	}

}
