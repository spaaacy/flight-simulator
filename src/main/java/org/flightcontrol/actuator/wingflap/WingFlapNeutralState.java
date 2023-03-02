package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_ALTITUDE;

public class WingFlapNeutralState implements WingFlapState {

    Altitude altitude;
    WingFlap wingFlap;
    Timer timer = new Timer();

    public WingFlapNeutralState(WingFlap wingFlap, Altitude altitude) {
        this.altitude = altitude;
        this.wingFlap = wingFlap;
    }

    @Override
    public void controlFlaps() {

        System.out.println("WingFlap: Neutral");

        TimerTask flapsTask = new TimerTask() {
            @Override
            public void run() {
                wingFlap.direction = Direction.NEUTRAL;
                Integer fluctuation = (int)(Math.random() * 1000) - 500;
                Integer newAltitude = CRUISING_ALTITUDE + fluctuation;
                altitude.setCurrentAltitude(newAltitude);

            }
        };

        timer.scheduleAtFixedRate(flapsTask, 0L, TICK_RATE);

    }

    @Override
    public void stopExecution() {
        timer.cancel();
    }

}
