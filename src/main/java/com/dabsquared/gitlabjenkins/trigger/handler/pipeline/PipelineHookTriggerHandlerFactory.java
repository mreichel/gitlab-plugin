package com.dabsquared.gitlabjenkins.trigger.handler.pipeline;

import com.dabsquared.gitlabjenkins.gitlab.hook.model.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Milena Zachow
 */
public final class PipelineHookTriggerHandlerFactory {

    private PipelineHookTriggerHandlerFactory() {
    }

    public static PipelineHookTriggerHandler newPipelineHookTriggerHandler(boolean triggerOnPipelineEvent) {
        if (triggerOnPipelineEvent) {
            return new PipelineHookTriggerHandlerImpl(retrieve(triggerOnPipelineEvent));
        } else {
            return new NopPipelineHookTriggerHandler();
        }
    }


    private static List<State> retrieve(boolean triggerOnPipelineEvent) {
        List<State> result = new ArrayList<>();
        if (triggerOnPipelineEvent) {
            result.add(State.success);
        }
        return result;
    }
}
