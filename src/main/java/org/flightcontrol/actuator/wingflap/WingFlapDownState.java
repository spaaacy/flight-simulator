package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;

public class WingFlapDownState implements WingFlapState {

    static final String POSITION_NAME = "DOWN";

    WingFlap wingFlap;
    Altitude altitude;
    Timer timer = new Timer();

    public WingFlapDownState(WingFlap wingFlap, Altitude altitude) {
        this.wingFlap = wingFlap;
        this.altitude = altitude;
    }

    @Override
    public void controlFlaps() {
        wingFlap.direction = Direction.DOWN;

        Integer currentAltitude = altitude.getCurrentAltitude();
        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_UP_DOWN * 2) - MAX_FLUCTUATION_UP_DOWN;
        Integer newAltitude = currentAltitude + INCREMENT_VALUE_UP_DOWN + fluctuation;
        altitude.setCurrentAltitude(newAltitude);

        // Checks to see if plane is now within acceptable range
        if (newAltitude - CRUISING_ALTITUDE > -ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapNeutralState(wingFlap, altitude));
        }

    }

    @Override
    public void stopExecution() {
        timer.cancel();
    }

    @Override
    public String toString() {
        return POSITION_NAME;
    }

}
