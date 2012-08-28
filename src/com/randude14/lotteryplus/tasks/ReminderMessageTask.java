package com.randude14.lotteryplus.tasks;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.Plugin;
import com.randude14.lotteryplus.configuration.Config;

public class ReminderMessageTask implements Task {
	private int updateId = -1;
	
	public void run() {
		String reminderMessage = Config.getProperty(Config.REMINDER_MESSAGE);
		ChatUtils.broadcast(reminderMessage);
	}

	public void scheduleTask() {
		if(!Config.getProperty(Config.REMINDER_ENABLE)) {
			Plugin.cancelTask(updateId);
			return;
		}
		Plugin.cancelTask(updateId);
		long delay = Config.getProperty(Config.REMINDER_MESSAGE_TIME) * SERVER_SECOND * MINUTE;
		updateId = Plugin.scheduleSyncRepeatingTask(this, delay, delay);
	}
}
