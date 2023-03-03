package org.flightcontrol.actuator.wingflap;

import java.util.Timer;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;

public class WingFlapDownState implements WingFlapState {

    WingFlap wingFlap;
    Timer timer = new Timer();

    public WingFlapDownState(WingFlap wingFlap) {
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.setDirection(WingFlapDirection.DOWN);

        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_UP_DOWN * 2) - MAX_FLUCTUATION_UP_DOWN;
        Integer newAltitude = wingFlap.currentAltitude + INCREMENT_VALUE_UP_DOWN + fluctuation;
        wingFlap.sendNewAltitude(newAltitude);

        // Checks to see if plane is now within acceptable range
        if (newAltitude - CRUISING_ALTITUDE > -ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapNeutralState(wingFlap));
        }

    }

    @Override
    public void stopExecution() {
        timer.cancel();
    }

}
