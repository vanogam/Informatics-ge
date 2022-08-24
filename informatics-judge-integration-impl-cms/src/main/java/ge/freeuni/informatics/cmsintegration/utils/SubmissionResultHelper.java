package ge.freeuni.informatics.cmsintegration.utils;

import ge.freeuni.informatics.cmsintegration.model.TestResult;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResultList;

import java.util.ArrayList;

public class SubmissionResultHelper {

    public static SubmissionTestResultList toSubmissionTestResultList(TestResult[] results) {
        SubmissionTestResultList submissionTestResultList = new SubmissionTestResultList();
        submissionTestResultList.setSubmissionTestResults(new ArrayList<>());
        for (TestResult result : results) {
            submissionTestResultList.getSubmissionTestResults().add(toSubmissionTestResult(result));
        }
        return submissionTestResultList;
    }

    private static SubmissionTestResult toSubmissionTestResult(TestResult result) {
        SubmissionTestResult submissionTestResult = new SubmissionTestResult();
        submissionTestResult.setIdx(result.getIdx());
        submissionTestResult.setMemory(result.getMemory());
        submissionTestResult.setTime(result.getTime());
        submissionTestResult.setOutcome(result.getOutcome());
        submissionTestResult.setText(result.getText()[0]);
        return submissionTestResult;
    }
}
