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
        switch (mode) {
            case PULL_UP:
                return GpioPullUpDown.PULL_UP;
            case PULL_DOWN:
                return GpioPullUpDown.PULL_DOWN;
            default:
                return GpioPullUpDown.NONE;
        }
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
        switch (trigger) {
            case NONE:
                return GpioEventTrigger.NONE;
            case RISING_EDGE:
            case HIGH_LEVEL:
                return !activeLow ? GpioEventTrigger.RISING : GpioEventTrigger.FALLING;
            case FALLING_EDGE:
            case LOW_LEVEL:
                return !activeLow ? GpioEventTrigger.FALLING : GpioEventTrigger.RISING;
            case BOTH_EDGES:
            case BOTH_LEVELS:
                return GpioEventTrigger.BOTH;
            default:
                return GpioEventTrigger.NONE;
        }
    }
}
