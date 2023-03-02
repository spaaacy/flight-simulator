package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class WingFlapCruisingState implements WingFlapState {

    Altitude altitude;
    WingFlap wingFlap;
    Timer timer;


    public WingFlapCruisingState(Altitude altitude, WingFlap wingFlap) {
        this.altitude = altitude;
        this.wingFlap = wingFlap;
    }

    @Override
    public void adjustDirection() {
        TimerTask cruisingTask = new TimerTask() {
            @Override
            public void run() {
                Integer currentAltitude = altitude.getCurrentAltitude();

                if (currentAltitude - CRUISING_ALTITUDE > 250) {
                    wingFlap.direction = Direction.UP;
                    Integer fluctuation = (int)(Math.random() * 200) - 100;
                    Integer newAltitude = currentAltitude - fluctuation;
                    altitude.setCurrentAltitude(newAltitude);
                } else if (currentAltitude - CRUISING_ALTITUDE < -250) {
                    wingFlap.direction = Direction.DOWN;
                    Integer fluctuation = (int)(Math.random() * 200) - 100;
                    Integer newAltitude = currentAltitude + fluctuation;
                    altitude.setCurrentAltitude(newAltitude);
                } else {
                    wingFlap.direction = Direction.NEUTRAL;
                    Integer fluctuation = (int)(Math.random() * 1000) - 500;
                    Integer newAltitude = CRUISING_ALTITUDE + fluctuation;
                    altitude.setCurrentAltitude(newAltitude);

                }

            }
        };

        timer.scheduleAtFixedRate(cruisingTask, 0L ,TICK_RATE);
    }
}
