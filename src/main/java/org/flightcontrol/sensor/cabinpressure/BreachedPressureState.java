package org.flightcontrol.sensor.cabinpressure;

import static org.flightcontrol.sensor.cabinpressure.CabinPressure.*;

public class BreachedPressureState implements CabinPressureState{

    CabinPressure cabinPressure;

    public BreachedPressureState(CabinPressure cabinPressure) {
        this.cabinPressure = cabinPressure;
        cabinPressure.setCabinPressureStatus(CabinPressureStatus.BREACHED);
    }

    @Override
    public void generatePsi() {
        Float fluctuation = (float) (Math.random() * MAX_FLUCTUATION * 2) - MAX_FLUCTUATION;
        Float newCabinPressure = BREACHED_CABIN_PRESSURE + fluctuation;
        cabinPressure.setCurrentCabinPressure(newCabinPressure);
    }
}
