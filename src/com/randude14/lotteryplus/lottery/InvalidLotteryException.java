package com.randude14.lotteryplus.lottery;

public class InvalidLotteryException extends RuntimeException {
	private static final long serialVersionUID = -9091643167060189764L;

	public InvalidLotteryException(String message) {
		super(message);
	}
	
	public InvalidLotteryException() {
		super();
	}

}
