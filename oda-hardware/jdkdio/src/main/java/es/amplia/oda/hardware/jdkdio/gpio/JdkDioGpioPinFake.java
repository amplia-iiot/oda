package es.amplia.oda.hardware.jdkdio.gpio;

public class
JdkDioGpioPinFake /*implements GpioPin*/{
/*
	private final int index;
	private final String name;
	private final GpioDirection direction;
	private final GpioMode mode;
	private final GpioTrigger trigger;
	private final boolean activeLow;
	private final boolean initialValue;

	private GPIOPin jdkDioPinFake;

	public JdkDioGpioPinFake(int index, String name, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
						 boolean activeLow, boolean initialValue) {
		this.index = index;
		this.name = name;
		this.direction = direction;
		this.mode = mode;
		this.trigger = trigger;
		this.activeLow = activeLow;
		this.initialValue = initialValue;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public String getName() {
		return name != null ? name : String.valueOf(index);
	}

	@Override
	public GpioDirection getDirection() {
		return direction;
	}

	@Override
	public GpioMode getMode() {
		return mode;
	}

	@Override
	public GpioTrigger getTrigger() {
		return trigger;
	}

	@Override
	public boolean isActiveLow() {
		return activeLow;
	}

	@Override
	public boolean getInitialValue() {
		return initialValue;
	}

	@Override
	public boolean isOpen() {
		return jdkDioPinFake != null;
	}

	@Override
	public void open() {
		GPIOPinConfig.Builder builder = new GPIOPinConfig.Builder();

		try {
			GPIOPinConfig pinConfig = builder.setPinNumber(index)
					.setDirection(JdkGpioMapper.mapGpioDirectionToJdkGpioDirection(direction))
					.setDriveMode(JdkGpioMapper.mapGpioModeToJdkGpioMode(mode))
					.setTrigger(JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(trigger, activeLow))
					.setInitValue(initialValue)
					.build();

			jdkDioPinFake = new GPIOPinFake(pinConfig);
		} catch (IOException exception) {
			throw new GpioDeviceException("Unable to open GPIO pin " + getName() + ": " + exception, exception);
		}
	}

	@Override
	public void close() {
		try {
			jdkDioPinFake.close();
			jdkDioPinFake = null;
		} catch (IOException ioexception) {
			throw new GpioDeviceException("Unable to close GPIO pin " + getName() + ": " + ioexception);
		}
	}

	@Override
	public boolean getValue() {
		checkIsOpen("get GPIO pin value");

		try {
			return jdkDioPinFake.getValue() ^ activeLow;
		} catch (IOException e) {
			throw new GpioDeviceException("Unable to get GPIO pin value: " + e, e);
		}
	}

	private void checkIsOpen(String operationDescription) {
		if (!isOpen()) {
			throw new GpioDeviceException("Unable to " + operationDescription + ": GPIO pin is closed");
		}
	}

	@FunctionalInterface
	private interface GpioOperation {
		void process() throws IOException;
	}

	private void doNothingOperation(String operationDescription, JdkDioGpioPinFake.GpioOperation operation) {
		checkIsOpen(operationDescription);
	}

	@Override
	public void setValue(boolean value) {
		doNothingOperation("set GPIO pin value", () -> jdkDioPinFake.setValue(value ^ activeLow));
	}

	@Override
	public void addGpioPinListener(GpioPinListener listener) {
		doNothingOperation("add GPIO pin listener", () ->  {
			JdkDioGpioPinListenerBridge pinListenerBridge = new JdkDioGpioPinListenerBridge(listener, activeLow);
			jdkDioPinFake.setInputListener(pinListenerBridge);
		});
	}

	@Override
	public void removeGpioPinListener() {
		doNothingOperation("remove GPIO pin listener", () -> jdkDioPinFake.setInputListener(null));
	}*/
}
