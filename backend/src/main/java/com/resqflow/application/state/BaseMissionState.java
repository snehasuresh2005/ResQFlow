package com.resqflow.application.state;

import com.resqflow.common.exception.InvalidMissionStateException;
import com.resqflow.domain.mission.Mission;
import com.resqflow.domain.routing.Route;

public abstract class BaseMissionState implements MissionState {

    @Override
    public void dispatch(Mission mission) {
        throw new InvalidMissionStateException("Cannot dispatch mission in status: " + mission.getStatus());
    }

    @Override
    public void transit(Mission mission) {
        throw new InvalidMissionStateException("Cannot start transit for mission in status: " + mission.getStatus());
    }

    @Override
    public void block(Mission mission) {
        throw new InvalidMissionStateException("Cannot block mission in status: " + mission.getStatus());
    }

    @Override
    public void reroute(Mission mission, Route newRoute) {
        throw new InvalidMissionStateException("Cannot reroute mission in status: " + mission.getStatus());
    }

    @Override
    public void deliver(Mission mission) {
        throw new InvalidMissionStateException("Cannot deliver mission in status: " + mission.getStatus());
    }

    @Override
    public void cancel(Mission mission) {
        throw new InvalidMissionStateException("Cannot cancel mission in status: " + mission.getStatus());
    }

    @Override
    public void fail(Mission mission) {
        throw new InvalidMissionStateException("Cannot fail mission in status: " + mission.getStatus());
    }
}
