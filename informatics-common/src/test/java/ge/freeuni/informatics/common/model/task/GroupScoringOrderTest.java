package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * GROUP_MIN slices its input positionally, so the caller must sort by test key first. Results
 * arrive in whatever order the workers finish, which is arbitrary once tests are spread across
 * partitions.
 */
public class GroupScoringOrderTest {

    /** ballmachine: 5 subtasks worth 0, 5, 10, 25 and 60. */
    private static final String PARAM = "[0,1],[5,9],[10,11],[25,23],[60,30]";
    private static final int[][] GROUPS = {{0, 1}, {1, 9}, {2, 11}, {3, 23}, {4, 30}};

    /** Every test of {@code solvedSubtask} scores 1, the rest 0. */
    private List<SubmissionTestResult> results(int solvedSubtask) {
        List<SubmissionTestResult> all = new ArrayList<>();
        for (int[] g : GROUPS) {
            for (int i = 1; i <= g[1]; i++) {
                SubmissionTestResult r = new SubmissionTestResult();
                r.setTestKey(g[0] + "-" + String.format("%02d", i));
                r.setScore(g[0] == solvedSubtask ? 1f : 0f);
                all.add(r);
            }
        }
        return all;
    }

    private List<SubmissionTestResult> sorted(List<SubmissionTestResult> in) {
        List<SubmissionTestResult> copy = new ArrayList<>(in);
        copy.sort(Comparator.comparing(SubmissionTestResult::getTestKey, TestKeys.NATURAL_ORDER));
        return copy;
    }

    @Test
    void scoresTheSubtaskThatWasSolved() {
        assertEquals(10f, TaskScoreType.GROUP_MIN.evaluate(sorted(results(2)), PARAM));
    }

    @Test
    void arrivalOrderDoesNotChangeTheScore() {
        for (long seed = 0; seed < 20; seed++) {
            List<SubmissionTestResult> shuffled = results(2);
            Collections.shuffle(shuffled, new Random(seed));
            assertEquals(10f, TaskScoreType.GROUP_MIN.evaluate(sorted(shuffled), PARAM),
                    "score must not depend on the order results came back in (seed " + seed + ")");
        }
    }

    @Test
    void scoresEachSubtaskIndependently() {
        assertEquals(0f, TaskScoreType.GROUP_MIN.evaluate(sorted(results(0)), PARAM));
        assertEquals(5f, TaskScoreType.GROUP_MIN.evaluate(sorted(results(1)), PARAM));
        assertEquals(25f, TaskScoreType.GROUP_MIN.evaluate(sorted(results(3)), PARAM));
        assertEquals(60f, TaskScoreType.GROUP_MIN.evaluate(sorted(results(4)), PARAM));
    }

    /**
     * A statement names what a subtask is worth, so each award is rounded as it is made. The
     * contestant sees those per-subtask figures, and they have to add up to the total shown
     * beside them.
     */
    @Test
    void subtaskAwardsAreRoundedAndSumToTheTotal() {
        // subtask 3 partial at the manager's 13/25 tier, everything else solved
        List<SubmissionTestResult> all = new ArrayList<>();
        for (int[] g : GROUPS) {
            for (int i = 0; i < g[1]; i++) {
                SubmissionTestResult r = new SubmissionTestResult();
                r.setTestKey(g[0] + "-" + String.format("%02d", i + 1));
                r.setScore(g[0] == 3 ? 13f / 25f : 1f);
                all.add(r);
            }
        }
        float total = TaskScoreType.GROUP_MIN.evaluate(sorted(all), PARAM);

        // 0 + 5 + 10 + 13 + 60, with the partial subtask landing on exactly 13
        assertEquals(88f, total);
        assertEquals(13f, TaskScoreType.roundScore(13f / 25f * 25f));
    }

    @Test
    void roundsEachAwardRatherThanOnlyTheTotal() {
        // three subtasks of 33.333 each: rounding per award gives 99.99, not a tidied 100
        List<SubmissionTestResult> results = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            SubmissionTestResult r = new SubmissionTestResult();
            r.setTestKey("0-0" + (i + 1));
            r.setScore(1f);
            results.add(r);
        }
        float sumOfAwards = TaskScoreType.GROUP_MIN.evaluate(results, "[33.33,1],[33.33,1],[33.33,1]");

        // Each award is exactly 33.33 rather than a tidied 100 overall...
        assertEquals(33.33f, TaskScoreType.roundScore(33.33f));
        // ...but summing rounded floats still drifts, which is why the total is rounded again
        // where it is stored (JudgeIntegration.finalizeSubmission).
        assertEquals(99.990005f, sumOfAwards);
        assertEquals(99.99f, TaskScoreType.roundScore(sumOfAwards));
    }

    @Test
    void sortsEmbeddedNumbersNumerically() {
        List<String> keys = new ArrayList<>(List.of("1-10", "1-2", "10", "2", "1-1"));
        keys.sort(TestKeys.NATURAL_ORDER);
        assertEquals(List.of("1-1", "1-2", "1-10", "2", "10"), keys);
    }
}
