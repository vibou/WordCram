package wordcram.animation.svg;

/**
 * The Class Attribute.
 */
public class Attribute {

	/** The key. */
	private final String	key;

	/** The value. */
	private final String	value;

	/**
	 * Instantiates a new attribute.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public Attribute(final String key, final String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
