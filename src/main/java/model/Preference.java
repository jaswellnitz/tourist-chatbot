package model;

public enum Preference {
	TRUE(1, "1"), FALSE(0, "0"), NOT_RATED(-1, "-");

	private final String fieldName;
	private final int value;

	Preference(int value, String fieldName) {
		this.value = value;
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public int getValue() {
		return value;
	}

	public static Preference valueByFieldName(String fieldName) {
		if (fieldName.equals("1")) {
			return TRUE;
		} else if (fieldName.equals("0")) {
			return FALSE;
		} else {
			return NOT_RATED;
		}
	}

	public boolean toBoolean() {
		return value == 1;
	}
}
