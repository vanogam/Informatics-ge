package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.submission.RejudgeAction;

import java.util.List;

/**
 * Which submissions to re-judge, and from where in the judging chain.
 *
 * <p>Both fields are nullable and unvalidated at the binding layer on purpose: a binding failure
 * would answer before the permission check has run, so a caller with no rights would learn that
 * the endpoint exists from the shape of the error.
 */
public record RejudgeRequest(RejudgeAction action, List<Long> submissionIds) {
}