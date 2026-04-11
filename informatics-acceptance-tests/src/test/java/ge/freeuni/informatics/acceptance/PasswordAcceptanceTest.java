package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.user.PasswordRecoveryJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance tests for password management: registration/login with bcrypt,
 * transparent SHA-256 to bcrypt migration, password change, and recovery.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PasswordAcceptanceTest extends BaseAcceptanceTest {

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private PasswordRecoveryJpaRepository recoveryRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        clearAllSessions();
    }

    @Test
    @DisplayName("Register a new user and verify login succeeds with bcrypt")
    void testRegisterAndLogin() {
        RequestSpecification anonWithCsrf = givenAnonymousWithCsrf();

        anonWithCsrf
                .body(Map.of(
                        "username", "newuser",
                        "password", "securePass123",
                        "email", "new@test.com",
                        "firstName", "New",
                        "lastName", "User"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(200);

        User saved = userRepository.getFirstByUsername("newuser");
        assertThat(saved).isNotNull();
        assertThat(saved.getPassword()).startsWith("$2a$");

        givenUser("newuser", "securePass123")
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Legacy SHA-256 user can login and is transparently migrated to bcrypt")
    void testLegacySha256UserTransparentMigration() {
        String password = "legacyPass456";
        User legacy = testDataFactory.createLegacyUser("legacyuser", password,
                ge.freeuni.informatics.common.model.user.UserRole.STUDENT);

        assertThat(legacy.getPassword()).doesNotStartWith("$2");

        givenUser("legacyuser", password)
                .when()
                .get("/user")
                .then()
                .statusCode(200);

        User migrated = userRepository.getFirstByUsername("legacyuser");
        assertThat(migrated.getPassword())
                .as("Password should be re-hashed to bcrypt after login")
                .startsWith("$2a$");
        assertThat(migrated.getPasswordSalt())
                .as("Salt should be cleared after bcrypt migration")
                .isEmpty();
    }

    @Test
    @DisplayName("After migration, user can login again with the same password")
    void testLoginAfterMigrationStillWorks() {
        String password = "migrateMe789";
        testDataFactory.createLegacyUser("migrateuser", password,
                ge.freeuni.informatics.common.model.user.UserRole.STUDENT);

        givenUser("migrateuser", password)
                .when()
                .get("/user")
                .then()
                .statusCode(200);

        clearSession("migrateuser");

        givenUser("migrateuser", password)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Wrong password is rejected for bcrypt user")
    void testWrongPasswordRejectedBcrypt() {
        testDataFactory.createStudent("student1", "correctPass");

        Response loginResponse = attemptLogin("student1", "wrongPass");
        assertThat(loginResponse.statusCode()).isNotEqualTo(200);
    }

    @Test
    @DisplayName("Wrong password is rejected for legacy SHA-256 user")
    void testWrongPasswordRejectedLegacy() {
        testDataFactory.createLegacyUser("legacystudent", "correctLegacy",
                ge.freeuni.informatics.common.model.user.UserRole.STUDENT);

        Response loginResponse = attemptLogin("legacystudent", "wrongLegacy");
        assertThat(loginResponse.statusCode()).isNotEqualTo(200);
    }

    @Test
    @DisplayName("Change password flow: old password works, then new password works, old is rejected")
    void testChangePassword() {
        String oldPassword = "oldPass123";
        String newPassword = "newPass456";
        testDataFactory.createStudent("changeuser", oldPassword);

        givenUser("changeuser", oldPassword)
                .body(Map.of(
                        "oldPassword", oldPassword,
                        "newPassword", newPassword
                ))
                .when()
                .post("/user/change-password")
                .then()
                .statusCode(200);

        clearSession("changeuser");
        logout("changeuser");

        Response oldPassLogin = attemptLogin("changeuser", oldPassword);
        assertThat(oldPassLogin.statusCode()).isNotEqualTo(200);

        givenUser("changeuser", newPassword)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Change password stores bcrypt hash even when migrating from legacy")
    void testChangePasswordFromLegacyStoresBcrypt() {
        String oldPassword = "legacyOld";
        String newPassword = "bcryptNew";
        testDataFactory.createLegacyUser("legacychange", oldPassword,
                ge.freeuni.informatics.common.model.user.UserRole.STUDENT);

        givenUser("legacychange", oldPassword)
                .body(Map.of(
                        "oldPassword", oldPassword,
                        "newPassword", newPassword
                ))
                .when()
                .post("/user/change-password")
                .then()
                .statusCode(200);

        User updated = userRepository.getFirstByUsername("legacychange");
        assertThat(updated.getPassword()).startsWith("$2a$");

        clearSession("legacychange");

        givenUser("legacychange", newPassword)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Password recovery: verify link and reset password")
    void testPasswordRecoveryFlow() {
        String originalPassword = "original123";
        String recoveredPassword = "recovered456";
        User user = testDataFactory.createStudent("recoveruser", originalPassword);

        String recoveryLink = "test-recovery-link-abc123";
        testDataFactory.createPasswordRecoveryLink(user, recoveryLink);

        // Verify the recovery link is valid (this endpoint requires auth in current config)
        givenUser("recoveruser", originalPassword)
                .when()
                .get("/recover/verify/" + recoveryLink)
                .then()
                .statusCode(200);

        // Use the recovery link to set a new password
        givenUser("recoveruser", originalPassword)
                .body(Map.of("newPassword", recoveredPassword))
                .when()
                .post("/recover/update-password/" + recoveryLink)
                .then()
                .statusCode(200);

        User recovered = userRepository.getFirstByUsername("recoveruser");
        assertThat(recovered.getPassword())
                .as("Recovered password should be bcrypt")
                .startsWith("$2a$");

        clearSession("recoveruser");

        givenUser("recoveruser", recoveredPassword)
                .when()
                .get("/user")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Recovery link cannot be reused after password reset")
    void testRecoveryLinkCannotBeReused() {
        User user = testDataFactory.createStudent("reuse_user", "pass123");
        String link = "reuse-link-xyz";
        testDataFactory.createPasswordRecoveryLink(user, link);

        givenUser("reuse_user", "pass123")
                .body(Map.of("newPassword", "newpass789"))
                .when()
                .post("/recover/update-password/" + link)
                .then()
                .statusCode(200);

        clearSession("reuse_user");

        givenUser("reuse_user", "newpass789")
                .body(Map.of("newPassword", "anotherpass"))
                .when()
                .post("/recover/update-password/" + link)
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Duplicate username registration is rejected")
    void testDuplicateUsernameRejected() {
        testDataFactory.createStudent("dupeuser", "pass1");

        givenAnonymousWithCsrf()
                .body(Map.of(
                        "username", "dupeuser",
                        "password", "pass2",
                        "email", "dupe@test.com",
                        "firstName", "Dupe",
                        "lastName", "User"
                ))
                .when()
                .post("/register")
                .then()
                .statusCode(400);
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
