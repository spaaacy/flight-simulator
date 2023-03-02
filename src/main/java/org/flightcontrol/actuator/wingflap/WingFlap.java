package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.Observer;
import org.flightcontrol.sensor.altitude.Altitude;

import java.util.concurrent.Phaser;

enum Direction { UP, DOWN, NEUTRAL }

public class WingFlap implements Runnable, Observer {

    Altitude altitude;
    WingFlapState wingFlapState;
    Phaser phaser;
    Direction direction;

    static final Integer CRUISING_INCREMENT = 500;


    public WingFlap(Altitude altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;
    }

    @Override
    public void run() {

    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> direction = Direction.DOWN;
            case 2 -> setWingFlapState(new WingFlapCruisingState(altitude, this));
            case 3 -> direction = Direction.UP;
        }
    }

    public void setWingFlapState(WingFlapState wingFlapState) {
        if (wingFlapState != null) {
            wingFlapState.stopExecution();
        }

        this.wingFlapState = wingFlapState;
        wingFlapState.adjustDirection();
    }
}
