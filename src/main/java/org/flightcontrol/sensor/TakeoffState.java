package org.flightcontrol.sensor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

public class TakeoffState implements AltitudeState {

    static final Integer INCREMENT_VALUE = 500;
    static final Integer INCREMENT_RATE = 500;
    Integer altitude;
    Phaser phaser;
    Timer timer;

    public TakeoffState(Integer altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;
    }

    @Override
    public void generateAltitude() {

        Timer timer = new Timer();

        TimerTask takeoffTimer = new TimerTask() {
            @Override
            public void run() {
                for (int i =0;i<20;i++) {
                    Integer fluctuation = (int)(Math.random() * 100);
                    altitude += INCREMENT_VALUE + fluctuation;
                    System.out.printf("\nAltitude: %d", altitude);
                }
                timer.cancel();
                phaser.arriveAndAwaitAdvance();
            }
        };

        timer.scheduleAtFixedRate(takeoffTimer, 0L, INCREMENT_RATE);

    }


}
