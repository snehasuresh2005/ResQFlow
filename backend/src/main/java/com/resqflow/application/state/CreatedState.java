package com.resqflow.application.state;

import com.resqflow.domain.mission.Mission;
import org.springframework.stereotype.Component;

@Component("STATE_CREATED")
public class CreatedState extends BaseMissionState {

    @Override
    public void dispatch(Mission mission) {
        mission.setStatus("DISPATCHED");
    }

    @Override
    public void cancel(Mission mission) {
        mission.setStatus("CANCELLED");
    }
}
