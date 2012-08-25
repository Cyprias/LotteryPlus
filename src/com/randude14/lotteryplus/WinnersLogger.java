package com.randude14.lotteryplus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class WinnersLogger extends java.util.logging.Logger {
	private static final Plugin plugin = Plugin.getInstance();
	private static final String winnersLogString = plugin.getDataFolder() + "/winners.log";
	private static final File winnersLogFile = new File(winnersLogString);
	
	private WinnersLogger() {
		super("WinnersLogger", null);
		try {
			if(!winnersLogFile.exists())
				winnersLogFile.createNewFile();
			FileHandler handler = new FileHandler(winnersLogString);
			handler.setFormatter(new WinnerFormatter());
			addHandler(handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class WinnerFormatter extends Formatter {
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat("[yyyy-MMM-ddd] [hh:mm:ss]");
		public String format(LogRecord record) {
			StringBuffer sb = new StringBuffer();
			sb.append(dateFormatter.format(new Date()));
			sb.append(" - ");
			sb.append(record.getMessage());
			return sb.toString();
		}
		
	}
	
	private static final WinnersLogger logger = new WinnersLogger();
	
	public static void log(String record) {
		logger.fine(record);
	}
}
