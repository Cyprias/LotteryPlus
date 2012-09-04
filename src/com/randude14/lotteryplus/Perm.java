package com.randude14.lotteryplus;

import org.bukkit.plugin.PluginManager;

public enum Perm {
	
	LIST("lottery.basic.list", "You do not have permission to list lotteries."),
	INFO("lottery.basic.info", "You do not have permission to look at a lotteries info."),
	BUY("lottery.basic.buy", "You do not have permission to buy tickets for lotteries."),
	CLAIM("lottery.basic.claim", "You do not have permission to claim rewards from lotteries."),
	WINNERS("lottery.basic.winners", "You do not have permission to list winners."),
	REWARD("lottery.admin.reward", "You do not have permission to reward players."),
	DRAW("lottery.admin.draw", "You do not have permission to force draw lotteries."),
	RELOAD("lottery.admin.reload", "You do not have permission to reload lotteries."),
	RELOAD_ALL("lottery.admin.reloadall", "You do not have permission to reload lotteries."),
	CONFIG_RELOAD("lottery.admin.creload", "You do not have permission to reload the config."),
	LOAD("lottery.admin.load", "You do not have permission to load lotteries."),
	UNLOAD("lottery.admin.unload", "You do not have permission to unload lotteries."),
	FORCE_SAVE("lottery.admin.save", "You do not have permission to force save lotteries."),
	UPDATE("lottery.admin.update", "You do not have permission to check for updates."),
	ADD_TO_POT("lottery.admin.addtopot", "You do not have permission to add to a lotteries pot."),
	CREATE("lottery.admin.create", "You do not have permission to create a lottery section."),
	SIGN_CREATE("lottery.sign.create", "You do not have permission to create signs."),
	SIGN_REMOVE("lottery.sign.remove", "You do not have permission to remove signs."),
	SIGN_USE("lottery.sign.use", "You do not have permission to use signs."),
	PARENT_BASIC("lottery.basic.*", LIST, INFO, BUY, CLAIM, WINNERS),
	PARENT_ADMIN("lottery.admin.*", REWARD, DRAW, RELOAD, RELOAD_ALL, LOAD, UNLOAD, UPDATE, ADD_TO_POT, CREATE),
	PARENT_SIGN("lottery.sign.*", SIGN_CREATE, SIGN_REMOVE, SIGN_USE),
	SUPER_PERM("lottery.*", PARENT_BASIC, PARENT_ADMIN, PARENT_SIGN);
	
	private Perm(String value, Perm... childrenArray) {
		this(value, DEFAULT_ERROR_MESSAGE, childrenArray);
	}
	
	private Perm(String perm, String errorMess) {
		this.permission = perm;
		this.errorMessage = errorMess;
		this.bukkitPerm = new org.bukkit.permissions.Permission(permission);
	}
	
	private Perm(String value, String errorMess, Perm... childrenArray) {
		this(value, DEFAULT_ERROR_MESSAGE);
		for(Perm child : childrenArray) {
			child.setParent(this);
		}
	}
	
	public void loadPermission(PluginManager pm) {
		pm.addPermission(bukkitPerm);
	}
	
	private void setParent(Perm parentValue) {
		if(this.parent != null)
			return;
		this.parent = parentValue;
	}
	
	public Perm getParent() {
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
	
	public static final String DEFAULT_ERROR_MESSAGE = "You do not have permission";
	private final org.bukkit.permissions.Permission bukkitPerm;
	private Perm parent;
	private final String permission;
	private final String errorMessage;
}
