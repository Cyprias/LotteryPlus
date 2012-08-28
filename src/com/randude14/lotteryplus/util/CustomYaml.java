package com.randude14.lotteryplus.util;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.Plugin;

public class CustomYaml {
	private static final Plugin plugin = Plugin.getInstance();
	private final FileConfiguration config;
	private final File configFile;
	
	public CustomYaml(String file) {
		this(file, true);
	}
	
	public CustomYaml(String file, boolean load) {
		configFile = new File(plugin.getDataFolder(), file);
		config = new YamlConfiguration();
		if(load)
			this.reloadConfig();
	}
	
	public void reloadConfig() {
		try {
			config.load(configFile);
		} catch (Exception ex) {
			Logger.info("Could not load config for '%s'", configFile.getName());
		}
	}
	
	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (Exception ex) {
			Logger.info("Could not save config for '%s'", configFile.getName());
		}
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public boolean exists() {
		return configFile.exists();
	}
	
	public void saveDefaultConfig() {
		if(exists())
			return;
		plugin.saveResource(configFile.getName(), false);
	}
}
