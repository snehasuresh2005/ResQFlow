package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import org.springframework.stereotype.Component;

@Component("STATE_DISPATCHED")
public class DispatchedState extends BaseMissionState {

    @Override
    public void transit(Mission mission) {
        mission.setStatus("IN_TRANSIT");
    }

    @Override
    public void cancel(Mission mission) {
        mission.setStatus("CANCELLED");
    }

    @Override
    public void fail(Mission mission) {
        mission.setStatus("FAILED");
    }
}
