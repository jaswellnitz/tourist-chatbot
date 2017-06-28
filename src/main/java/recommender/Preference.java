package recommender;

/**
 * Enum that indicates the preference for tourist categories. 
 * Extends the primitive boolean as a "NOT_RATED" value is included, 
 * showing whether a user has not mentioned a preference for a certain tourist category.
 * @author Jasmin Wellnitz
 *
 */
public enum Preference {
	TRUE(1, "1"), FALSE(0, "0"), NOT_RATED(-1, "-");

	private final String fieldName;
	private final int value;

	Preference(int value, String fieldName) {
		this.value = value;
		this.fieldName = fieldName;
	}

	/**
	 * Gets the corresponding string representation.
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Gets the corresponding integer representation.
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Converts the string representation into the enum value.
	 * @param fieldName
	 * @return
	 */
	public static Preference valueByFieldName(String fieldName) {
		if (fieldName.equals("1")) {
			return TRUE;
		} else if (fieldName.equals("0")) {
			return FALSE;
		} else {
			return NOT_RATED;
		}
	}

	/**
	 * Converts the Preference value to boolean by matching FALSE and NOT_RATED to false and TRUE to true.
	 * @return
	 */
	public boolean toBoolean() {
		return value == 1;
	}
}
