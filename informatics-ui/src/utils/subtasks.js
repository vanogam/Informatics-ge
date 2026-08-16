/**
 * Helpers for presenting testcases in the order and grouping a task is actually scored by.
 *
 * The server returns testcases in whatever order the database hands them back - Task.testcases
 * is a @OneToMany with no @OrderBy - so the client sorts them itself.
 */

/**
 * Compares keys so that embedded numbers sort numerically: "1-2" before "1-10", "2" before "10".
 * A plain string comparison puts "1-10" before "1-2", which is what makes the list look shuffled.
 */
export function compareTestKeys(a, b) {
    const left = String(a ?? '');
    const right = String(b ?? '');
    const chunk = /(\d+)|(\D+)/g;
    const leftParts = left.match(chunk) || [];
    const rightParts = right.match(chunk) || [];

    for (let i = 0; i < Math.min(leftParts.length, rightParts.length); i++) {
        const l = leftParts[i];
        const r = rightParts[i];
        const bothNumeric = /^\d/.test(l) && /^\d/.test(r);
        if (bothNumeric) {
            const diff = parseInt(l, 10) - parseInt(r, 10);
            if (diff !== 0) return diff;
        } else if (l !== r) {
            return l < r ? -1 : 1;
        }
    }
    return leftParts.length - rightParts.length;
}

/**
 * @param keyOf reads the key from an item - testcases carry `key`, submission results `testKey`
 */
export function sortTestcases(testcases, keyOf = (t) => t.key) {
    return [...(testcases || [])].sort((a, b) => compareTestKeys(keyOf(a), keyOf(b)));
}

/**
 * Parses a GROUP_MIN taskScoreParameter into its groups.
 *
 * Accepts both "[0,1],[5,9]" and "[[0,1],[5,9]]" - the server's own two readers disagree on
 * whether the outer brackets belong, so stored values can be in either form.
 *
 * @returns {Array<{score: number, count: number}>} or [] if the value is not parseable
 */
export function parseScoreGroups(taskScoreParameter) {
    if (!taskScoreParameter) return [];
    const groups = [];
    const groupPattern = /\[([^[\]]+)]/g;
    let match;
    while ((match = groupPattern.exec(taskScoreParameter)) !== null) {
        const parts = match[1].split(',').map(p => p.trim());
        const score = Number.parseFloat(parts[0]);
        const count = Number.parseInt(parts[1], 10);
        if (!Number.isFinite(score) || !Number.isInteger(count) || count < 0) {
            return [];
        }
        groups.push({score, count});
    }
    return groups;
}

/**
 * Splits sorted testcases into subtasks described by the score parameter.
 *
 * Grouping is positional, exactly as TaskScoreType.GROUP_MIN consumes results, so it is only
 * applied when the group sizes account for every testcase. Otherwise the caller gets a single
 * ungrouped block rather than a grouping that would misrepresent the score.
 *
 * @returns {{grouped: boolean, groups: Array<{score: number, testcases: Array, from: number}>}}
 */
export function groupTestcases(testcases, taskScoreType, taskScoreParameter, keyOf = (t) => t.key) {
    const sorted = sortTestcases(testcases, keyOf);
    const flat = {grouped: false, groups: [{score: null, testcases: sorted, from: 0}]};

    if (taskScoreType !== 'GROUP_MIN') return flat;

    const groups = parseScoreGroups(taskScoreParameter);
    if (groups.length === 0) return flat;

    const total = groups.reduce((sum, g) => sum + g.count, 0);
    if (total !== sorted.length) return flat;

    let cursor = 0;
    return {
        grouped: true,
        groups: groups.map(g => {
            const slice = sorted.slice(cursor, cursor + g.count);
            const from = cursor;
            cursor += g.count;
            return {score: g.score, testcases: slice, from};
        }),
    };
}

/**
 * Rounds a score to two decimals for display.
 *
 * Scores are sums of per-test fractions times subtask multipliers, so floating point leaves
 * values like 61.998 where 62 was meant. Newly judged submissions are already rounded server
 * side; this also tidies rows stored before that.
 */
export function roundScore(score) {
    if (typeof score !== 'number' || Number.isNaN(score)) {
        return score;
    }
    return Math.round(score * 100) / 100;
}
