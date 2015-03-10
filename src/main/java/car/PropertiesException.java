package car;

/**
 * @version Date: Jan 1, 2014 8:09:49 PM
 * @author Shang Pu
 */
public class PropertiesException extends Exception {
	private static final long serialVersionUID = 1090704100611102878L;

	public PropertiesException(Throwable t) {
		super(t);
	}

	public PropertiesException(String message) {
		super(message);
	}

	public PropertiesException(String message, Throwable t) {
		super(message, t);
	}
}
