package com.resqflow.application.state;

import com.resqflow.common.exception.InvalidMissionStateException;
import com.resqflow.domain.mission.Mission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MissionStateTransitionTest {

    private Mission mission;
    private CreatedState createdState;
    private DispatchedState dispatchedState;
    private InTransitState inTransitState;

    @BeforeEach
    public void setUp() {
        mission = Mission.builder().status("CREATED").build();
        createdState = new CreatedState();
        dispatchedState = new DispatchedState();
        inTransitState = new InTransitState();
    }

    @Test
    public void testValidTransitions() {
        // CREATED -> DISPATCHED
        createdState.dispatch(mission);
        assertEquals("DISPATCHED", mission.getStatus());

        // DISPATCHED -> IN_TRANSIT
        dispatchedState.transit(mission);
        assertEquals("IN_TRANSIT", mission.getStatus());

        // IN_TRANSIT -> DELIVERED
        inTransitState.deliver(mission);
        assertEquals("DELIVERED", mission.getStatus());
    }

    @Test
    public void testInvalidTransitions() {
        // Try starting transit directly on a newly CREATED mission (must fail)
        assertThrows(InvalidMissionStateException.class, () -> {
            createdState.transit(mission);
        });

        // Try delivering a CREATED mission directly (must fail)
        assertThrows(InvalidMissionStateException.class, () -> {
            createdState.deliver(mission);
        });
    }
}
