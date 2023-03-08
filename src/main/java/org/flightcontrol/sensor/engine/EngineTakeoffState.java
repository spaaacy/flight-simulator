package org.flightcontrol.sensor.engine;

import static org.flightcontrol.sensor.engine.Engine.*;

public class EngineTakeoffState implements EngineState {

    Engine engine;

    public EngineTakeoffState(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void generateRpm() {
        int largestPossibleNextValue = engine.currentPercentage + MAX_FLUCTUATION + INCREMENT_TAKEOFF_LANDING;
        if (largestPossibleNextValue <= CRUISING_PERCENTAGE) {
            Integer fluctuation = (int) (Math.random() * MAX_FLUCTUATION * 2) - MAX_FLUCTUATION;
            Integer newPercentage = engine.currentPercentage + INCREMENT_TAKEOFF_LANDING + fluctuation;
            engine.setCurrentPercentage(newPercentage);
        } else {
            engine.setCurrentPercentage(CRUISING_PERCENTAGE);
            engine.engineState = new EngineCruisingState(engine);
        }
    }
}
