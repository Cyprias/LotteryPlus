package com.randude14.lotteryplus.tasks;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.LotteryManager;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;

public class SaveTask implements Task {
	private int updateId = -1;

	public void run() {
		boolean flag = Config.getBoolean(Config.SHOULD_LOG);
		if (flag)
			Logger.info("Force saving lotteries...");
		LotteryManager.saveLotteries();
		if (flag)
			Logger.info("lotteries saved.");
	}

	public void scheduleTask() {
		long delay = Config.getLong(Config.SAVE_DELAY);
		Plugin.cancelTask(updateId);
		if (delay <= 0) {
			return;
		}
		delay *= SERVER_SECOND * MINUTE;
		updateId = Plugin.scheduleSyncRepeatingTask(this, delay, delay);
	}
}
