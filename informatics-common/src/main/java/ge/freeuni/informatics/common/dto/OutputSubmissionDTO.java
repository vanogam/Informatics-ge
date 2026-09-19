package ge.freeuni.informatics.common.dto;

import java.util.List;

/**
 * What became of an output upload.
 *
 * <p>An upload is not all-or-nothing: a zip typically covers only the tests the contestant has
 * solved so far, and it may also carry files that match no test at all. Both are reported rather
 * than refused, so the contestant can see what was accepted - the tests left uncovered simply
 * score zero.
 *
 * @param submissionId  the submission that was queued
 * @param matchedTests  how many of the task's tests the upload provided an output for
 * @param totalTests    how many tests the task has
 * @param unmatchedFiles names in the upload that could not be mapped to a test
 */
public record OutputSubmissionDTO(
        Long submissionId,
        int matchedTests,
        int totalTests,
        List<String> unmatchedFiles
) {}
