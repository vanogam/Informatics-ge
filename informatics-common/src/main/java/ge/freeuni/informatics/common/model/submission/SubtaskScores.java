package ge.freeuni.informatics.common.model.submission;

import ge.freeuni.informatics.common.model.task.TaskScoreType;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encoding and merging of a submission's per-subtask awards.
 *
 * <p>The vector produced by {@link TaskScoreType#evaluateSubtasks} is stored as text on the
 * submission and on the contestant's task result, so a contest scored by
 * {@link ge.freeuni.informatics.common.model.contest.ScoringType#SUBTASK_MAX} can accumulate a
 * running maximum without re-reading every earlier submission's test results.
 */
public final class SubtaskScores {

    /**
     * Beyond this many scoring units the vector is not stored at all, and the task falls back to
     * whole-submission scoring. SUM tasks have one unit per test, so a task with thousands of
     * tests would otherwise write a vector of that length onto every submission row and every
     * standings row - for a scoring rule that is only meaningful when units are subtasks.
     */
    public static final int MAX_TRACKED = 200;

    private SubtaskScores() {
    }

    /**
     * @return the encoded vector, or null when there is nothing worth storing - which the
     * standings read as "this submission has no breakdown" and fall back on
     */
    public static String format(List<Float> awards) {
        if (awards == null || awards.isEmpty() || awards.size() > MAX_TRACKED) {
            return null;
        }
        StringBuilder encoded = new StringBuilder();
        for (Float award : awards) {
            if (encoded.length() > 0) {
                encoded.append(',');
            }
            encoded.append(award == null ? 0f : award.floatValue());
        }
        return encoded.toString();
    }

    /**
     * @return the decoded vector, or an empty list when the column is empty or unreadable. A
     * malformed value is treated as absent rather than fatal: it can only cost a contestant the
     * accumulation, whereas throwing would fail the submission that happened to arrive next.
     */
    public static List<Float> parse(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return Collections.emptyList();
        }
        String[] parts = encoded.split(",");
        List<Float> awards = new ArrayList<>(parts.length);
        for (String part : parts) {
            try {
                awards.add(Float.parseFloat(part.trim()));
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(SubtaskScores.class)
                        .warn("Unreadable subtask score vector '{}', treating it as absent", encoded);
                return Collections.emptyList();
            }
        }
        return awards;
    }

    /**
     * Element-wise maximum of two award vectors - the whole point of SUBTASK_MAX: a contestant
     * keeps the best they have ever scored on each subtask, whichever submission it came from.
     *
     * @return null when the two cannot be merged, meaning the caller must fall back. That is
     * either vector being absent, or the two disagreeing on length - which happens when a task's
     * subtasks are re-configured while the contest is running, and leaves no honest way to line
     * the old vector's entries up with the new one's.
     */
    public static List<Float> merge(List<Float> current, List<Float> incoming) {
        if (current == null || incoming == null || current.isEmpty() || incoming.isEmpty()) {
            return null;
        }
        if (current.size() != incoming.size()) {
            return null;
        }
        List<Float> merged = new ArrayList<>(current.size());
        for (int i = 0; i < current.size(); i++) {
            merged.add(Math.max(current.get(i), incoming.get(i)));
        }
        return merged;
    }

    /**
     * Sum of a vector, rounded the way {@link TaskScoreType} rounds a total. Each entry was
     * already rounded as it was awarded; this clears the residue from adding them up.
     */
    public static float total(List<Float> awards) {
        float sum = 0;
        if (awards != null) {
            for (Float award : awards) {
                sum += award == null ? 0f : award;
            }
        }
        return TaskScoreType.roundScore(sum);
    }
}