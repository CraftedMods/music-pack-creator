package craftedMods.utils.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ExceptionUtils {

	public static String getStackTraceAsString(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		t.printStackTrace(out);
		return writer.getBuffer().toString();
	}

	public static void executeFailableTask(FailableExecutable task, Consumer<Exception> logger,
			Consumer<Exception> errorHandler, Predicate<ErrorCode> invalidInputCodeHandler) {
		try {
			task.execute();
		} catch (InvalidInputException ie) {
			if (invalidInputCodeHandler != null) {
				if (!invalidInputCodeHandler.test(ie.getErrorCode()))
					if (logger != null)
						logger.accept(ie);
			} else if (logger != null) {
				logger.accept(ie);
			}
		} catch (Exception e) {
			if (logger != null)
				logger.accept(e);
			if (errorHandler != null)
				errorHandler.accept(e);
		}
	}
}
