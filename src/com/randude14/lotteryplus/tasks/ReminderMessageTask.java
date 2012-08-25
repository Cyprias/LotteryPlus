package com.randude14.lotteryplus.tasks;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.configuration.Config;

public class ReminderMessageTask implements Runnable {
	
	public ReminderMessageTask() {
		
	}
	
	public void run() {
		String reminderMessage = Config.getProperty(Config.REMINDER_MESSAGE);
		ChatUtils.broadcast(reminderMessage);
	}

}
