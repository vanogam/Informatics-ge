package ge.freeuni.informatics.server.plagiarism;

import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.user.User;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link PlagiarismManager#groupComparableSubmissionsByLanguage} - the pure selection and
 * grouping logic a plagiarism run is built from - without touching the database or JPlag.
 */
class PlagiarismManagerTest {

    private static Submission submission(long id, long userId, String language, Float score, long submissionTimeMillis) {
        User user = new User();
        user.setId(userId);

        Submission submission = new Submission();
        submission.setId(id);
        submission.setUser(user);
        submission.setLanguage(language);
        submission.setScore(score);
        submission.setSubmissionTime(new Date(submissionTimeMillis));
        return submission;
    }

    @Test
    void picksHighestScoringSubmissionPerUser() {
        // Query order is already best-then-latest per user; the lower-scoring row for user 1
        // comes second and must lose to the first.
        List<Submission> candidates = List.of(
                submission(1, 1, "CPP", 90f, 1000),
                submission(2, 1, "CPP", 50f, 2000),
                submission(3, 2, "CPP", 70f, 1000)
        );

        Map<String, List<Submission>> grouped = PlagiarismManager.groupComparableSubmissionsByLanguage(candidates);

        List<Submission> cpp = grouped.get("CPP");
        assertEquals(2, cpp.size());
        assertTrue(cpp.stream().anyMatch(s -> s.getId() == 1));
        assertTrue(cpp.stream().noneMatch(s -> s.getId() == 2));
    }

    @Test
    void tiebreaksEqualScoresByTakingTheFirstRow() {
        // The query breaks score ties by latest submissionTime first, so among equally-scored
        // rows for one user, whichever comes first in the list is already the latest and wins.
        List<Submission> candidates = List.of(
                submission(1, 1, "CPP", 50f, 2000),
                submission(2, 1, "CPP", 50f, 1000),
                submission(3, 2, "CPP", 50f, 1000)
        );

        Map<String, List<Submission>> grouped = PlagiarismManager.groupComparableSubmissionsByLanguage(candidates);

        List<Submission> cpp = grouped.get("CPP");
        assertTrue(cpp.stream().anyMatch(s -> s.getId() == 1));
        assertTrue(cpp.stream().noneMatch(s -> s.getId() == 2));
    }

    @Test
    void groupsByLanguageSeparately() {
        List<Submission> candidates = List.of(
                submission(1, 1, "CPP", 90f, 1000),
                submission(2, 2, "CPP", 80f, 1000),
                submission(3, 3, "PYTHON", 70f, 1000),
                submission(4, 4, "PYTHON", 60f, 1000)
        );

        Map<String, List<Submission>> grouped = PlagiarismManager.groupComparableSubmissionsByLanguage(candidates);

        assertEquals(2, grouped.get("CPP").size());
        assertEquals(2, grouped.get("PYTHON").size());
    }

    @Test
    void dropsLanguageGroupsWithFewerThanTwoSubmissions() {
        // JPlag needs at least two submissions to compare; a lone submission in a language is not
        // a run worth starting.
        List<Submission> candidates = List.of(
                submission(1, 1, "CPP", 90f, 1000),
                submission(2, 2, "CPP", 80f, 1000),
                submission(3, 3, "PYTHON", 70f, 1000)
        );

        Map<String, List<Submission>> grouped = PlagiarismManager.groupComparableSubmissionsByLanguage(candidates);

        assertTrue(grouped.containsKey("CPP"));
        assertFalse(grouped.containsKey("PYTHON"));
    }
}
