package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.routing.Route;
import org.springframework.stereotype.Component;

@Component("STATE_BLOCKED")
public class BlockedState extends BaseMissionState {

    @Override
    public void reroute(Mission mission, Route newRoute) {
        mission.setRoute(newRoute);
        mission.setStatus("REROUTING");
    }

    @Override
    public void fail(Mission mission) {
        mission.setStatus("FAILED");
    }
}
