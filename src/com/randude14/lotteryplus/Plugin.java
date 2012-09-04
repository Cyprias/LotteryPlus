package com.randude14.lotteryplus;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.randude14.lotteryplus.command.*;
import com.randude14.lotteryplus.configuration.CustomYaml;
import com.randude14.lotteryplus.listeners.PlayerListener;
import com.randude14.lotteryplus.listeners.SignListener;
import com.randude14.lotteryplus.lottery.ItemReward;
import com.randude14.lotteryplus.lottery.Lottery;
import com.randude14.lotteryplus.register.permission.BukkitPermission;
import com.randude14.lotteryplus.register.permission.Permission;
import com.randude14.lotteryplus.register.permission.VaultPermission;
import com.randude14.lotteryplus.tasks.*;
import com.randude14.lotteryplus.util.TimeConstants;

public class Plugin extends JavaPlugin implements Listener, TimeConstants {
	private static Plugin instance = null;
	private static final Map<InventoryHolder, Inventory> inventories = new HashMap<InventoryHolder, Inventory>();
	private static final Map<String, String> buyers = new HashMap<String, String>();
	private static final List<Task> tasks = new ArrayList<Task>();
	public static final String CMD_LOTTERY = "lottery";
	private static Permission perm;
	private File configFile;

	public void onEnable() {
		instance = this;
		File dataFolder = getDataFolder();
		dataFolder.mkdirs();
		configFile = new File(dataFolder, "config.yml");

		if (!configFile.exists()) {
			Logger.info("Config file not found. Writing defaults.");
			saveDefaultConfig();
		}
		
		tasks.add(new ReminderMessageTask());
		tasks.add(new SaveTask());
		tasks.add(new UpdateCheckTask());
		ClaimManager.loadClaims();
		WinnersManager.loadWinners();
		int numLotteries = LotteryManager.loadLotteries();
		if(numLotteries == 1) {
			Logger.info("1 lottery was loaded.");
		} else {
			Logger.info("%d lotteries were loaded.", numLotteries);
		}
		loadPermissions();
		loadRegistry();
		callTasks();
		saveExtras();
		registerListeners(this, new PlayerListener(), new SignListener());
		Command atpCommand = new AddToPotCommand();
		CommandManager cm = new CommandManager()
		    .registerCommand("buy", new BuyCommand())
		    .registerCommand("draw", new DrawCommand())
		    .registerCommand("info", new InfoCommand())
		    .registerCommand("claim", new ClaimCommand())
		    .registerCommand("create", new CreateCommand())
		    .registerCommand("reload", new ReloadCommand())
		    .registerCommand("reloadall", new ReloadAllCommand())
		    .registerCommand("load", new LoadCommand())
		    .registerCommand("list", new ListCommand())
		    .registerCommand("unload", new UnloadCommand())
		    .registerCommand("addtopot", atpCommand)
		    .registerCommand("atp", atpCommand)
		    .registerCommand("winners", new WinnersCommand())
		    .registerCommand("reward", new RewardCommand())
		    .registerCommand("save", new SaveCommand())
		    .registerCommand("config", new ConfigCommand())
		    .registerCommand("version", new VersionCommand())
		    .registerCommand("update", new UpdateCommand());
		this.getCommand("lottery").setExecutor(cm);
		scheduleAsyncRepeatingTask(new LotteryManager.TimerTask(), 20L, 20L);
		Logger.info("enabled.");
	}
	
	public void onDisable() {
		Logger.info("disabled.");
		getServer().getScheduler().cancelTasks(this);
		LotteryManager.saveLotteries();
		instance = null;
		perm = null;
	}
	
	private void loadPermissions() {
		PluginManager pm = Bukkit.getPluginManager();
		for (Perm permission : Perm.values()) {
			permission.loadPermission(pm);
		}
	}
	
	private static void loadRegistry() {
		if(!VaultPermission.isVaultInstalled()) {
			Logger.info("Permission system not found from Vault or Vault is not installed.");
			Logger.info("Defaulting to Bukkit's permission system.");
			perm = new BukkitPermission();
		} else {
			perm = new VaultPermission();
		}
	}

	private void saveExtras() {
		CustomYaml enchants = new CustomYaml("enchantments.yml", false);
		FileConfiguration enchantsConfig = enchants.getConfig();
		for (Enchantment enchant : Enchantment.values()) {
			enchantsConfig.set(
					"enchantments." + enchant.getName(),
					String.format("%d-%d", enchant.getStartLevel(),
							enchant.getMaxLevel()));
		}
		enchants.saveConfig();
		CustomYaml items = new CustomYaml("items.yml", false);
		FileConfiguration itemsConfig = items.getConfig();
		for (Material mat : Material.values()) {
			itemsConfig.set("items." + mat.name(), mat.getId());
		}
		items.saveConfig();
		CustomYaml colors = new CustomYaml("colors.yml", false);
		FileConfiguration colorsConfig = colors.getConfig();
		for (ChatColor color : ChatColor.values()) {
			colorsConfig.set("colors." + color.name(),
					Character.toString(color.getChar()));
		}
		colors.saveConfig();
	}
	
	private static void callTasks() {
		for(Task task : tasks) {
			task.scheduleTask();
		}
	}

	public static void reload() {
		instance.reloadConfig();
		callTasks();
		loadRegistry();
	}
	
	public static void disable() {
		instance.getServer().getPluginManager().disablePlugin(instance);
	}

	private void registerListeners(Listener... listeners) {
		PluginManager manager = getServer().getPluginManager();

		for (Listener listener : listeners) {
			manager.registerEvents(listener, this);
		}
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
			Perm permission) {
		if (!hasPermission(sender, permission)) {
			String mess = permission.getErrorMessage();
			if (mess == null)
				mess = Perm.DEFAULT_ERROR_MESSAGE;
			ChatUtils.error(sender, mess);
			return false;
		}
		return true;
	}

	public static boolean hasPermission(CommandSender sender,
			Perm permission) {
		if (sender instanceof ConsoleCommandSender)
			return true;
		Player player = (Player) sender;
		return perm.hasPermission(player, permission);
	}

	public static void addBuyer(String player, String lottery) {
		buyers.put(player, lottery);
	}

	public static boolean isBuyer(String name) {
		return buyers.containsKey(name);
	}

	public static String removeBuyer(String name) {
		return buyers.remove(name);
	}

	
	// remove buyers when the leave
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	// remove buyers when they get kicked
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		String name = event.getPlayer().getName();
		buyers.remove(name);
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
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
					ChatUtils.errorRaw(player, "Invalid number");
					ChatUtils.errorRaw(player, "Transaction cancelled");
					ChatUtils
							.sendRaw(player, ChatColor.GOLD,
									"---------------------------------------------------");
					event.setCancelled(true);
					return;
				}

				if (tickets <= 0) {
					ChatUtils.errorRaw(player,
							String.format("Tickets must be positive."));
					ChatUtils.errorRaw(player, "Transaction cancelled");
					ChatUtils
							.sendRaw(player, ChatColor.GOLD,
									"---------------------------------------------------");
					return;
				}

				if (lottery.buyTickets(player, tickets)) {
					ChatUtils.sendRaw(player, ChatColor.GREEN, "Transaction completed");
					ChatUtils
							.sendRaw(player, ChatColor.GOLD,
									"---------------------------------------------------");
					lottery.broadcast(player.getName(), tickets);
					if(lottery.isOver()) {
						lottery.draw();
					}
				} else {
					ChatUtils.errorRaw(player, "Transaction cancelled");
					ChatUtils
							.sendRaw(player, ChatColor.GOLD,
									"---------------------------------------------------");
				}
			}

			else {
				ChatUtils.errorRaw(player,
						"%s has been removed for unknown reasons", lotteryName);
				ChatUtils.sendRaw(player, ChatColor.GREEN, "Transaction cancelled");
				ChatUtils.sendRaw(player, ChatColor.GOLD, "---------------------------------------------------");
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClose(InventoryCloseEvent event) {
		HumanEntity entity = event.getPlayer();
		Inventory inventory = inventories.get(entity);
		if(inventory != null) {
			dropItems(inventory.getContents(), entity.getLocation());
		}
	}
	
	public static int scheduleAsyncRepeatingTask(Runnable runnable,
			long initialDelay, long reatingDelay) {
		return instance
				.getServer()
				.getScheduler()
				.scheduleAsyncRepeatingTask(instance, runnable, initialDelay,
						reatingDelay);
	}

	public static int scheduleAsyncDelayedTask(Runnable runnable, long delay) {
		return instance.getServer().getScheduler()
				.scheduleAsyncDelayedTask(instance, runnable, delay);
	}

	public static int scheduleSyncRepeatingTask(Runnable runnable,
			long initialDelay, long reatingDelay) {
		return instance
				.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(instance, runnable, initialDelay,
						reatingDelay);
	}
	
	public static void openInventory(List<ItemReward> rewards, Player player) {
		int invSize = getSize(rewards.size());
		Inventory inventory = Bukkit.createInventory(player, invSize, "Item Rewards");
		for(ItemReward reward : rewards) {
			inventory.addItem(reward.getItem());
		}
		player.openInventory(inventory);
		inventories.put(player, inventory);
	}
	
	private static void dropItems(ItemStack[] contents, Location loc) {
		World world = loc.getWorld();
		for(ItemStack item : contents) {
			if(item != null && item.getType() == Material.AIR) {
				world.dropItem(loc, item);
			}
		}
	}
	
	private static int getSize(int size) {
		int invSize = 9;
		while(invSize < size) {
			invSize += 9;
		}
		return (invSize > 54) ? 54 : invSize;
	}
	
	public static void updateCheck(String currentVersion) {
		updateCheck(Bukkit.getConsoleSender(), currentVersion);
	}
	
	public static void updateCheck(CommandSender sender, String currentVersion) {
		String latestVersion = currentVersion;
		try {
			URL url = new URL("http://dev.bukkit.org/server-mods/lotteryplus/files.rss");
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
				latestVersion = firstNodes.item(0).getNodeValue();
			}
		} catch (Exception ex) {
			latestVersion = currentVersion;
		}
		if(!latestVersion.endsWith(currentVersion)) {
			ChatUtils.send(sender, ChatColor.YELLOW, "New version available: Current version: %s, Latest version: %s.", currentVersion, latestVersion);
		} else {
			ChatUtils.error(sender, "No updates available.");
		}
	}

	public static int scheduleSyncDelayedTask(Runnable runnable, long delay) {
		return instance.getServer().getScheduler()
				.scheduleSyncDelayedTask(instance, runnable, delay);
	}
	
	public static void cancelTask(int taskId) {
		instance.getServer().getScheduler().cancelTask(taskId);
	}
	
	public static String getVersion() {
		return instance.getDescription().getVersion();
	}

	public static boolean isSign(Block block) {
		return block.getState() instanceof Sign;
	}

	public static boolean isSign(Location loc) {
		return isSign(loc.getBlock());
	}
	
	public static final Plugin getInstance() {
		return instance;
	}
}
