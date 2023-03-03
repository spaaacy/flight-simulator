package org.flightcontrol.actuator.tailflap;

import static org.flightcontrol.actuator.tailflap.TailFlap.*;

public class TailFlapLeftState implements TailFlapState {

    TailFlap tailFlap;

    public TailFlapLeftState(TailFlap tailFlap) {
        this.tailFlap = tailFlap;
    }

    @Override
    public void controlFlaps() {
        tailFlap.setTailFlapDirection(TailFlapDirection.LEFT);

        int fluctuation = (int) (Math.random() * MAX_FLUCTUATION_LEFT_RIGHT * 2) - MAX_FLUCTUATION_LEFT_RIGHT;
        int newBearing = tailFlap.currentBearing + INCREMENT_VALUE_LEFT_RIGHT + fluctuation;
        tailFlap.sendNewBearing(newBearing);

        if (newBearing - BEARING_DESTINATION > -ACCEPTED_DIFFERENCE) {
            tailFlap.tailFlapState = new TailFlapNeutralState(tailFlap);
        }

    }
}
