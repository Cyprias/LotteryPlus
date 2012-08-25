package com.randude14.lotteryplus.tasks;

import com.randude14.lotteryplus.Logger;
import com.randude14.lotteryplus.LotteryManager;

public class SaveTask implements Runnable {
	
	public SaveTask() {
		
	}
	
	public void run() {
		Logger.info("Force saving lotteries...");
		LotteryManager.saveLotteries();
		Logger.info("lotteries saved.");
	}
}
