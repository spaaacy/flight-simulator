package org.flightcontrol.actuator.wingflap;

import java.util.Timer;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;

public class WingFlapNeutralState implements WingFlapState {

    WingFlap wingFlap;
    Timer timer = new Timer();

    public WingFlapNeutralState(WingFlap wingFlap) {
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.setDirection(WingFlapDirection.NEUTRAL);

        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
        Integer newAltitude = wingFlap.currentAltitude + fluctuation;
        wingFlap.sendNewAltitude(newAltitude);

        // Plane flying too high
        if (newAltitude - CRUISING_ALTITUDE > ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapUpState(wingFlap));
        // Plane flying too low
        } else if (newAltitude - CRUISING_ALTITUDE < -ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapDownState(wingFlap));
        }
    }

    @Override
    public void stopExecution() {
        timer.cancel();
    }

}
