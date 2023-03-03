package org.flightcontrol.actuator.tailflap;

import static org.flightcontrol.actuator.tailflap.TailFlap.*;
import static org.flightcontrol.actuator.tailflap.TailFlap.ACCEPTED_RANGE;

public class TailFlapRightState implements TailFlapState {

    TailFlap tailFlap;

    public TailFlapRightState(TailFlap tailFlap) {
        this.tailFlap = tailFlap;
    }

    @Override
    public void controlFlaps() {
        tailFlap.setTailFlapDirection(TailFlapDirection.RIGHT);

        int fluctuation = (int) (Math.random() * MAX_FLUCTUATION_LEFT_RIGHT * 2) - MAX_FLUCTUATION_LEFT_RIGHT;
        int newBearing = tailFlap.currentBearing - INCREMENT_VALUE_LEFT_RIGHT + fluctuation;
        tailFlap.sendNewBearing(newBearing);

        if (newBearing - BEARING_DESTINATION < ACCEPTED_RANGE ) {
            tailFlap.tailFlapState = new TailFlapNeutralState(tailFlap);
        }

    }
}
