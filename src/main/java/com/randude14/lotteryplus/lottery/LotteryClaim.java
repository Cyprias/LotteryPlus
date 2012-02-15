package com.randude14.lotteryplus.lottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class LotteryClaim {
	private List<ItemStack> itemRewards;
	private String lotteryName;
	private double pot;

	public LotteryClaim(String lottery, List<ItemStack> itemRewards, double pot) {
		this.itemRewards = itemRewards;
		this.lotteryName = lottery;
		this.pot = pot;
	}

	public String getLotteryName() {
		return lotteryName;
	}

	public double getPot() {
		return pot;
	}

	public void setPot(double pot) {
		this.pot = pot;
	}

	public List<ItemStack> getItemRewards() {
		return itemRewards;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> serialMap = new HashMap<String, Object>();
		serialMap.put("lottery name", lotteryName);
		serialMap.put("pot", pot);
		int cntr = 1;
		for (ItemStack itemReward : itemRewards) {
			serialMap.put("item reward " + (cntr++), itemReward.serialize());
		}
		return serialMap;
	}

	public static LotteryClaim deserialize(Map<String, Object> serialMap) {
		String lotteryName = (String) serialMap.get("lottery name");
		double pot = (Double) serialMap.get("pot");
		List<ItemStack> itemRewards = new ArrayList<ItemStack>();

		for (int cntr = 1; true; cntr++) {

			if (serialMap.containsKey("item reward " + cntr)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> itemMap = (Map<String, Object>) serialMap
						.get("item reward " + cntr);
				ItemStack itemReward = ItemStack.deserialize(itemMap);
				itemRewards.add(itemReward);
			}

			else {
				break;
			}

		}

		return new LotteryClaim(lotteryName, itemRewards, pot);
	}

}
