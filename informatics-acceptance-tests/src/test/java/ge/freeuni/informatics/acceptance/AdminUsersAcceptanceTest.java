package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.model.user.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Acceptance tests for the admin "users" page: listing/searching, and disabling/re-enabling an
 *  account - including that a disabled account is actually blocked from logging in again, and
 *  that an admin account can never be disabled through this endpoint. */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminUsersAcceptanceTest extends BaseAcceptanceTest {

    private static final String PASSWORD = "password123";

    @AfterEach
    void tearDown() {
        clearAllSessions();
    }

    @Test
    @DisplayName("Admin lists users and can search by username/first/last name")
    void adminCanListAndSearchUsers() {
        testDataFactory.createAdmin("admin1", PASSWORD);
        testDataFactory.createStudent("alice", PASSWORD);
        testDataFactory.createStudent("bob", PASSWORD);

        givenUser("admin1", PASSWORD)
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200)
                .body("totalCount", org.hamcrest.Matchers.greaterThanOrEqualTo(3));

        givenUser("admin1", PASSWORD)
                .queryParam("search", "alice")
                .when()
                .get("/admin/users")
                .then()
                .statusCode(200)
                .body("totalCount", org.hamcrest.Matchers.equalTo(1))
                .body("users[0].username", org.hamcrest.Matchers.equalTo("alice"));
    }

    @Test
    @DisplayName("Listing users is refused to a non-admin")
    void listingUsersRequiresAdmin() {
        testDataFactory.createStudent("plainstudent", PASSWORD);

        givenUser("plainstudent", PASSWORD)
                .when()
                .get("/admin/users")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Disabling a user blocks their next login, and re-enabling lets them back in")
    void disablingBlocksLoginAndEnablingRestoresIt() {
        testDataFactory.createAdmin("admin2", PASSWORD);
        User target = testDataFactory.createStudent("disableme", PASSWORD);

        givenUser("disableme", PASSWORD)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
        clearSession("disableme");
        logout("disableme");

        givenUser("admin2", PASSWORD)
                .when()
                .post("/admin/users/{userId}/disable", target.getId())
                .then()
                .statusCode(200);

        Response rejected = attemptLogin("disableme", PASSWORD);
        assertThat(rejected.statusCode()).isNotEqualTo(200);
        assertThat(rejected.jsonPath().getString("message")).isEqualTo("accountDisabled");

        givenUser("admin2", PASSWORD)
                .when()
                .post("/admin/users/{userId}/enable", target.getId())
                .then()
                .statusCode(200);

        Response allowed = attemptLogin("disableme", PASSWORD);
        assertThat(allowed.statusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("A wrong password on a disabled account still looks like a wrong password")
    void wrongPasswordOnDisabledAccountIsIndistinguishable() {
        testDataFactory.createAdmin("admin3", PASSWORD);
        User target = testDataFactory.createStudent("disabledwrongpass", PASSWORD);

        givenUser("admin3", PASSWORD)
                .when()
                .post("/admin/users/{userId}/disable", target.getId())
                .then()
                .statusCode(200);

        Response wrongPassword = attemptLogin("disabledwrongpass", "notThePassword");
        assertThat(wrongPassword.statusCode()).isNotEqualTo(200);
        assertThat(wrongPassword.jsonPath().getString("message")).isEqualTo("incorrectCredentials");
    }

    @Test
    @DisplayName("Disabling an admin account is refused")
    void cannotDisableAnAdminAccount() {
        testDataFactory.createAdmin("admin4", PASSWORD);
        User otherAdmin = testDataFactory.createAdmin("admin5", PASSWORD);

        givenUser("admin4", PASSWORD)
                .when()
                .post("/admin/users/{userId}/disable", otherAdmin.getId())
                .then()
                .statusCode(400)
                .body("message", org.hamcrest.Matchers.equalTo("cannotDisableAdmin"));

        givenUser("admin5", PASSWORD)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Disabling an unknown user id is rejected")
    void disablingAnUnknownUserIsRejected() {
        testDataFactory.createAdmin("admin6", PASSWORD);

        givenUser("admin6", PASSWORD)
                .when()
                .post("/admin/users/{userId}/disable", 999999L)
                .then()
                .statusCode(404);
    }

    /**
     * Password recovery (verify/update-password, and the request step itself) requires an
     * already-authenticated session in this app - see the same note in PasswordAcceptanceTest.
     * A disabled account can't authenticate at all, so it has no path through recovery either:
     * every step is turned away at the authentication gate before the disabled flag is ever
     * consulted. This locks in that a disabled user is fully locked out, not just logged-out.
     */
    @Test
    @DisplayName("A disabled user can neither log in nor use password recovery")
    void disabledUserCannotLoginOrRecoverPassword() {
        testDataFactory.createAdmin("admin7", PASSWORD);
        User target = testDataFactory.createStudent("recoverme", PASSWORD);
        String link = "disabled-recovery-link";
        testDataFactory.createPasswordRecoveryLink(target, link);

        givenUser("admin7", PASSWORD)
                .when()
                .post("/admin/users/{userId}/disable", target.getId())
                .then()
                .statusCode(200);

        Response loginAttempt = attemptLogin("recoverme", PASSWORD);
        assertThat(loginAttempt.statusCode()).isNotEqualTo(200);
        assertThat(loginAttempt.jsonPath().getString("message")).isEqualTo("accountDisabled");

        givenAnonymousWithCsrf()
                .body(Map.of("username", "recoverme"))
                .when()
                .post("/recover/request")
                .then()
                .statusCode(401);

        givenAnonymous()
                .when()
                .get("/recover/verify/" + link)
                .then()
                .statusCode(401);

        givenAnonymousWithCsrf()
                .body(Map.of("newPassword", "newAttempt123"))
                .when()
                .post("/recover/update-password/" + link)
                .then()
                .statusCode(401);
    }

    private Response attemptLogin(String username, String password) {
        Response csrfResponse = givenAnonymous().when().get("/csrf");
        String csrfToken = csrfResponse.cookie("XSRF-TOKEN");
        Map<String, String> cookies = new java.util.concurrent.ConcurrentHashMap<>(csrfResponse.cookies());

        return RestAssured.given()
                .basePath("/api")
                .contentType(ContentType.JSON)
                .cookies(cookies)
                .header("X-XSRF-TOKEN", csrfToken)
                .body(Map.of("username", username, "password", password))
                .post("/login");
    }

    private RequestSpecification givenAnonymousWithCsrf() {
        Response csrfResponse = givenAnonymous().when().get("/csrf");
        return givenAnonymous()
                .cookies(csrfResponse.cookies())
                .header("X-XSRF-TOKEN", csrfResponse.cookie("XSRF-TOKEN"));
    }
}
