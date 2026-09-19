package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.submission.RejudgeOutcome;

/**
 * What happened to one submission in a re-judge request.
 *
 * <p>A refusal is a row rather than an exception, so that re-judging many submissions at once
 * reports per-submission reasons instead of failing the whole batch on the first bad one.
 *
 * @param code null when accepted, otherwise why it was refused. Drawn from the same vocabulary as
 *             {@link ge.freeuni.informatics.common.exception.InformaticsServerException#getCode()},
 *             so the client translates it exactly like the message of a failed request.
 */
public record RejudgeResultDTO(long submissionId, RejudgeOutcome outcome, String code) {

    public static RejudgeResultDTO accepted(long submissionId) {
        return new RejudgeResultDTO(submissionId, RejudgeOutcome.ACCEPTED, null);
    }

    public static RejudgeResultDTO refused(long submissionId, String code) {
        return new RejudgeResultDTO(submissionId, RejudgeOutcome.REFUSED, code);
    }
}