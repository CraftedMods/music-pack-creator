package craftedMods.utils.exceptions;

public class InvalidInputException extends Exception {

	private static final long serialVersionUID = -1971169171481583089L;

	private final ErrorCode errorCode;

	public InvalidInputException() {
		this(null, null);
	}

	public InvalidInputException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public InvalidInputException(String message) {
		this(message, null);
	}

	public InvalidInputException(ErrorCode errorCode) {
		this(errorCode.toString(), errorCode);
	}

	public ErrorCode getErrorCode() {
		return this.errorCode;
	}

}
