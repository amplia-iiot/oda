package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.*;

import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JdkDioGpioPin implements GpioPin {

    static final String GPIO_BASE_PATH = "/sys/class/gpio";

    private final int index;
    private final String name;
    private final GpioDirection direction;
    private final GpioMode mode;
    private final GpioTrigger trigger;
    private final boolean activeLow;
    private final boolean initialValue;

    private GPIOPin jdkDioPin;

    public JdkDioGpioPin(int index, String name, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
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
        return jdkDioPin != null && jdkDioPin.isOpen();
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

            jdkDioPin = JdkDioGpioPinFactory.createAndOpen(pinConfig);
            initModeIfNeeded();
        } catch (IOException exception) {
            throw new GpioDeviceException("Unable to open GPIO pin " + getName() + ": " + exception, exception);
        }
    }

    private void initModeIfNeeded() throws IOException {
        if (direction == GpioDirection.INPUT) {
            Files.write(Paths.get(getPinModeFilePath()), mode.toString().getBytes());
        }
    }

    private String getPinModeFilePath() {
        return GPIO_BASE_PATH + "/gpio" + index + "/pull";
    }

    @Override
    public void close() {
        try {
            jdkDioPin.close();
            jdkDioPin = null;
        } catch (IOException ioexception) {
            throw new GpioDeviceException("Unable to close GPIO pin " + getName() + ": " + ioexception);
        }
    }

    @Override
    public boolean getValue() {
        checkIsOpen("get GPIO pin value");

        try {
            return jdkDioPin.getValue() ^ activeLow;
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

    private void performGpioPinOperation(String operationDescription, GpioOperation operation) {
        checkIsOpen(operationDescription);

        try {
            operation.process();
        } catch (IOException e) {
            throw new GpioDeviceException("Unable to " + operationDescription + ": " + e, e);
        }
    }

    @Override
    public void setValue(boolean value) {
        performGpioPinOperation("set GPIO pin value", () -> jdkDioPin.setValue(value ^ activeLow));
    }

    @Override
    public void addGpioPinListener(GpioPinListener listener) {
        performGpioPinOperation("add GPIO pin listener", () ->  {
            JdkDioGpioPinListenerBridge pinListenerBridge = new JdkDioGpioPinListenerBridge(listener, activeLow);
            jdkDioPin.setInputListener(pinListenerBridge);
        });
    }

    @Override
    public void removeGpioPinListener() {
        performGpioPinOperation("remove GPIO pin listener", () -> jdkDioPin.setInputListener(null));
    }
}
