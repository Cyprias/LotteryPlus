package com.randude14.lotteryplus;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.randude14.lotteryplus.command.*;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.listeners.PlayerListener;
import com.randude14.lotteryplus.listeners.SignListener;
import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.tasks.ReminderMessageTask;
import com.randude14.lotteryplus.util.CustomYaml;
import com.randude14.lotteryplus.util.TimeConstants;

public class Plugin extends JavaPlugin implements Listener, TimeConstants {
	private static Plugin instance = null;
	private static final Map<String, String> buyers = new HashMap<String, String>();
	private static net.milkbowl.vault.permission.Permission perm;
	private static Economy econ;
	public static final String CMD_LOTTERY = "lottery";
	private String checkVersion;
	private File configFile;
	private static boolean reminderMessageEnabled;
	private static int reminderId;
	private int updateId = -1;

	public void onEnable() {
		instance = this;
		File dataFolder = getDataFolder();
		dataFolder.mkdirs();
		configFile = new File(dataFolder, "config.yml");

		if (!configFile.exists()) {
			Logger.info("Config file not found. Writing defaults.");
			saveDefaultConfig();
		}

		if (!setupEconomy()) {
			Logger.warning("economy system not found! Lottery+ uses 'Vault' to plug into other economies.");
			Logger.warning("download is at 'http://dev.bukkit.org/server-mods/vault/'");
			return;
		}

		if (!setupPermission()) {
			Logger.warning("permission system not found! Lottery+ uses 'Vault' to plug into other permissions.");
			Logger.warning("download is at 'http://dev.bukkit.org/server-mods/vault/'");
			return;
		}
		

		ClaimManager.loadClaims();
		WinnersManager.loadWinners();
		LotteryManager.loadLotteries();

		if (this.isEnabled()) {
			loadPermissions();
			saveExtras();
			registerListeners(this, new PlayerListener(), new SignListener());
			checkVersion = getDescription().getVersion();
			callTasks();
			CommandManager cm = new CommandManager()
			    .registerCommand("buy", new BuyCommand())
				.registerCommand("draw", new DrawCommand());
			//TODO add remaining commands
			this.getCommand(CMD_LOTTERY).setExecutor(cm);
			Logger.info("enabled.");
		}

	}

	private void callTasks() {
		if (Config.getProperty(Config.REMINDER_ENABLE)) {
			long delayAutoMessenger = MINUTE * SERVER_SECOND
					* Config.getProperty(Config.REMINDER_MESSAGE_TIME);
			reminderId = scheduleSyncRepeatingTask(new ReminderMessageTask(),
					delayAutoMessenger, delayAutoMessenger);
			reminderMessageEnabled = true;
		}
		if (updateId == -1) {
			long delayUpdate = MINUTE * SERVER_SECOND * Config.getProperty(Config.UPDATE_DELAY);
			updateId = scheduleSyncRepeatingTask(
					new Runnable() {
						public void run() {
							String currentVersion = updateCheck(checkVersion);
							if (!currentVersion.endsWith(checkVersion)) {
								Logger.info("there is a new version of %s: %s (you are running v%s)");
							}

						}
					}, 0, delayUpdate);
		}
		//TODO add save task
	}

	private void loadPermissions() {
		PluginManager pm = Bukkit.getPluginManager();
		for (Permission permission : Permission.values()) {
			permission.loadPermission(pm);
		}
	}
	
	private void saveExtras() {
		CustomYaml enchants = new CustomYaml("enchantments.yml", false);
		FileConfiguration enchantsConfig = enchants.getConfig();
		for(Enchantment enchant : Enchantment.values()) {
			enchantsConfig.set("enchantments." + enchant.getName(), String.format("%d-%d", enchant.getStartLevel(), enchant.getMaxLevel()));
		}
		enchants.saveConfig();
		CustomYaml items = new CustomYaml("items.yml", false);
		FileConfiguration itemsConfig = items.getConfig();
		for(Material mat : Material.values()) {
			itemsConfig.set("items." + mat.name(), mat.getId());
		}
		items.saveConfig();
		CustomYaml colors = new CustomYaml("colors.yml", false);
		FileConfiguration colorsConfig = colors.getConfig();
		for(ChatColor color : ChatColor.values()) {
			colorsConfig.set("colors." + color.name(), Character.toString(color.getChar()));
		}
		colors.saveConfig();
	}

	public static void reload() {
		instance.reloadConfig();
		if (!reminderMessageEnabled) {
			long delayAutoMessenger = MINUTE * SERVER_SECOND
					* Config.getProperty(Config.REMINDER_MESSAGE_TIME);
			reminderId = Plugin.scheduleSyncRepeatingTask(new ReminderMessageTask(), 
					delayAutoMessenger, delayAutoMessenger);
			reminderMessageEnabled = true;
		}

		else {

			if (!Config.getProperty(Config.REMINDER_ENABLE)) {
				instance.getServer().getScheduler().cancelTask(reminderId);
			}

		}

	}

	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		Logger.info("disabled.");
	}

	private void registerListeners(Listener... listeners) {
		PluginManager manager = getServer().getPluginManager();

		for (Listener listener : listeners) {
			manager.registerEvents(listener, this);
		}

	}

	private static boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = instance
				.getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}

		return (econ != null);
	}

	private static boolean setupPermission() {
		RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = instance
				.getServer()
				.getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			perm = permissionProvider.getProvider();
		}

		return (perm != null);
	}

	public static boolean locsInBounds(Location loc1, Location loc2) {
		return loc1.getBlockX() == loc2.getBlockX()
				&& loc1.getBlockY() == loc2.getBlockY()
				&& loc1.getBlockZ() == loc2.getBlockZ();
	}

	// uses binary search
	public static OfflinePlayer getOfflinePlayer(String name) {
		OfflinePlayer[] players = instance.getServer().getOfflinePlayers();
		int left = 0;
		int right = players.length - 1;
		while (left <= right) {
			int mid = (left + right) / 2;
			int result = players[mid].getName().compareToIgnoreCase(name);
			if (result == 0)
				return players[mid];
			else if (result < 0)
				left = mid + 1;
			else
				right = mid - 1;
		}

		// if it doesn't exist, then have the server
		// create the object instead of returning null
		return instance.getServer().getOfflinePlayer(name);
	}

	public static boolean checkPermission(CommandSender sender,
			Permission permission) {
		if (!hasPermission(sender, permission)) {
			String mess = permission.getErrorMessage();
			if (mess == null)
				mess = Permission.DEFAULT_ERROR_MESSAGE;
			ChatUtils.error(sender, mess);
			return false;
		}
		return true;
	}

	public static boolean hasPermission(CommandSender sender,
			Permission permission) {
		if (sender instanceof ConsoleCommandSender)
			return true;
		Player p = (Player) sender;
		String player = p.getName();
		String world = p.getWorld().getName();
		return permission.hasPermission(perm, player, world);
	}

	public static void addBuyer(String player, String lottery) {
		buyers.put(player, lottery);
	}

	public static boolean isBuyer(String name) {
		return buyers.containsKey(name);
	}

	public static void removeBuyer(String name) {
		buyers.remove(name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		String chat = event.getMessage();

		if (buyers.containsKey(name)) {
			String lotteryName = buyers.remove(name);
			Lottery lottery = LotteryManager.getLottery(lotteryName);
			event.setCancelled(true);

			if (lottery != null) {
				int tickets = 0;

				try {
					tickets = Integer.parseInt(chat);
				} catch (Exception ex) {
					ChatUtils.error(player, "Invalid number");
					ChatUtils.send(player, "Transaction cancelled");
					ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
					event.setCancelled(true);
					return;
				}

				if (tickets <= 0) {
					ChatUtils.error(player, String.format("Tickets cannot be negative."));
					ChatUtils.error(player, "Transaction cancelled");
					ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
					return;
				}

				if(lottery.buyTickets(player, tickets)) {
					ChatUtils.error(player, "Transaction completed");
					ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
					String message = Config.getProperty(Config.BUY_MESSAGE);
					message.replace("<player>", name).replace("<tickets>", "" + tickets).replace("<lottery>", lottery.getName());
					ChatUtils.broadcast(message);
				}
				else {
					ChatUtils.error(player, "Transaction cancelled");
					ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
				}
			}

			else {
				ChatUtils.error(player, "%s has been removed for unknown reasons", lotteryName);
				ChatUtils.error(player, "Transaction cancelled");
				ChatUtils.send(player, ChatColor.YELLOW, "---------------------------------------------------");
			}

		}

	}

	private String updateCheck(String currentVersion) {
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

	public static int scheduleSyncRepeatingTask(Runnable runnable,
			long initialDelay, long reatingDelay) {
		return instance
				.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(instance, runnable, initialDelay,
						reatingDelay);
	}
	
	public static int scheduleSyncDelayedTask(Runnable runnable,
			long delay) {
		return instance
				.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(instance, runnable, delay);
	}

	public static boolean isSign(Block block) {
		return block.getState() instanceof Sign;
	}

	public static boolean isSign(Location loc) {
		return isSign(loc.getBlock());
	}

	public static Economy getEconomy() {
		return econ;
	}

	public static final Plugin getInstance() {
		return instance;
	}
}
