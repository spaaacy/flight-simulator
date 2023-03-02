package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;

import static org.flightcontrol.actuator.wingflap.WingFlap.*;

public class WingFlapNeutralState implements WingFlapState {

    static final String POSITION_NAME = "NEUTRAL";

    Altitude altitude;
    WingFlap wingFlap;
    Timer timer = new Timer();

    public WingFlapNeutralState(WingFlap wingFlap, Altitude altitude) {
        this.altitude = altitude;
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {
        wingFlap.direction = Direction.NEUTRAL;

        Integer currentAltitude = altitude.getCurrentAltitude();
        Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION_NEUTRAL * 2) - MAX_FLUCTUATION_NEUTRAL;
        Integer newAltitude = currentAltitude + fluctuation;
        altitude.setCurrentAltitude(newAltitude);

        // Plane flying too high
        if (newAltitude - CRUISING_ALTITUDE > ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapUpState(wingFlap, altitude));
        // Plane flying too low
        } else if (newAltitude - CRUISING_ALTITUDE < -ACCEPTED_RANGE) {
            wingFlap.setWingFlapState(new WingFlapDownState(wingFlap, altitude));
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
