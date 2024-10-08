package org.flightcontrol.sensor.cabinpressure;

import static org.flightcontrol.sensor.cabinpressure.CabinPressure.MAX_FLUCTUATION;
import static org.flightcontrol.sensor.cabinpressure.CabinPressure.NORMAL_CABIN_PRESSURE;

public class CabinPressureNormalState implements CabinPressureState{

    CabinPressure cabinPressure;

    public CabinPressureNormalState(CabinPressure cabinPressure) {
        this.cabinPressure = cabinPressure;
        cabinPressure.setCabinPressureStatus(CabinPressureStatus.NORMAL);
    }

    @Override
    public void generatePsi() {
        Float fluctuation = (float) (Math.random() * MAX_FLUCTUATION * 2) - MAX_FLUCTUATION;
        Float newCabinPressure = NORMAL_CABIN_PRESSURE + fluctuation;
        cabinPressure.setCurrentCabinPressure(newCabinPressure);
    }
}
