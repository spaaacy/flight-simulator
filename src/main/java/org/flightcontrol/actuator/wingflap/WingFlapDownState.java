package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;

public class WingFlapDownState implements WingFlapState {

    WingFlap wingFlap;
    Altitude altitude;
    Timer timer = new Timer();

    public WingFlapDownState(WingFlap wingFlap, Altitude altitude) {
        this.wingFlap = wingFlap;
        this.altitude = altitude;
    }

    @Override
    public void controlFlaps() {

        System.out.println("WingFlap: Down");

        TimerTask flapsTask = new TimerTask() {
            @Override
            public void run() {
                Integer currentAltitude = altitude.getCurrentAltitude();
                wingFlap.direction = Direction.DOWN;
                Integer fluctuation = (int)(Math.random() * 200) - 100;
                Integer newAltitude = currentAltitude + fluctuation;
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
