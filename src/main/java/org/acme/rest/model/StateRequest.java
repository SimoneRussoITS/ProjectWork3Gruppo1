package org.acme.rest.model;

import org.acme.persistence.model.State;

public class StateRequest {
    private State state;

    public State getState(String state) {
        return State.valueOf(state);
    }

    public void setState(State state) {
        this.state = state;
    }
}
