package org.flightcontrol.actuator.wingflap;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;

public class WingFlapNeutralState implements WingFlapState {

    WingFlap wingFlap;

    public WingFlapNeutralState(WingFlap wingFlap) {
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.setDirection(WingFlapDirection.NEUTRAL);

        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
        Integer newAltitude = CRUISING_ALTITUDE + fluctuation;
        wingFlap.sendNewAltitude(newAltitude);

        // Plane flying too high
        if (newAltitude - CRUISING_ALTITUDE > ACCEPTED_DIFFERENCE) {
            wingFlap.wingFlapState = new WingFlapUpState(wingFlap);
        // Plane flying too low
        } else if (newAltitude - CRUISING_ALTITUDE < -ACCEPTED_DIFFERENCE) {
            wingFlap.wingFlapState = new WingFlapDownState(wingFlap);
        }
    }
}
