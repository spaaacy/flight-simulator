package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.actuator.wingflap.WingFlap.CRUISING_ALTITUDE;
import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.*;

public class TakeoffState extends TimerTask implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();

    public TakeoffState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void run() {
        int maxPossibleNextValue = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + MAX_FLUCTUATION_TAKEOFF_LANDING;
        if (maxPossibleNextValue <= CRUISING_ALTITUDE) {
            Integer fluctuation = (int)(Math.random() * MAX_FLUCTUATION_TAKEOFF_LANDING * 2) - MAX_FLUCTUATION_TAKEOFF_LANDING;
            Integer newAltitude = altitude.currentAltitude + INCREMENT_TAKEOFF_LANDING + fluctuation;
            altitude.setCurrentAltitude(newAltitude);
        } else {
            altitude.changeState(new CruisingState(altitude));
        }

        System.out.println("Altitude: " + altitude.currentAltitude);
    }

    @Override
    public void generateAltitude() {
        timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
    }

    @Override
    public void stopExecuting() {
        timer.cancel();
    }
}
