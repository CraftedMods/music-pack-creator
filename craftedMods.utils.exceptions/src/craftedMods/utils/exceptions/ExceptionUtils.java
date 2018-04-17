package craftedMods.utils.exceptions;

import java.io.*;
import java.util.function.*;
import java.util.logging.*;

public class ExceptionUtils {

	public static String getStackTraceAsString(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		t.printStackTrace(out);
		return writer.getBuffer().toString();
	}

	public static void executeFailableTask(FailableExecutable task, Logger logger, String loggedMessage, Consumer<Exception> errorHandler,
			Predicate<ErrorCode> invalidInputCodeHandler) {
		try {
			task.execute();
		} catch (InvalidInputException ie) {
			if (invalidInputCodeHandler != null) {
				if (!invalidInputCodeHandler.test(ie.getErrorCode())) {
					if (logger != null) logger.log(Level.SEVERE, loggedMessage, ie);
				}
			}
		} catch (Exception e) {
			if (logger != null) logger.log(Level.SEVERE, loggedMessage, e);
			if (errorHandler != null) errorHandler.accept(e);
		}
	}

}
