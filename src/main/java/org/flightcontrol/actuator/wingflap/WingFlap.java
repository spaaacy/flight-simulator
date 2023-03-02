package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.Observer;
import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import static org.flightcontrol.flight.Flight.TICK_RATE;

enum Direction {UP, DOWN, NEUTRAL}

public class WingFlap extends TimerTask implements Observer {

    Altitude altitude;
    Phaser phaser;
    WingFlapState wingFlapState;
    Direction direction;
    Timer timer = new Timer();


    public static final Integer CRUISING_ALTITUDE = 11000;
    static final Integer MAX_FLUCTUATION_UP_DOWN = 5;
    static final Integer MAX_FLUCTUATION_NEUTRAL = 500;
    static final Integer INCREMENT_VALUE_UP_DOWN = 15;
    static final Integer ACCEPTED_RANGE = 500;
    // Plane attempts to fly 10500-11500

    public WingFlap(Altitude altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        wingFlapState.controlFlaps();
    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> direction = Direction.DOWN;
            case 2 -> {
                setWingFlapState(new WingFlapNeutralState(this, altitude));
                timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
            }
            case 3 -> {
                timer.cancel();
                direction = Direction.UP;
            }
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        System.out.println("WingFlap: " + direction.toString());
    }

    public void setWingFlapState(WingFlapState newWingFlapState) {
        if (newWingFlapState != null) {
            newWingFlapState.stopExecution();
        }

        this.wingFlapState = newWingFlapState;
        System.out.println("WingFlap: " + newWingFlapState.toString());
    }

}
