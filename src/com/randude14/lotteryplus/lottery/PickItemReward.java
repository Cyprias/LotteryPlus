package com.randude14.lotteryplus.lottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SerializableAs("PickItemReward")
public class PickItemReward implements Reward {
	private final List<ItemStack> items;
	private ItemStack rewardChosen = null;
	
	public PickItemReward(List<ItemStack> items) {
		Validate.notEmpty(items, "Items cannot be empty.");
		this.items = items;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(ItemStack item : items) {
			list.add(item.serialize());
		}
		map.put("items", list);
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static PickItemReward valueOf(Map<String, Object> map) {
		List<Map<?, ?>> list = (List<Map<?, ?>>) map.get("items");
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(Map<?, ?> itemMap : list) {
			ItemStack item = ItemStack.deserialize((Map<String, Object>) itemMap);
			if(item != null) {
				items.add(item);
			}
		}
		return new PickItemReward(items);
	}

	public void rewardPlayer(Player player) {
		Inventory inventory = this.createInventory(player);
		player.openInventory(inventory);
		
	}
	
	private Inventory createInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(player, getSize(), "Pick Item");
		for(ItemStack item : items) {
			inventory.addItem(item);
		}
		return inventory;
	}
	
	private int getSize() {
		int size = 9;
		while(size < items.size()) {
			size += 9;
		}
		return (size > 54) ? 54 : size;
	}
	
	public void setChosenReward(ItemStack chosen) {
		if(!items.contains(chosen))
			throw new IllegalArgumentException("Cannot set reward chosen when not in this PickItemReward.");
		this.rewardChosen = chosen;
	}

	public String getInfo() {
		return String.format("Pick Item: [%d item(s)]", items.size());
	}
	
	public String toString() {
		return (rewardChosen != null) ? String.format("[%d %s's]", rewardChosen.getType().name(), rewardChosen.getAmount()) : "[NULL]"; 
	}
}
