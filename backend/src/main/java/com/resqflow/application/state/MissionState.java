package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.routing.Route;

public interface MissionState {
    void dispatch(Mission mission);
    void transit(Mission mission);
    void block(Mission mission);
    void reroute(Mission mission, Route newRoute);
    void deliver(Mission mission);
    void cancel(Mission mission);
    void fail(Mission mission);
}
