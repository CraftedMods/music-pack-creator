package craftedMods.utils.exceptions;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings(value = { "unchecked" })
public class ExceptionUtilsTest extends EasyMockSupport {

	public enum DummyErrorCode implements ErrorCode {
		ERROR1, ERROR2;
	}

	private Consumer<Exception> mockLogger;
	private Consumer<Exception> mockErrorHandler;
	private Predicate<ErrorCode> mockErrorCodeHandler;

	@Before
	public void setup() {
		mockLogger = this.createMock(Consumer.class);
		mockErrorHandler = this.createMock(Consumer.class);
		mockErrorCodeHandler = this.createMock(Predicate.class);
	}

	@Test
	public void testExecuteFailableTask() {
		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			// Do nothing
		}, mockLogger, mockErrorHandler, mockErrorCodeHandler);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableAllHandlersNull() {
		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			// Do nothing
		}, null, null, null);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorNoLogging() {
		EasyMock.expect(mockErrorCodeHandler.test(DummyErrorCode.ERROR1)).andReturn(true).once();

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw new InvalidInputException(DummyErrorCode.ERROR1);
		}, mockLogger, mockErrorHandler, mockErrorCodeHandler);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorLogging() {
		InvalidInputException exception = new InvalidInputException(DummyErrorCode.ERROR1);

		EasyMock.expect(mockErrorCodeHandler.test(DummyErrorCode.ERROR1)).andStubReturn(false);
		mockLogger.accept(exception);
		EasyMock.expectLastCall().once();

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw exception;
		}, mockLogger, mockErrorHandler, mockErrorCodeHandler);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorLoggingNullLogger() {
		EasyMock.expect(mockErrorCodeHandler.test(DummyErrorCode.ERROR1)).andStubReturn(false);

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw new InvalidInputException(DummyErrorCode.ERROR1);
		}, null, mockErrorHandler, mockErrorCodeHandler);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorNoLoggingNullHandler() {
		EasyMock.expect(mockErrorCodeHandler.test(DummyErrorCode.ERROR1)).andStubReturn(false);

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw new InvalidInputException(DummyErrorCode.ERROR1);
		}, null, mockErrorHandler, null);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorLoggingNullHandler() {
		InvalidInputException exception = new InvalidInputException(DummyErrorCode.ERROR1);

		EasyMock.expect(mockErrorCodeHandler.test(DummyErrorCode.ERROR1)).andStubReturn(false);
		mockLogger.accept(exception);
		EasyMock.expectLastCall().once();

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw exception;
		}, mockLogger, mockErrorHandler, null);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputError() {
		IOException exception = new IOException("Error");

		mockLogger.accept(exception);
		EasyMock.expectLastCall().once();

		mockErrorHandler.accept(exception);
		EasyMock.expectLastCall().once();

		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw exception;
		}, mockLogger, mockErrorHandler, null);

		this.verifyAll();
	}

	@Test
	public void testExecuteFailableTaskInputErrorAllNull() {
		this.replayAll();

		ExceptionUtils.executeFailableTask(() -> {
			throw new IOException("Error");
		}, null, null, null);

		this.verifyAll();
	}

}
