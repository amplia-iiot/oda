package es.amplia.oda.core.commons.adc;

public enum DeviceType {
	FX30("fx30"),
	DEFAULT("default");

	private final String displayName;

	DeviceType(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
