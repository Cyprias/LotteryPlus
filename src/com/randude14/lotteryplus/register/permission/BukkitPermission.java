package com.randude14.lotteryplus.register.permission;

import org.bukkit.entity.Player;

public class BukkitPermission extends Permission {

	public boolean playerHas(Player player, String permission) {
		return player.hasPermission(permission);
	}
}
