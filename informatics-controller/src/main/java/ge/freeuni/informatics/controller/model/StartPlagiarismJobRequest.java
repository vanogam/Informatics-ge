package ge.freeuni.informatics.controller.model;

import java.util.List;

/**
 * Exactly one of {@code contestId}/{@code taskId} must be set - validated downstream, not here,
 * so a malformed request is turned away by the permission check first.
 */
public record StartPlagiarismJobRequest(Long contestId, Long taskId, List<String> usernames) {
}
