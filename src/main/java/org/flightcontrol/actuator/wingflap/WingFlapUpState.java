package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;

public class WingFlapUpState implements WingFlapState {

    WingFlap wingFlap;
    Altitude altitude;
    Timer timer = new Timer();

    public WingFlapUpState(WingFlap wingFlap, Altitude altitude) {
        this.wingFlap = wingFlap;
        this.altitude = altitude;
    }

    @Override
    public void controlFlaps() {
        System.out.println("WingFlap: Up");
        Integer currentAltitude = altitude.getCurrentAltitude();

        wingFlap.direction = Direction.UP;
        Integer fluctuation = (int) (Math.random() * 200) - 100;
        Integer newAltitude = currentAltitude - fluctuation;
        altitude.setCurrentAltitude(newAltitude);
    }

    @Override
    public void stopExecution() {
        timer.cancel();
    }
}
