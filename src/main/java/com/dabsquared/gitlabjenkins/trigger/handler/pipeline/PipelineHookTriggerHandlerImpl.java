package com.dabsquared.gitlabjenkins.trigger.handler.pipeline;

import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.PipelineEventObjectAttributes;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.PipelineHook;
import com.dabsquared.gitlabjenkins.gitlab.hook.model.State;
import com.dabsquared.gitlabjenkins.trigger.exception.NoRevisionToBuildException;
import com.dabsquared.gitlabjenkins.trigger.filter.BranchFilter;
import com.dabsquared.gitlabjenkins.trigger.filter.MergeRequestLabelFilter;
import com.dabsquared.gitlabjenkins.trigger.handler.AbstractWebHookTriggerHandler;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.RevisionParameterAction;

import java.util.List;
import java.util.logging.Logger;

import static com.dabsquared.gitlabjenkins.cause.CauseDataBuilder.causeData;
import static com.dabsquared.gitlabjenkins.trigger.handler.builder.generated.BuildStatusUpdateBuilder.buildStatusUpdate;

/**
 * @author Milena Zachow
 */
class PipelineHookTriggerHandlerImpl extends AbstractWebHookTriggerHandler<PipelineHook> implements PipelineHookTriggerHandler {

    private static final Logger LOGGER = Logger.getLogger(PipelineHookTriggerHandlerImpl.class.getName());

    private final List<State> allowedStates;

    PipelineHookTriggerHandlerImpl(List<State> allowedStates) {
        this.allowedStates = allowedStates;
    }

    @Override
    public void handle(Job<?, ?> job, PipelineHook hook, boolean ciSkip, BranchFilter branchFilter, MergeRequestLabelFilter mergeRequestLabelFilter) {
        PipelineEventObjectAttributes objectAttributes = hook.getObjectAttributes();
        if (allowedStates.contains(objectAttributes.getStatus())) {
            super.handle(job, hook, ciSkip, branchFilter, mergeRequestLabelFilter);
        }
    }

    @Override
    protected boolean isCiSkip(PipelineHook hook) {
        return hook.getObjectAttributes() != null
                && hook.getObjectAttributes().getStatus() != null;
    }

    @Override
    protected String getTargetBranch(PipelineHook hook) {
        return hook.getObjectAttributes().getRef() == null ? null : hook.getObjectAttributes().getRef().replaceFirst("^refs/heads/", "");
    }

    @Override
    protected String getTriggerType() {
        return "pipeline event";
    }

    @Override
    protected CauseData retrieveCauseData(PipelineHook hook) {
        return causeData()
                .withActionType(CauseData.ActionType.PIPELINE)
                .withSourceProjectId(hook.getObjectAttributes().getId())
                .withTargetProjectId(hook.getObjectAttributes().getId())
                .withBranch(getTargetBranch(hook))
                .withSourceBranch(getTargetBranch(hook))
                .withUserName(hook.getUser().getName())
                .withSourceRepoName(hook.getRepository().getName())
                .withSourceNamespace(hook.getProject().getNamespace())
                .withSourceRepoSshUrl(hook.getRepository().getGitSshUrl())
                .withSourceRepoHttpUrl(hook.getRepository().getGitHttpUrl())
                .withMergeRequestTitle("")
                .withTargetBranch(getTargetBranch(hook))
                .withTargetRepoName("")
                .withTargetNamespace("")
                .withTargetRepoSshUrl("")
                .withTargetRepoHttpUrl("")
                .withLastCommit(hook.getObjectAttributes().getSha())
                .withTriggeredByUser(hook.getUser().getName())
                .withRef(hook.getObjectAttributes().getRef())
                .withSha(hook.getObjectAttributes().getSha())
                .withBeforeSha(hook.getObjectAttributes().getBeforeSha())
                .withStatus(hook.getObjectAttributes().getStatus()==null?"":hook.getObjectAttributes().getStatus().toString())
                .withStages(hook.getObjectAttributes().getStages()==null?"":hook.getObjectAttributes().getStages().toString())
                .withCreatedAt(hook.getObjectAttributes().getCreatedAt()==null?"":hook.getObjectAttributes().getCreatedAt().toString())
                .withFinishedAt(hook.getObjectAttributes().getFinishedAt()==null?"":hook.getObjectAttributes().getFinishedAt().toString())
                .withBuildDuration(String.valueOf(hook.getObjectAttributes().getDuration()))
                .build();
    }

    @Override
    protected RevisionParameterAction createRevisionParameter(PipelineHook hook, GitSCM gitSCM) throws NoRevisionToBuildException {
        return new RevisionParameterAction(retrieveRevisionToBuild(hook), retrieveUrIish(hook));
    }

    @Override
    protected BuildStatusUpdate retrieveBuildStatusUpdate(PipelineHook hook) {
        return buildStatusUpdate()
            .withProjectId(hook.getObjectAttributes().getId())
            .withSha(hook.getObjectAttributes().getSha())
            .withRef(hook.getObjectAttributes().getRef())
            .build();
    }

    private String retrieveRevisionToBuild(PipelineHook hook) throws NoRevisionToBuildException {
        if (hook.getObjectAttributes() != null
                && hook.getObjectAttributes().getSha() != null) {

            return hook.getObjectAttributes().getSha();
        } else {
            throw new NoRevisionToBuildException();
        }
    }


}
