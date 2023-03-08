package org.flightcontrol.sensor.engine;

import static org.flightcontrol.sensor.engine.Engine.*;

public class EngineLandingState implements EngineState {

    Engine engine;

    public EngineLandingState(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void generateRpm() {
        int smalledNextPossibleValue = engine.currentPercentage - INCREMENT_TAKEOFF_LANDING - MAX_FLUCTUATION;

        if (smalledNextPossibleValue >= TAKEOFF_LANDING_PERCENTAGE ||
                engine.isAltitudeZero) {
            if (smalledNextPossibleValue >= 0) {
                int fluctuation = (int) (Math.random() * MAX_FLUCTUATION * 2) - MAX_FLUCTUATION;
                int newPercentage = engine.currentPercentage - INCREMENT_TAKEOFF_LANDING + fluctuation;
                engine.setCurrentPercentage(newPercentage);
            } else {
                engine.setCurrentPercentage(0);
                engine.sendLandedFlagToFlight();
            }
        }
    }
}
