package com.resqflow.application.state;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MissionStateFactory {

    private final Map<String, MissionState> states;

    public MissionStateFactory(Map<String, MissionState> states) {
        this.states = states;
    }

    public MissionState getState(String status) {
        String key = "STATE_" + status.toUpperCase();
        MissionState state = states.get(key);
        if (state == null) {
            throw new IllegalArgumentException("Unknown mission status: " + status);
        }
        return state;
    }
}
