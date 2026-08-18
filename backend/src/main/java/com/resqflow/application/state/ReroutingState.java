package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import org.springframework.stereotype.Component;

@Component("STATE_REROUTING")
public class ReroutingState extends BaseMissionState {

    @Override
    public void transit(Mission mission) {
        mission.setStatus("IN_TRANSIT");
    }

    @Override
    public void fail(Mission mission) {
        mission.setStatus("FAILED");
    }
}
