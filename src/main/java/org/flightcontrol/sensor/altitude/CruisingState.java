package org.flightcontrol.sensor.altitude;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_KEY;
import static org.flightcontrol.actuator.wingflap.WingFlap.WING_FLAP_EXCHANGE_NAME;
import static org.flightcontrol.flight.Flight.TICK_RATE;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_KEY;
import static org.flightcontrol.sensor.altitude.Altitude.ALTITUDE_EXCHANGE_NAME;


public class CruisingState implements AltitudeState {

    Altitude altitude;

    // Callback to be used by Rabbit MQ receive


    public CruisingState(Altitude altitude) {
        this.altitude = altitude;

    }

    @Override
    public void generateAltitude() {
        // NO-OP: Wing flap will take over when cruising
    }





}
