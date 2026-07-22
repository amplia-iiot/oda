package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;

import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;

final class DioZeroGpioMapper {

    // Hide public default constructor.
    private DioZeroGpioMapper() {}

    /**
     * Map the ODA GPIO mode to the diozero pull up/down configuration.
     *
     * Only the input pull modes have an equivalent in diozero. The output drive modes
     * (OPEN_DRAIN, PUSH_PULL) have no pull configuration — diozero always drives output
     * pins push-pull — so they map to {@link GpioPullUpDown#NONE}.
     */
    static GpioPullUpDown mapGpioModeToDioZeroPullUpDown(GpioMode mode) {
        return switch (mode) {
            case PULL_UP -> GpioPullUpDown.PULL_UP;
            case PULL_DOWN -> GpioPullUpDown.PULL_DOWN;
            default -> GpioPullUpDown.NONE;
        };
    }

    /**
     * Map the ODA (logical) GPIO trigger to the diozero (physical) event trigger.
     *
     * ODA triggers are expressed in "active" terms, so when the pin is active low the
     * logical edge is inverted with respect to the physical edge (preserving the previous
     * active-low semantics). diozero only supports edge triggers, so the level triggers are
     * degraded to their closest edge equivalent.
     */
    static GpioEventTrigger mapGpioTriggerToDioZeroEventTrigger(GpioTrigger trigger, boolean activeLow) {
        return switch (trigger) {
            case NONE -> GpioEventTrigger.NONE;
            case RISING_EDGE, HIGH_LEVEL -> !activeLow ? GpioEventTrigger.RISING : GpioEventTrigger.FALLING;
            case FALLING_EDGE, LOW_LEVEL -> !activeLow ? GpioEventTrigger.FALLING : GpioEventTrigger.RISING;
            case BOTH_EDGES, BOTH_LEVELS -> GpioEventTrigger.BOTH;
            default -> GpioEventTrigger.NONE;
        };
    }
}
