/**
 * Colours for submission test rows and the subtask headers that wrap them.
 *
 * A subtask takes the colour of its worst test, which is why severity is ordered rather than
 * derived from the subtask's own score: a subtask containing a failed test should look failed
 * even when the rest passed.
 */

export const RESULT_COLORS = {
    full: '#CFE8D3',
    partial: '#F2E8CF',
    failed: '#E8CFD4',
};

/** Higher is worse, so the worst test can be picked with a max. */
const SEVERITY = {full: 0, partial: 1, failed: 2};

/**
 * Severity of a single test. Anything neither correct nor partially scored counts as failed,
 * which covers wrong answers, time and memory limits, runtime and system errors alike.
 */
export function testSeverity(testcase) {
    if (testcase?.testStatus === 'CORRECT') {
        return 'full';
    }
    if (testcase?.testStatus === 'PARTIAL' || (testcase?.score > 0 && testcase?.score < 1)) {
        return 'partial';
    }
    return 'failed';
}

export function testColor(testcase) {
    return RESULT_COLORS[testSeverity(testcase)];
}

/**
 * Colour of a group of tests: the worst one present.
 */
export function worstColor(testcases) {
    const worst = (testcases || []).reduce(
        (acc, t) => Math.max(acc, SEVERITY[testSeverity(t)]), SEVERITY.full);
    return RESULT_COLORS[Object.keys(SEVERITY).find(k => SEVERITY[k] === worst)];
}