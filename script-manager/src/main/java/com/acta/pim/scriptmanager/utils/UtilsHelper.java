package com.acta.pim.scriptmanager.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class UtilsHelper {

	/**
	 * This method will extrat the stacktrace of an exception as String
	 * @param e The thrown exception
	 * @return
	 */
	public static String getExceptionTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}
	
}
