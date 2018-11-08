package es.amplia.oda.core.commons.gpio;

/**
 * GPIO pin mode representation.
 */
public enum GpioMode {
    PULL_DOWN("down"),
    PULL_UP("up"),
    OPEN_DRAIN("open_drain"),
    PUSH_PULL("push_pull");

    private String displayName;

    GpioMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
