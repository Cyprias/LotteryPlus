package com.randude14.lotteryplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.randude14.lotteryplus.lottery.Lottery;

public class LotteryManager {

	private final Plugin plugin;
	private List<Lottery> lotteries;
	private FileConfiguration lotteryConfig;
	private File lotteryFile;
	private File lotteryStore;

	protected LotteryManager(final Plugin plugin) {
		this.plugin = plugin;
		lotteries = new ArrayList<Lottery>();
		lotteryStore = new File(plugin.getDataFolder(), "lotteries");
		reloadConfig();
		if (!lotteryFile.exists()) {
			plugin.warning("'lotteries.yml' was not found. writing defaults.");
			writeConfig();
		}

	}

	public Lottery searchLottery(String name) {

		for (Lottery lottery : lotteries) {

			if (lottery.getName().equalsIgnoreCase(name)) {
				return lottery;
			}

		}

		return null;
	}

	private void reloadConfig() {

		if (lotteryFile == null) {
			lotteryFile = new File(plugin.getDataFolder(), "lotteries.yml");
		}

		lotteryConfig = YamlConfiguration.loadConfiguration(lotteryFile);

		InputStream lotteryStream = plugin.getResource("lotteries.yml");

		if (lotteryStream != null) {
			YamlConfiguration config = YamlConfiguration
					.loadConfiguration(lotteryStream);
			config.setDefaults(lotteryConfig);
		}

	}

	private void writeConfig() {
		FileConfiguration config = getConfig();
		config.createSection("lotteries");
		saveConfig();
	}

	private void saveConfig() {

		if (lotteryConfig == null || lotteryFile == null) {
			return;
		}

		try {
			lotteryConfig.save(lotteryFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + lotteryFile, ex);
		}

	}

	@SuppressWarnings("unchecked")
	protected void loadLotteries() {
		Map<String, Map<String, Object>> loadMap = null;

		try {
			ObjectInputStream stream = new ObjectInputStream(
					new FileInputStream(lotteryStore));
			loadMap = (Map<String, Map<String, Object>>) stream.readObject();
			stream.close();
		} catch (Exception ex) {
		}

		try {
			LotteryConfig lotteryConfig = plugin.getLotteryConfig();
			ConfigurationSection lotterySection = getConfig()
					.getConfigurationSection("lotteries");

			for (String lotteryName : lotterySection.getKeys(false)) {
				Lottery lottery = null;

				if (nameExists(lotteryName)) {
					continue;
				}

				if (loadMap != null && loadMap.containsKey(lotteryName)) {
					Map<String, Object> serialMap = (Map<String, Object>) loadMap
							.get(lotteryName);
					lottery = Lottery.deserialize(serialMap, plugin);
				}

				else {
					ConfigurationSection section = lotterySection
							.getConfigurationSection(lotteryName);
					double pot = section.getDouble("pot",
							lotteryConfig.getDefaultPot());
					double ticketCost = section.getDouble("ticketcost",
							lotteryConfig.getDefaultTicketCost());
					long time = section.getLong("time",
							lotteryConfig.getDefaultTime());
					boolean repeat = section.getBoolean("repeat", Boolean.TRUE);
					boolean itemOnly = section.getBoolean("item-only",
							Boolean.FALSE);
					int maxTickets = section.getInt("max-tickets",
							lotteryConfig.getDefaultMaxTickets());
					int maxPlayers = section.getInt("max-players",
							lotteryConfig.getDefaultMaxPlayers());
					int minPlayers = section.getInt("min-players",
							lotteryConfig.getDefaultMinPlayers());
					lottery = new Lottery(plugin, lotteryName, time, pot,
							ticketCost, repeat, itemOnly, maxTickets,
							maxPlayers, minPlayers);

					if (section.contains("item-reward")) {
						ConfigurationSection itemRewardOptions = section
								.getConfigurationSection("item-reward");

						for (String matName : itemRewardOptions.getKeys(false)) {
							ConfigurationSection itemOptions = itemRewardOptions
									.getConfigurationSection(matName);
							ItemStack itemReward = loadItem(itemOptions,
									matName);
							lottery.addItemReward(itemReward);
						}

					}

				}

				lotteries.add(lottery);
			}

			plugin.info("lotteries loaded.");
		} catch (Exception ex) {
			ex.printStackTrace();
			plugin.warning("error has occured while loading lotteries in config.");
			plugin.abort();
		}

	}

	public void resetLottery(Lottery lottery) {
		LotteryConfig config = plugin.getLotteryConfig();
		ConfigurationSection data = getConfig().getConfigurationSection(
				"lotteries." + lottery.getName());
		double pot = data.getDouble("pot", config.getDefaultPot());
		double ticketCost = data.getDouble("ticketcost",
				config.getDefaultTicketCost());
		long time = data.getLong("time", config.getDefaultTime());
		boolean repeat = data.getBoolean("repeat", Boolean.TRUE);
		boolean itemOnly = data.getBoolean("item-only", Boolean.FALSE);
		int maxTickets = data.getInt("max-tickets",
				config.getDefaultMaxTickets());
		int maxPlayers = data.getInt("max-players",
				config.getDefaultMaxPlayers());
		int minPlayers = data.getInt("min-players",
				config.getDefaultMinPlayers());

		lottery.setRepeat(repeat).setItemOnly(itemOnly).setPot(pot)
				.setTicketCost(ticketCost).setTimer(time)
				.setMaxPlayers(maxPlayers).setMinPlayers(minPlayers)
				.setMaxTickets(maxTickets);

		if (data.contains("item-reward")) {
			ConfigurationSection itemRewardOptions = data
					.getConfigurationSection("item-reward");

			for (String matName : itemRewardOptions.getKeys(false)) {
				ConfigurationSection itemOptions = itemRewardOptions
						.getConfigurationSection(matName);
				ItemStack itemReward = loadItem(itemOptions, matName);
				lottery.addItemReward(itemReward);
			}

		}

	}

	private ItemStack loadItem(ConfigurationSection itemOptions, String matName) {

		Material material = Material.getMaterial(matName);

		if (material == null) {
			return null;
		}

		int stackSize = itemOptions.getInt("stack-size", 1);
		ItemStack stack = new ItemStack(material, stackSize);

		for (String key : itemOptions.getKeys(false)) {

			if (key.equals("item-id") || key.equals("stack-size")) {
				continue;
			}

			Enchantment enchantment = Enchantment.getByName(key);
			int level = itemOptions.getInt(key);

			if (enchantment != null && enchantment.canEnchantItem(stack)) {

				try {
					stack.addEnchantment(enchantment, level);
				} catch (Exception ex) {
				}

			}

		}

		return stack;
	}

	public void saveLotteries() {

		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(lotteryStore));
			Map<String, Map<String, Object>> saveMap = new HashMap<String, Map<String, Object>>();

			for (Lottery lottery : lotteries) {
				saveMap.put(lottery.getName(), lottery.serialize());
			}

			oos.writeObject(saveMap);
			oos.flush();
			oos.close();
			plugin.info("lotteries saved.");
		} catch (Exception ex) {
			plugin.severe("failed to save lotteries.");
		}

	}

	public boolean nameExists(String name) {

		for (Lottery lottery : lotteries) {

			if (lottery.getName().equalsIgnoreCase(name)) {
				return true;
			}

		}

		return false;
	}

	public void removeLottery(String name) {

		for (int cntr = 0; cntr < lotteries.size(); cntr++) {
			Lottery lottery = lotteries.get(cntr);

			if (lottery.getName().equalsIgnoreCase(name)) {
				lotteries.remove(cntr);
			}

		}

	}

	private FileConfiguration getConfig() {

		if (lotteryConfig == null) {
			reloadConfig();
		}

		return lotteryConfig;
	}

	public List<Lottery> getLotteries() {
		return lotteries;
	}

}
