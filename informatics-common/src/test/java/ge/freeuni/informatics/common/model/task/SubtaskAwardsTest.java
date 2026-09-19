package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The per-subtask breakdown is the primary output of every scoring rule and the total is its sum,
 * so that a SUBTASK_MAX contest accumulating the breakdown can never show a total that disagrees
 * with the figures beside it.
 */
public class SubtaskAwardsTest {

    private List<SubmissionTestResult> results(float... scores) {
        List<SubmissionTestResult> all = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) {
            SubmissionTestResult r = new SubmissionTestResult();
            r.setTestKey(String.format("%02d", i));
            r.setScore(scores[i]);
            all.add(r);
        }
        return all;
    }

    @Test
    void sumAwardsOneEntryPerTest() {
        List<SubmissionTestResult> tests = results(1f, 0f, 0.5f);
        assertEquals(List.of(10f, 0f, 5f), TaskScoreType.SUM.evaluateSubtasks(tests, "10"));
    }

    @Test
    void sumAwardsPerTestWeights() {
        List<SubmissionTestResult> tests = results(1f, 1f, 0f);
        assertEquals(List.of(5f, 20f, 0f), TaskScoreType.SUM.evaluateSubtasks(tests, "[5,20,30]"));
    }

    /** ballmachine's shape: 5 subtasks worth 0, 5, 10, 25 and 60, only the third one solved. */
    @Test
    void groupMinAwardsOneEntryPerGroup() {
        String parameter = "[0,1],[5,2],[10,2],[25,2],[60,2]";
        List<SubmissionTestResult> tests = results(1f, 1f, 0f, 1f, 1f, 0f, 1f, 0f, 0f);
        assertEquals(List.of(0f, 0f, 10f, 0f, 0f),
                TaskScoreType.GROUP_MIN.evaluateSubtasks(tests, parameter));
    }

    /**
     * A group is only worth its points once every group it depends on is solved too. Parent
     * references are 1-based indices into the running list of group scores, so the first group's
     * min score is parent 1.
     */
    @Test
    void groupMinKeepsParentGroupClamping() {
        String parameter = "[10,1],[20,1,1]";
        assertEquals(List.of(0f, 0f),
                TaskScoreType.GROUP_MIN.evaluateSubtasks(results(0f, 1f), parameter),
                "the second group cannot pay out while its parent is unsolved");
        assertEquals(List.of(10f, 20f),
                TaskScoreType.GROUP_MIN.evaluateSubtasks(results(1f, 1f), parameter));
    }

    @Test
    void totalIsTheSumOfTheAwards() {
        String parameter = "[0,1],[5,2],[10,2],[25,2],[60,2]";
        List<SubmissionTestResult> tests = results(1f, 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1f);
        float sum = 0;
        for (Float award : TaskScoreType.GROUP_MIN.evaluateSubtasks(tests, parameter)) {
            sum += award;
        }
        assertEquals(sum, TaskScoreType.GROUP_MIN.evaluate(tests, parameter));
    }

    /**
     * A parameter describing more tests than the submission has results for means the task's
     * testcases changed after it was judged; it must name the mismatch, not index past the end.
     */
    @Test
    void groupMinRejectsAParameterLongerThanTheResults() {
        assertThrows(IllegalStateException.class,
                () -> TaskScoreType.GROUP_MIN.evaluateSubtasks(results(1f, 1f), "[10,1],[20,5]"));
    }
}
