package com.randude14.lotteryplus.tasks;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;

public class SaveTask implements Task {
	private int updateId = -1;
	
	public void run() {
		Logger.info("Force saving lotteries...");
		LotteryManager.saveLotteries();
		Logger.info("lotteries saved.");
	}

	public void scheduleTask() {
		Plugin.cancelTask(updateId);
		long delay = Config.getProperty(Config.SAVE_DELAY) * SERVER_SECOND * MINUTE;
		updateId = Plugin.scheduleSyncRepeatingTask(this, delay, delay);
	}
}
