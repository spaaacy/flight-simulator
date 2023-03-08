package org.flightcontrol.sensor.cabinpressure;

import static org.flightcontrol.sensor.cabinpressure.CabinPressure.BREACHED_CABIN_PRESSURE;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.MAX_FLUCTUATION;

public class CabinPressureBreachedState implements CabinPressureState{

    CabinPressure cabinPressure;

    public CabinPressureBreachedState(CabinPressure cabinPressure) {
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
