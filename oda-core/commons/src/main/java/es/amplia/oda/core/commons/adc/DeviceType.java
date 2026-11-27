package es.amplia.oda.core.commons.adc;

public enum DeviceType {
	OWASYS("owasys"),
	DEFAULT("default");

	private final String displayName;

	DeviceType(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

	public static DeviceType typeOf(String device) {
		switch (device) {
			case "owasys":
			case "OWASYS":
				return OWASYS;
			default:
				return DEFAULT;
		}
	}
}
