package org.flightcontrol.actuator.wingflap;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ACCEPTED_DIFFERENCE;
import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_ALTITUDE;

public class WingFlapUpState implements WingFlapState {

    WingFlap wingFlap;

    public WingFlapUpState(WingFlap wingFlap) {
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.setDirection(WingFlapDirection.UP);

        int fluctuation = (int) (Math.random() * MAX_FLUCTUATION_UP_DOWN * 2) - MAX_FLUCTUATION_UP_DOWN;
        Integer newAltitude = wingFlap.currentAltitude - INCREMENT_VALUE_UP_DOWN + fluctuation;
        wingFlap.sendNewAltitude(newAltitude);

        // Checks to see if plane is now within acceptable range
        if (newAltitude - CRUISING_ALTITUDE < ALTITUDE_ACCEPTED_DIFFERENCE) {
            wingFlap.wingFlapState = new WingFlapNeutralState(wingFlap);
        }

    }
}