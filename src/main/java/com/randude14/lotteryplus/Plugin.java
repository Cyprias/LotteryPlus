package com.randude14.lotteryplus;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.randude14.lotteryplus.io.ObjectLoadStream;
import com.randude14.lotteryplus.io.ObjectSaveStream;
import com.randude14.lotteryplus.listeners.SignListener;
import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.lottery.LotteryClaim;
import com.randude14.lotteryplus.util.TimeConstants;

public class Plugin extends JavaPlugin implements Listener, Runnable,
		TimeConstants {
	private static final String CMD_LOTTERY = "lottery";
	private Map<String, List<LotteryClaim>> claims;
	private Map<String, String> buyers;
	private LotteryManager manager;
	private List<String> winners;
	private LotteryConfig config;
	private SignListener signListener;
	private Logger logger;
	private BukkitScheduler scheduler;
	private String logName;
	private String checkVersion;
	private File configFile;
	private File listMaterials;
	private File listEnchantments;
	private File listColors;
	private File claimsFile;
	private File winnersFile;
	private File winnersLogFile;
	private Random random;
	private Permission perm;
	private Economy econ;

	@SuppressWarnings("unchecked")
	public void onEnable() {
		buyers = new HashMap<String, String>();
		winners = new ArrayList<String>();
		logger = Logger.getLogger("Minecraft");
		scheduler = getServer().getScheduler();
		logName = "[" + this + "]";
		random = new Random(getName().hashCode());
		config = new LotteryConfig(this);
		manager = new LotteryManager(this);
		claimsFile = new File(getDataFolder(), "claims");
		winnersFile = new File(getDataFolder(), "winners");
		configFile = new File(getDataFolder(), "config.yml");
		listColors = new File(getDataFolder(), "colors.yml");
		listMaterials = new File(getDataFolder(), "items.yml");
		winnersLogFile = new File(getDataFolder(), "winners.log");
		listEnchantments = new File(getDataFolder(), "enchantments.yml");
		signListener = new SignListener(this);

		if (!configFile.exists()) {
			info("config file not found. attempting to write default config...");
			config.writeConfig();
		}

		LotteryExtras extras = new LotteryExtras(this);

		if (!listMaterials.exists()) {
			extras.writeMaterialConfig();
		}

		if (!listEnchantments.exists()) {
			extras.writeEnchantmentConfig();
		}

		if (!listColors.exists()) {
			extras.writeColorConfig();
		}

		if (!setupEconomy()) {
			warning("economy system not found! Lottery+ uses 'Vault' to plug into other economies.");
			warning("download is at 'http://dev.bukkit.org/server-mods/vault/'");
			abort();
			return;
		}

		if (!setupPermission()) {
			warning("permission system not found! Lottery+ uses 'Vault' to plug into other permissions.");
			warning("download is at 'http://dev.bukkit.org/server-mods/vault/'");
			abort();
			return;
		}

		config.loadConfig();
		manager.loadLotteries();
		if (manager.getLotteries().isEmpty()) {
			severe("no lotteries have been loaded.");
			abort();
			return;
		}

		try {
			ObjectLoadStream stream = new ObjectLoadStream(claimsFile);
			Map<String, List<Map<String, Object>>> savesMap = (Map<String, List<Map<String, Object>>>) stream
					.readObject();
			claims = new HashMap<String, List<LotteryClaim>>();

			for (String player : savesMap.keySet()) {
				List<Map<String, Object>> saves = savesMap.get(player);
				List<LotteryClaim> claims = new ArrayList<LotteryClaim>();
				this.claims.put(player, claims);

				for (Map<String, Object> save : saves) {
					LotteryClaim claim = LotteryClaim.deserialize(save);
					claims.add(claim);
				}

			}

		} catch (Exception ex) {
			claims = new HashMap<String, List<LotteryClaim>>();
		}

		try {
			ObjectLoadStream stream = new ObjectLoadStream(winnersFile);
			Object store = stream.readObject();
			winners = (store != null) ? ((List<String>) (store))
					: new ArrayList<String>();
		} catch (Exception ex) {
			winners = new ArrayList<String>();
		}

		if (this.isEnabled()) {
			info("enabled.");
			registerListeners(this, signListener);
			checkVersion = getDescription().getVersion();
			callTasks();
			getCommand(CMD_LOTTERY).setExecutor(new LotteryCommands(this));
		}

	}

	public void callTasks() {
		long delayAutoMessenger = MINUTE * SERVER_SECOND
				* config.getReminderMessageTime();
		scheduler.scheduleSyncRepeatingTask(this, this, delayAutoMessenger,
				delayAutoMessenger);
		long delayUpdate = MINUTE * SERVER_SECOND * config.getUpdateDelay();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				String currentVersion = updateCheck(checkVersion);
				if (!currentVersion.endsWith(checkVersion)) {
					info(String
							.format("there is a new version of %s: %s (you are running v%s)",
									getName(), currentVersion, checkVersion));
				}

			}
		}, 0, delayUpdate);
		scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				manager.start();
			}
		});
	}

	public void abort() {
		severe("An error has ocurred. shutting down...");
		setEnabled(false);
	}

	public void reload() {
		manager.reloadLotteries();
	}

	public void onDisable() {
		scheduler.cancelTasks(this);
		manager.saveLotteries();

		try {
			ObjectSaveStream stream = new ObjectSaveStream(claimsFile);
			Map<String, List<Map<String, Object>>> savesMap = new HashMap<String, List<Map<String, Object>>>();

			for (String player : claims.keySet()) {
				List<LotteryClaim> claims = this.claims.get(player);
				if (claims.isEmpty()) {
					continue;
				}
				List<Map<String, Object>> claimMaps = new ArrayList<Map<String, Object>>();

				for (LotteryClaim claim : claims) {
					claimMaps.add(claim.serialize());
				}
				savesMap.put(player, claimMaps);
			}

			stream.writeObject(savesMap);
			stream.close();
			info("lottery claims saved.");
		} catch (Exception ex) {
			severe("failed to save lottery claims.");
		}

		try {
			ObjectSaveStream stream = new ObjectSaveStream(winnersFile);
			stream.writeObject(winners);
			stream.close();
			info("winners saved.");
		} catch (Exception ex) {
			severe("failed to save winners.");
		}

		info("disabled.");
	}

	private void registerListeners(Listener... listeners) {
		PluginManager manager = getServer().getPluginManager();

		for (Listener listener : listeners) {
			manager.registerEvents(listener, this);
		}

	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}

		return (econ != null);
	}

	private boolean setupPermission() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			perm = permissionProvider.getProvider();
		}

		return (perm != null);
	}

	public void info(String info) {
		logger.log(Level.INFO, logName + " - " + info);
	}

	public void warning(String info) {
		logger.log(Level.WARNING, logName + " - " + info);
	}

	public void severe(String info) {
		logger.log(Level.SEVERE, logName + " - " + info);
	}

	public void infoRaw(String info) {
		logger.log(Level.INFO, info);
	}

	public void warningRaw(String info) {
		logger.log(Level.WARNING, info);
	}

	public void severeRaw(String info) {
		logger.log(Level.SEVERE, info);
	}

	public String format(double value) {
		return econ.format(value);
	}

	public void broadcast(String message, String permission) {

		for (Player player : getServer().getOnlinePlayers()) {

			if (hasPermission(player, permission)) {
				send(player, message);
			}

		}

		message = ChatColor.stripColor(message);
		infoRaw(message);
	}

	public void broadcast(String message) {

		for (Player player : getServer().getOnlinePlayers()) {
			send(player, message);
		}

		infoRaw(message);
	}

	public void run() {
		broadcast(config.getReminderMessage(), "lottery.list");
	}

	public String replaceColors(String message) {
		return message.replaceAll("&0", ChatColor.BLACK.toString())
				.replaceAll("&1", ChatColor.DARK_BLUE.toString())
				.replaceAll("&2", ChatColor.DARK_GREEN.toString())
				.replaceAll("&3", ChatColor.DARK_AQUA.toString())
				.replaceAll("&4", ChatColor.DARK_RED.toString())
				.replaceAll("&5", ChatColor.DARK_PURPLE.toString())
				.replaceAll("&6", ChatColor.GOLD.toString())
				.replaceAll("&7", ChatColor.GRAY.toString())
				.replaceAll("&8", ChatColor.DARK_GRAY.toString())
				.replaceAll("&9", ChatColor.BLUE.toString())
				.replaceAll("&a", ChatColor.GREEN.toString())
				.replaceAll("&b", ChatColor.AQUA.toString())
				.replaceAll("&c", ChatColor.RED.toString())
				.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString())
				.replaceAll("&e", ChatColor.YELLOW.toString())
				.replaceAll("&f", ChatColor.WHITE.toString())
				.replaceAll("&k", ChatColor.MAGIC.toString());
	}

	public boolean locsInBounds(Location loc1, Location loc2) {
		return loc1.getBlockX() == loc2.getBlockX()
				&& loc1.getBlockY() == loc2.getBlockY()
				&& loc1.getBlockZ() == loc2.getBlockZ();
	}

	public void send(Player player, String mess, ChatColor color) {
		player.sendMessage(color + mess);
	}

	public void send(Player player, String mess) {
		send(player, mess, ChatColor.YELLOW);
	}

	public void help(Player player, String mess) {
		send(player, mess, ChatColor.GOLD);
	}

	public void error(Player player, String mess) {
		send(player, mess, ChatColor.RED);
	}

	public boolean hasPermission(Player player, String permission) {
		return perm.has(player.getWorld().getName(), player.getName(),
				permission) || !config.isPermsEnabled();
	}

	public void addBuyer(String player, String lottery) {
		buyers.put(player, lottery);
	}

	public boolean isBuyer(String name) {
		return buyers.containsKey(name);
	}

	public void removeBuyer(String name) {
		buyers.remove(name);
	}

	public void addWinner(String winner) {
		winners.add(winner);

		while (winners.size() > 5) {
			winners.remove(0);
		}
		if (winnersLogFile.exists()) {
			try {
				List<String> logs = new ArrayList<String>();
				Scanner scan = new Scanner(winnersLogFile);
				while (scan.hasNextLine()) {
					logs.add(scan.nextLine());
				}
				logs.add(winner);
				PrintWriter writer = new PrintWriter(winnersLogFile);
				for (String winnerLog : logs) {
					writer.println(winnerLog);
				}
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				warning("exception caught in addWinner(String winner).");
			}
		}
		else {
			try {
				PrintWriter writer = new PrintWriter(winnersLogFile);
				writer.println(winner);
				writer.flush();
				writer.close();
			} catch (Exception ex) {
				warning("exception caught in addWinner(String winner).");
			}
		}
	}

	public void addClaim(String name, String lottery,
			List<ItemStack> itemRewards) {
		addClaim(name, lottery, itemRewards, -1);
	}

	public void addClaim(String name, String lottery, double pot) {
		addClaim(name, lottery, null, pot);
	}

	public void addClaim(String name, String lottery,
			List<ItemStack> itemRewards, double pot) {

		if (!claims.containsKey(name)) {
			claims.put(name, new ArrayList<LotteryClaim>());
		}

		claims.get(name).add(new LotteryClaim(lottery, itemRewards, pot));
	}

	public List<LotteryClaim> getClaims(String player) {
		return claims.get(player);
	}

	protected void listWinners(Player player) {
		if (!hasPermission(player, "lottery.winners")) {
			error(player, "You do not have permission");
			return;
		}

		help(player, "---------------------------------------------------");
		send(player, logName + " - winners");
		send(player, "");

		if (winners.isEmpty()) {
			error(player, "There are currently no winners");
		}

		else {

			for (int cntr = 0; cntr < winners.size(); cntr++) {
				send(player, (cntr + 1) + ". " + winners.get(cntr));
			}

		}

		help(player, "---------------------------------------------------");
	}

	public void playerBuyFromLottery(Player player, Lottery lottery, int tickets) {
		String name = player.getName();

		if (!econ.hasAccount(name)) {
			error(player, "You do not have an account");
			send(player, "Transaction cancelled");
			help(player, "---------------------------------------------------");
			return;
		}

		int maxTickets = lottery.getMaxTickets();
		int ticketsBought = lottery.getTicketsBought(name);

		if (maxTickets != -1) {

			if (ticketsBought >= maxTickets) {
				error(player, "You have bought too many tickets.");
				send(player, "Transaction cancelled");
				help(player,
						"---------------------------------------------------");
				return;
			}

			if (ticketsBought + tickets > maxTickets) {
				error(player, "You cannot buy this many tickets.");
				send(player, "Transaction cancelled");
				help(player,
						"---------------------------------------------------");
				return;
			}

		}

		double cost = tickets * lottery.getTicketCost();
		if (econ.getBalance(name) < cost) {
			error(player, "You do not have enough money");
			send(player, "Transaction cancelled");
			help(player, "---------------------------------------------------");
			return;
		}

		if (cost < 0) {
			error(player, "Money must be positive");
			send(player, "Transaction cancelled");
			help(player, "---------------------------------------------------");
			return;
		}

		econ.withdrawPlayer(name, cost);
		double added = lottery.playerBought(name, tickets);
		send(player, "Player has bought " + tickets + " ticket(s) for "
				+ ChatColor.GOLD.toString() + format(cost));
		send(player, ChatColor.GOLD + format(added) + ChatColor.YELLOW
				+ " has been added to the pot.");
		send(player, "Transaction completed");
		help(player, "---------------------------------------------------");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerLogout(PlayerQuitEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerKick(PlayerKickEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	@EventHandler
	public void playerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		String message = event.getMessage();

		if (buyers.containsKey(name)) {
			Lottery lottery = manager.searchLottery(buyers.get(name));

			if (lottery != null) {
				int tickets = 0;

				try {
					tickets = Integer.parseInt(message);
				} catch (Exception ex) {
					error(player, "Invalid number");
					send(player, "Transaction cancelled");
					help(player,
							"---------------------------------------------------");
					buyers.remove(name);
					event.setCancelled(true);
					return;
				}

				playerBuyFromLottery(player, lottery, tickets);
			}

			else {
				error(player, "Lottery has been removed for unknown reasons");
				send(player, "Transaction cancelled");
				help(player,
						"---------------------------------------------------");
			}

			buyers.remove(name);
			event.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		List<LotteryClaim> claims = this.claims.get(name);

		if (claims != null && !claims.isEmpty()) {
			send(player,
					"You have a lottery reward(s) to claim. Type '/lottery claim' to claim your reward.");
		}

	}

	public String updateCheck(String currentVersion) {
		try {
			URL url = new URL(
					"http://dev.bukkit.org/server-mods/lotteryplus/files.rss");
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName
						.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();
				return firstNodes.item(0).getNodeValue();
			}
		} catch (Exception ex) {
		}

		return currentVersion;
	}

	public boolean isSign(Block block) {
		return block.getType() == Material.SIGN_POST
				|| block.getType() == Material.WALL_SIGN;
	}

	public Economy getEconomy() {
		return econ;
	}

	public BukkitScheduler getScheduler() {
		return scheduler;
	}

	public Random getRandom() {
		return random;
	}

	public LotteryConfig getLotteryConfig() {
		return config;
	}

	public LotteryManager getLotteryManager() {
		return manager;
	}

}
