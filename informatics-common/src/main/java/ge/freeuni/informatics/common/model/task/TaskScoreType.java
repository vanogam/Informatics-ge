package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public enum TaskScoreType {
    /**
     *  Sum of all test cases.
     *  When selected, task score parameters should be
     * one float, representing value of each test case.
     */
    SUM((testResults, s) -> {
        if (s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
            String[] parts = s.substring(1, s.length() - 1).split(",");
            if (parts.length != testResults.size()) {
                LoggerFactory.getLogger(TaskScoreType.class).error("Invalid score format for SUM type: expected {} parts, got {}", testResults.size(), parts.length);
                throw new IllegalStateException("invalidFormat");
            }
            float sum = 0;
            for (int i = 0; i < parts.length; i++) {
                sum += Float.parseFloat(parts[i].trim()) * testResults.get(i).getScore();
            }
            return sum;
        } else {
            float sum = 0;
            float multiplier = Float.parseFloat(s);
            for (SubmissionTestResult testResult : testResults) {
                sum += testResult.getScore() * multiplier;
            }
            return sum;
        }
    }),
    /**
     *  Minimum of test case group.
     *  When selected, task score parameters should be
     * formatted like this: "[[m1, t1(, p1...)], [m2, t2(, p2...)], ...]"
     * where m is float - multiplier, t is integer - number of test cases and p1-s are parent test case groups.
     * Sum of all t's must be equal to the quantity of all test cases.
     */
    GROUP_MIN((testResults, s) -> {
        float sum = 0;
        if (s.charAt(0) == '[' && s.charAt(s.length() - 1) == ']') {
            s = s.substring(1, s.length() - 1);
            s = s.replaceAll("\\s+", "");
            String[] groups = s.split("\\],\\[");
            List<Float> groupScores = new ArrayList<>();
            int testIndex = 0;
            for (int i = 0; i < groups.length; i++) {
                List<String> group = Arrays.asList(groups[i].split(","));
                if (group.size() < 2) {
                    LoggerFactory.getLogger(TaskScoreType.class).error("Invalid score format for GROUP_MIN type: each group must contain at least a multiplier and a test case count");
                    throw new IllegalStateException("invalidFormat");
                }
                float multiplier = Float.parseFloat(group.get(0));
                int testcaseCount = Integer.parseInt(group.get(1));
                float minScore = 1.0f;
                for (int j = 0; j < testcaseCount; j++, testIndex++) {
                    minScore = Math.min(minScore, testResults.get(testIndex).getScore());
                }
                for (int j = 2; j < group.size(); j++) {
                    int parentIndex = Integer.parseInt(group.get(j)) - 1;
                    if (parentIndex < 0 || parentIndex >= testResults.size()) {
                        LoggerFactory.getLogger(TaskScoreType.class).error("Invalid parent index {} for group {}", parentIndex, i);
                        throw new IllegalStateException("invalidParentIndex");
                    }
                    minScore = Math.min(minScore, groupScores.get(parentIndex));
                }
                groupScores.add(minScore);
                groupScores.add(minScore * multiplier);
                sum += minScore * multiplier;
            }
        } else {
            LoggerFactory.getLogger(TaskScoreType.class).error("Invalid score format for GROUP_MIN type: expected format [[m1, t1(, p1...)], [m2, t2(, p2...)], ...]");
            throw new IllegalStateException("invalidFormat");
        }
        return sum;
    });

    private BiFunction<List<SubmissionTestResult>, String, Float> evaluator;

    TaskScoreType(BiFunction<List<SubmissionTestResult>, String, Float> evaluator) {
        this.evaluator = evaluator;
    }

    public Float evaluate(List<SubmissionTestResult> testResults, String scoreParameter) {
        return evaluator.apply(testResults, scoreParameter);
    }
}
