package org.flightcontrol.actuator.tailflap;

import static org.flightcontrol.actuator.tailflap.TailFlap.FLUCTUATION_OFF_COURSE;
import static org.flightcontrol.actuator.tailflap.TailFlap.MAX_FLUCTUATION_NEUTRAL;
import static org.flightcontrol.sensor.gps.GPS.BEARING_DESTINATION;
import static org.flightcontrol.sensor.gps.GPS.GPS_ACCEPTED_DIFFERENCE;

public class TailFlapNeutralState implements TailFlapState {


    TailFlap tailFlap;

    public TailFlapNeutralState(TailFlap tailFlap) {
        this.tailFlap = tailFlap;
    }

    @Override
    public void controlFlaps() {
        tailFlap.setTailFlapDirection(TailFlapDirection.NEUTRAL);

        int newBearing;
        if (tailFlap.onCourse) {
            int fluctuation = (int)(Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
            newBearing = BEARING_DESTINATION + fluctuation;
        } else {
            int fluctuation = (int)(Math.random() * FLUCTUATION_OFF_COURSE * 2) - FLUCTUATION_OFF_COURSE;
            newBearing = tailFlap.currentBearing + fluctuation;
        }
        tailFlap.sendNewBearing(newBearing);

        // Plane flying too far right
        if (!tailFlap.isTakingOffOrLanding) {
            if (newBearing - BEARING_DESTINATION > GPS_ACCEPTED_DIFFERENCE) {
                tailFlap.tailFlapState = new TailFlapRightState(tailFlap);
        // Plane flying too far left
            } else if (newBearing - BEARING_DESTINATION < -GPS_ACCEPTED_DIFFERENCE) {
                tailFlap.tailFlapState = new TailFlapLeftState(tailFlap);
            }
        }
    }
}
