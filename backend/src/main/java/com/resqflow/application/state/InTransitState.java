package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import org.springframework.stereotype.Component;

@Component("STATE_IN_TRANSIT")
public class InTransitState extends BaseMissionState {

    @Override
    public void block(Mission mission) {
        mission.setStatus("BLOCKED");
    }

    @Override
    public void deliver(Mission mission) {
        mission.setStatus("DELIVERED");
    }

    @Override
    public void fail(Mission mission) {
        mission.setStatus("FAILED");
    }
}
