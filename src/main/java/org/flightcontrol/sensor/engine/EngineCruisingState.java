package org.flightcontrol.sensor.engine;

import static org.flightcontrol.sensor.engine.Engine.CRUISING_PERCENTAGE;
import static org.flightcontrol.sensor.engine.Engine.MAX_FLUCTUATION;

public class EngineCruisingState implements EngineState {

    Engine engine;

    public EngineCruisingState(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void generateRpm() {
        int fluctuation = (int) (Math.random() * MAX_FLUCTUATION);
        int newPercentage = CRUISING_PERCENTAGE - fluctuation;
        engine.setCurrentPercentage(newPercentage);
    }
}
