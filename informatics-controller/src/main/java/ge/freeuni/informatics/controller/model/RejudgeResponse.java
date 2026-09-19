package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.RejudgeResultDTO;

import java.util.List;

/**
 * One result per requested submission, in the order they were given. A submission that was turned
 * away is a row carrying the reason, not a failed request - so re-judging many at once reports on
 * each of them.
 */
public record RejudgeResponse(List<RejudgeResultDTO> results) {
}