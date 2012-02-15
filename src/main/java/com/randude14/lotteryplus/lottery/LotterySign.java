package com.randude14.lotteryplus.lottery;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public class LotterySign {
	private final Lottery lottery;
	private final Sign sign;

	public LotterySign(Lottery lottery, Sign sign) {
		this.lottery = lottery;
		this.sign = sign;
	}

	public Location getLocation() {
		return sign.getBlock().getLocation();
	}

	protected Sign getSign() {
		return sign;
	}

	public void over(String winner) {

		if (!sign.getBlock().getChunk().isLoaded()) {
			return;
		}

		sign.setLine(0, ChatColor.GREEN + "[Lottery+]");
		sign.setLine(1, lottery.getName());
		sign.setLine(2, "Lottery ended.");
		sign.setLine(3, winner);
		sign.update(true);
	}

	public void draw() {

		if (!sign.getBlock().getChunk().isLoaded()) {
			return;
		}

		sign.setLine(0, ChatColor.GREEN + "[Lottery+]");
		sign.setLine(1, lottery.getName());
		sign.setLine(2, "Drawing lottery...");
		if (lottery.isItemOnly()) {
			List<ItemStack> itemRewards = lottery.getItemRewards();

			if (itemRewards.size() == 1) {
				sign.setLine(3, itemRewards.get(0).getType().name());
			}

			else {
				sign.setLine(3, itemRewards.size() + " items");
			}

		} else {
			sign.setLine(3, lottery.formatPot());
		}
		sign.update(true);
	}

	public void update() {

		if (!sign.getBlock().getChunk().isLoaded()) {
			return;
		}

		sign.setLine(0, ChatColor.GREEN + "[Lottery+]");
		sign.setLine(1, lottery.getName());
		sign.setLine(2, lottery.formatTimer());
		if (lottery.isItemOnly()) {
			List<ItemStack> itemRewards = lottery.getItemRewards();

			if (itemRewards.size() == 1) {
				sign.setLine(3, itemRewards.get(0).getType().name());
			}

			else {
				sign.setLine(3, itemRewards.size() + " items");
			}

		} else {
			sign.setLine(3, lottery.formatPot());
		}
		sign.update(true);
	}

	public LotteryLocation serialize() {
		return new LotteryLocation(sign.getBlock().getLocation());
	}

	protected static final LotterySign deserialize(Lottery lottery,
			LotteryLocation location) {
		Block block = location.toLocation().getBlock();
		Sign sign = (Sign) block.getState();
		return new LotterySign(lottery, sign);
	}

}
