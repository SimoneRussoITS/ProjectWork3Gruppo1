package org.acme.rest.model;

import org.acme.persistence.model.TestState;

import javax.print.attribute.TextSyntax;
import java.lang.annotation.Target;

public class TestStateRequest {
    private TestState testState;

    public TestState getTestState() {
        return testState;
    }

    public void setTestState(TestState testState) {
        this.testState = testState;
    }
}
