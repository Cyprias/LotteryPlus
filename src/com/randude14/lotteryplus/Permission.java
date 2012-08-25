package com.randude14.lotteryplus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.PluginManager;

public enum Permission {
	
	LIST("lottery.list", "You do not have permission to list lotteries."),
	INFO("lottery.info", "You do not have permission to look at a lotteries info."),
	BUY("lottery.buy", "You do not have permission to buy tickets for lotteries."),
	ADD_TO_POT("lottery.addtopot", "You do not have permission to add to a lotteries pot."),
	WINNERS("lottery.winners", "You do not have permission to list winners."),
	REWARD("lottery.admin.reward", "You do not have permission to reward players."),
	DRAW("lottery.admin.draw", "You do not have permission to force draw lotteries."),
	RELOAD("lottery.admin.reload", "You do not have permission to reload."),
	SIGN_CREATE("lottery.sign.create", "You do not have permission to create signs."),
	SIGN_REMOVE("lottery.sign.remove", "You do not have permission to remove signs."),
	PARENT_BASIC("lottery.basic.*", LIST, INFO, BUY, ADD_TO_POT, WINNERS),
	PARENT_ADMIN("lottery.admin.*", REWARD, DRAW, RELOAD),
	PARENT_SIGN("lottery.sign.*", SIGN_CREATE, SIGN_REMOVE),
	SUPER_PERM("lottery.*", PARENT_BASIC, PARENT_ADMIN, PARENT_SIGN);
	
	private Permission(String value, Permission... childrenArray) {
		this(value, DEFAULT_ERROR_MESSAGE, childrenArray);
	}
	
	private Permission(String perm, String errorMess) {
		this.permission = perm;
		this.errorMessage = errorMess;
		this.bukkitPerm = new org.bukkit.permissions.Permission(permission);
	}
	
	private Permission(String value, String errorMess, Permission... childrenArray) {
		this(value, DEFAULT_ERROR_MESSAGE);
		for(Permission child : childrenArray) {
			this.children.add(child);
			child.setParent(this);
			child.getBukkitPerm().addParent(bukkitPerm, false);
		}
	}
	
	public void loadPermission(PluginManager pm) {
		pm.addPermission(bukkitPerm);
	}
	
	private void setParent(Permission parentValue) {
		this.parent = parentValue;
	}
	
	public Permission getParent() {
		return parent;
	}
	
	public org.bukkit.permissions.Permission getBukkitPerm() {
		return bukkitPerm;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean hasPermission(net.milkbowl.vault.permission.Permission perm, String player, String world) {
		if(perm.has(world, player, permission))
			return true;
		return (parent != null) ? parent.hasPermission(perm, player, world) : false;
	}
	
	public static final String DEFAULT_ERROR_MESSAGE = "You do not have permission";
	private final org.bukkit.permissions.Permission bukkitPerm;
	private final List<Permission> children = new ArrayList<Permission>();
	private Permission parent;
	private final String permission;
	private final String errorMessage;
}
