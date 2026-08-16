package ge.freeuni.informatics.common.model.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ordering for testcase keys.
 *
 * <p>Test results are stored as a bag - an {@code @ElementCollection} with no order column - so
 * the database returns them in no particular order. {@link TaskScoreType#GROUP_MIN} consumes them
 * positionally, assigning the first n to the first subtask and so on, which only produces the
 * right answer if the caller sorts first. A submission that fails some subtasks and passes others
 * is scored against the wrong groups otherwise; one that passes everything scores correctly by
 * accident, because every value is identical.
 *
 * <p>Digits compare numerically so "1-2" sorts before "1-10", matching how the task editor lists
 * tests and therefore how subtask sizes were counted when they were configured.
 */
public final class TestKeys {

    private static final Pattern CHUNK = Pattern.compile("(\\d+)|(\\D+)");

    public static final Comparator<String> NATURAL_ORDER = TestKeys::compare;

    private TestKeys() {
    }

    public static int compare(String left, String right) {
        List<String> l = chunks(left);
        List<String> r = chunks(right);
        for (int i = 0; i < Math.min(l.size(), r.size()); i++) {
            String a = l.get(i);
            String b = r.get(i);
            boolean numeric = Character.isDigit(a.charAt(0)) && Character.isDigit(b.charAt(0));
            int diff = numeric
                    ? new java.math.BigInteger(a).compareTo(new java.math.BigInteger(b))
                    : a.compareTo(b);
            if (diff != 0) {
                return diff;
            }
        }
        return Integer.compare(l.size(), r.size());
    }

    private static List<String> chunks(String key) {
        List<String> parts = new ArrayList<>();
        Matcher m = CHUNK.matcher(key == null ? "" : key);
        while (m.find()) {
            parts.add(m.group());
        }
        return parts;
    }
}
