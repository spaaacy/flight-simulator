package org.flightcontrol.actuator.wingflap;

import org.flightcontrol.Observer;
import org.flightcontrol.sensor.altitude.Altitude;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.CRUISING_ALTITUDE;

enum Direction { UP, DOWN, NEUTRAL }

public class WingFlap extends TimerTask implements Observer {

    Altitude altitude;
    WingFlapState wingFlapState;
    Phaser phaser;
    Direction direction;
    Timer timer = new Timer();

    static final Integer CRUISING_INCREMENT = 500;


    public WingFlap(Altitude altitude, Phaser phaser) {
        this.altitude = altitude;
        this.phaser = phaser;
    }

    @Override
    public void run() {
        Integer currentAltitude = altitude.getCurrentAltitude();
        if (currentAltitude - CRUISING_ALTITUDE > 250) { // Plane flying too high
            setWingFlapState(new WingFlapUpState(this, altitude));
        } else if (currentAltitude - CRUISING_ALTITUDE < -250) { // Plane flying too low
            setWingFlapState(new WingFlapDownState(this, altitude));
        } else { // Plane at optimal altitude
            setWingFlapState(new WingFlapNeutralState(this, altitude));
        }
    }

    @Override
    public void update() {
        switch (phaser.getPhase()) {
            case 1 -> direction = Direction.DOWN;
            case 2 -> cruisingFlapControl();
            case 3 -> {
                timer.cancel();
                direction = Direction.UP;
            }
        }
    }

    public void setWingFlapState(WingFlapState wingFlapState) {
        this.wingFlapState = wingFlapState;
        wingFlapState.controlFlaps();
    }

    public void cruisingFlapControl() {
        timer.scheduleAtFixedRate(this, 0L, TICK_RATE);
    }

}
