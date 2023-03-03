package org.flightcontrol.actuator.tailflap;

import static org.flightcontrol.actuator.tailflap.TailFlap.*;

public class TailFlapNeutralState implements TailFlapState {


    TailFlap tailFlap;

    public TailFlapNeutralState(TailFlap tailFlap) {
        this.tailFlap = tailFlap;
    }

    @Override
    public void controlFlaps() {
        tailFlap.setTailFlapDirection(TailFlapDirection.NEUTRAL);

        Integer fluctuation = (int)(Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
        int newBearing;
        if (tailFlap.onCourse) {
            newBearing = BEARING_DESTINATION + fluctuation;
        } else {
            newBearing = tailFlap.currentBearing + fluctuation;
        }
        tailFlap.sendNewBearing(newBearing);

        // Plane flying too far right
        if (newBearing - BEARING_DESTINATION > ACCEPTED_DIFFERENCE) {
            tailFlap.tailFlapState = new TailFlapRightState(tailFlap);
            tailFlap.onCourse = true;
            // Plane flying too far left
        } else if (newBearing - BEARING_DESTINATION < -ACCEPTED_DIFFERENCE) {
            tailFlap.tailFlapState = new TailFlapLeftState(tailFlap);
            tailFlap.onCourse = true;
        }
    }
}
