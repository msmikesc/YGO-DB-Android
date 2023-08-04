package ygodb.commonlibrary.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class YGOLogger {

	private YGOLogger() {
	}

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

	public static void logException(Throwable e) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		error(sw.toString());

	}

}
