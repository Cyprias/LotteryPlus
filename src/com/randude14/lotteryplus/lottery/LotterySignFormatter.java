package com.randude14.lotteryplus.lottery;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import com.randude14.lotteryplus.ChatUtils;
import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.util.FormatOptions;
import com.randude14.lotteryplus.util.SignFormatter;

public class LotterySignFormatter implements SignFormatter, FormatOptions {
	private final Lottery lottery;

	public LotterySignFormatter(Lottery lottery) {
		this.lottery = lottery;
	}

	public void format(Sign sign) {
		sign.setLine(0, ChatColor.GREEN + "[Lottery+]");

		if (lottery.isRunning()) {
			sign.setLine(1, format(Config.getProperty(Config.NORMAL_LINE_ONE)));
			sign.setLine(2, format(Config.getProperty(Config.NORMAL_LINE_TWO)));
			sign.setLine(3, format(Config.getProperty(Config.NORMAL_LINE_THREE)));
		}

		else if (lottery.isDrawing()) {
			sign.setLine(1, format(Config.getProperty(Config.DRAWING_LINE_ONE)));
			sign.setLine(2, format(Config.getProperty(Config.DRAWING_LINE_ONE)));
			sign.setLine(3, format(Config.getProperty(Config.DRAWING_LINE_ONE)));
		}

		else {
			sign.setLine(1, format(Config.getProperty(Config.OVER_LINE_ONE)));
			sign.setLine(2, format(Config.getProperty(Config.OVER_LINE_ONE)));
			sign.setLine(3, format(Config.getProperty(Config.OVER_LINE_ONE)));
		}

	}

	private String format(String format) {
		String message = ChatUtils.replaceColorCodes(format);
		message = lottery.format(message);
		return message;
	}

}
