package org.flightcontrol.sensor.altitude;

import java.util.Timer;
import java.util.TimerTask;

import static org.flightcontrol.sensor.altitude.Altitude.*;

public class TakeoffState implements AltitudeState {

    Altitude altitude;
    Timer timer = new Timer();

    public TakeoffState(Altitude altitude) {
        this.altitude = altitude;
    }

    @Override
    public void generateAltitude() {

        TimerTask takeoffTask = new TimerTask() {
            @Override
            public void run() {
                if (altitude.currentAltitude + 100 <= CRUISING_ALTITUDE) {
                    Integer fluctuation = (int)(Math.random() * 200) - 100;
                    altitude.currentAltitude += TAKEOFF_LANDING_INCREMENT + fluctuation;
                    System.out.println("Altitude: " + altitude.currentAltitude);
                } else {
                    timer.cancel();
                    System.out.println("Altitude: Cruising");
                    altitude.changeState(new CruisingState(altitude));
                    altitude.phaser.arrive();
                    System.out.println("Phaser: " + altitude.phaser.getPhase());
                }
            }
        };

        timer.scheduleAtFixedRate(takeoffTask, 0L, UPDATE_RATE);

    }


}
