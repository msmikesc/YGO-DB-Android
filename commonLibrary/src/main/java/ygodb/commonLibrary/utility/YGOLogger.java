package ygodb.commonLibrary.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YGOLogger {
	private static final Logger logger = LoggerFactory.getLogger(YGOLogger.class);

	public static void info(String input) {
		logger.info(input);
	}

	public static void debug(String input) {
		logger.debug(input);
	}

	public static void error(String input) {
		logger.error(input);
	}

}
