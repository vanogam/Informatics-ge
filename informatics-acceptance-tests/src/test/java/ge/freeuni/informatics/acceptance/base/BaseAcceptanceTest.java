package ge.freeuni.informatics.acceptance.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.acceptance.config.AcceptanceTestConfig;
import ge.freeuni.informatics.acceptance.mock.MockJudgeIntegration;
import ge.freeuni.informatics.acceptance.util.TestDataFactory;
import ge.freeuni.informatics.system.InformaticsApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest(
        classes = InformaticsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(AcceptanceTestConfig.class)
@ActiveProfiles("acceptance")
@EmbeddedKafka(
        partitions = 1,
        topics = {"submission-topic", "submission-callback"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "port=0"
        }
)
public abstract class BaseAcceptanceTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestDataFactory testDataFactory;

    @Autowired
    protected ge.freeuni.informatics.acceptance.mock.MockKafkaWorker mockKafkaWorker;

    @Autowired
    protected ObjectMapper objectMapper;

    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();

    @BeforeEach
    void baseSetUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        userSessions.clear();
//        mockJudgeIntegration.clearPredefinedResults();
    }

    /**
     * Creates a request specification for anonymous (unauthenticated) requests.
     * Use this for testing public endpoints.
     */
    protected RequestSpecification givenAnonymous() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().uri();
    }

    /**
     * Creates a request specification for authenticated requests.
     * Automatically handles login, session management, and CSRF tokens.
     * Sessions are cached, so multiple calls with the same user reuse the session.
     * 
     * @param username the username to authenticate as
     * @param password the user's password
     * @return authenticated request specification with cookies and CSRF token
     */
    protected RequestSpecification givenUser(String username, String password) {
        UserSession session = userSessions.computeIfAbsent(
                username,
                u -> login(username, password)
        );
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookies(session.cookies())
                .header("X-XSRF-TOKEN", session.csrfToken())
                .log().uri();
    }
    
    /**
     * Creates a request specification for an already authenticated user.
     * If the user hasn't logged in yet, throws an exception.
     * Use this when you want to ensure a user is already authenticated.
     */
    protected RequestSpecification givenAuthenticatedUser(String username) {
        UserSession session = userSessions.get(username);
        if (session == null) {
            throw new IllegalStateException("User " + username + " is not authenticated. Call givenUser() first or login manually.");
        }
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookies(session.cookies())
                .header("X-XSRF-TOKEN", session.csrfToken())
                .log().uri();
    }

    protected UserSession login(String username, String password) {
        var csrfResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .get("/csrf");

        String csrfToken = csrfResponse.cookie("XSRF-TOKEN");
        Map<String, String> cookies = new ConcurrentHashMap<>(csrfResponse.cookies());

        var loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .header("X-XSRF-TOKEN", csrfToken)
                .body(Map.of("username", username, "password", password))
                .post("/login");

        if (loginResponse.statusCode() != 200) {
            throw new RuntimeException("Login failed for user: " + username +
                    ", status: " + loginResponse.statusCode() +
                    ", body: " + loginResponse.body().asString());
        }

        cookies.putAll(loginResponse.cookies());
        return new UserSession(username, cookies, csrfToken);
    }

    protected void logout(String username) {
        UserSession session = userSessions.remove(username);
        if (session != null) {
            RestAssured.given()
                    .cookies(session.cookies())
                    .header("X-XSRF-TOKEN", session.csrfToken())
                    .post("/logout");
        }
    }

    protected void clearSession(String username) {
        userSessions.remove(username);
    }

    protected void clearAllSessions() {
        userSessions.clear();
    }

    public record UserSession(String username, Map<String, String> cookies, String csrfToken) {}
}

