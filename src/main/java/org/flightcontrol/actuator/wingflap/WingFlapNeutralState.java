package org.flightcontrol.actuator.wingflap;

import static org.flightcontrol.actuator.wingflap.WingFlap.MAX_FLUCTUATION_NEUTRAL;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_ACCEPTED_DIFFERENCE;

public class WingFlapNeutralState implements WingFlapState {

    WingFlap wingFlap;

    public WingFlapNeutralState(WingFlap wingFlap) {
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.setWingFlapDirection(WingFlapDirection.NEUTRAL);

        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
        Integer newAltitude = wingFlap.targetAltitude + fluctuation;
        wingFlap.sendNewAltitude(newAltitude);

        // Plane flying too high
        if (newAltitude - wingFlap.targetAltitude > ALTITUDE_ACCEPTED_DIFFERENCE) {
            wingFlap.wingFlapState = new WingFlapUpState(wingFlap);
        // Plane flying too low
        } else if (newAltitude - wingFlap.targetAltitude < -ALTITUDE_ACCEPTED_DIFFERENCE) {
            wingFlap.wingFlapState = new WingFlapDownState(wingFlap);
        }
    }
}
