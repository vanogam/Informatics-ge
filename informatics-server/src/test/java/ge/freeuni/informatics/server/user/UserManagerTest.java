
package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.PasswordRecoveryJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.utils.MailSender;
import ge.freeuni.informatics.utils.UserUtils;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private MailSender mailSender;

    @Mock
    private PasswordRecoveryJpaRepository recoveryJpaRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserManager userManager;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Set password recovery validity using reflection
        ReflectionTestUtils.setField(userManager, "passwordRecoveryValidityMinutes", "60");
        ReflectionTestUtils.setField(userManager, "emailAddress", "test@example.com");
        ReflectionTestUtils.setField(userManager, "emailPassword", "password");
        ReflectionTestUtils.setField(userManager, "emailHost", "smtp.example.com");
        ReflectionTestUtils.setField(userManager, "host", "http://localhost");
        ReflectionTestUtils.setField(userManager, "port", "8080");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordSalt("salt123");
        testUser.setPassword("hashedPassword");
        testUser.setRole("STUDENT");
        testUser.setVersion(1);

        testUserDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                1,
                "STUDENT"
        );
    }

    @Test
    void testGetUser() {
        when(userRepository.getReferenceById(1L)).thenReturn(testUser);
        
        User result = userManager.getUser(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).getReferenceById(1L);
    }

    @Test
    void testCreateUser_Success() throws InformaticsServerException {
        UserDTO newUserDTO = new UserDTO(
                0L,
                "newuser",
                "new@example.com",
                "New",
                "User",
                null,
                null
        );

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        userManager.createUser(newUserDTO, "password123");

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                user.getEmail().equals("new@example.com") &&
                user.getRole().equals("STUDENT") &&
                user.getVersion() == 1 &&
                user.getPasswordSalt() != null &&
                user.getPassword() != null
        ));
    }

    @Test
    void testCreateUser_DuplicateUsername() {
        UserDTO newUserDTO = new UserDTO(
                0L,
                "existinguser",
                "new@example.com",
                "New",
                "User",
                null,
                null
        );

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Unique constraint violation"));

        assertThrows(InformaticsServerException.class, () -> {
            userManager.createUser(newUserDTO, "password123");
        });

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testAuthenticate_Success() {
        // Use a known salt and password for testing
        String testSalt = "testsalt";
        String testPassword = "password123";
        
        // Compute the actual hash using UserUtils (same method used in UserManager)
        String expectedHash = UserUtils.getHash(testPassword, testSalt);
        
        User userWithPassword = new User();
        userWithPassword.setId(1L);
        userWithPassword.setUsername("testuser");
        userWithPassword.setPasswordSalt(testSalt);
        userWithPassword.setPassword(expectedHash);
        userWithPassword.setEmail("test@example.com");
        userWithPassword.setFirstName("Test");
        userWithPassword.setLastName("User");
        userWithPassword.setRole("STUDENT");

        when(userRepository.getFirstByUsername("testuser")).thenReturn(userWithPassword);
        
        // Authenticate with the correct password
        User result = userManager.authenticate("testuser", testPassword);
        
        // Verify authentication succeeded
        assertNotNull(result, "Authentication should succeed with correct password");
        assertEquals("testuser", result.getUsername(), "Username should match");
        assertEquals(1L, result.getId(), "User ID should match");
        
        // Verify repository was called
        verify(userRepository).getFirstByUsername("testuser");
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.getFirstByUsername("nonexistent")).thenReturn(null);

        User result = userManager.authenticate("nonexistent", "password");

        assertNull(result);
        verify(userRepository).getFirstByUsername("nonexistent");
    }

    @Test
    void testAuthenticate_WrongPassword() {
        when(userRepository.getFirstByUsername("testuser")).thenReturn(testUser);

        User result = userManager.authenticate("testuser", "wrongpassword");

        assertNull(result);
        verify(userRepository).getFirstByUsername("testuser");
    }

    @Test
    void testIsLoggedIn_True() {
        InformaticsPrincipal principal = new InformaticsPrincipal(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        boolean result = userManager.isLoggedIn();

        assertTrue(result);
    }

    @Test
    void testIsLoggedIn_False() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        boolean result = userManager.isLoggedIn();

        assertFalse(result);
    }

    @Test
    void testGetAuthenticatedUser_Success() throws InformaticsServerException {
        InformaticsPrincipal principal = new InformaticsPrincipal(testUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        UserDTO result = userManager.getAuthenticatedUser();

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals(1L, result.id());
    }

    @Test
    void testGetAuthenticatedUser_NotLoggedIn() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.setContext(securityContext);

        assertThrows(InformaticsServerException.class, () -> {
            userManager.getAuthenticatedUser();
        });
    }

    @Test
    void testAddPasswordRecoveryQuery_Success() throws InformaticsServerException {
        when(userRepository.getFirstByUsername("testuser")).thenReturn(testUser);
        when(recoveryJpaRepository.save(any(RecoverPassword.class))).thenAnswer(invocation -> {
            RecoverPassword rp = invocation.getArgument(0);
            return rp;
        });

        userManager.addPasswordRecoveryQuery("testuser");

        verify(userRepository).getFirstByUsername("testuser");
        verify(recoveryJpaRepository).save(argThat(recoverPassword ->
                recoverPassword.getUserId() == 1L &&
                recoverPassword.getLink() != null &&
                !recoverPassword.isUsed()
        ));
    }

    @Test
    void testAddPasswordRecoveryQuery_InvalidUsername() {
        when(userRepository.getFirstByUsername("nonexistent")).thenReturn(null);

        assertThrows(InformaticsServerException.class, () -> {
            // Simulate NoResultException scenario
            when(userRepository.getFirstByUsername("nonexistent"))
                    .thenThrow(new NoResultException());
            userManager.addPasswordRecoveryQuery("nonexistent");
        });
    }

    @Test
    void testVerifyRecoveryQuery_Success() throws InformaticsServerException {
        RecoverPassword recoverPassword = new RecoverPassword();
        recoverPassword.setUserId(1L);
        recoverPassword.setLink("validlink");
        recoverPassword.setUsed(false);
        recoverPassword.setCreateTime(new Date()); // Current time

        when(recoveryJpaRepository.getFirstByLink("validlink")).thenReturn(recoverPassword);

        RecoverPassword result = userManager.verifyRecoveryQuery("validlink");

        assertNotNull(result);
        assertEquals("validlink", result.getLink());
        assertFalse(result.isUsed());
        verify(recoveryJpaRepository).getFirstByLink("validlink");
    }

    @Test
    void testVerifyRecoveryQuery_InvalidLink() {
        when(recoveryJpaRepository.getFirstByLink("invalidlink"))
                .thenThrow(new NoResultException());

        assertThrows(InformaticsServerException.class, () -> {
            userManager.verifyRecoveryQuery("invalidlink");
        });
    }

    @Test
    void testVerifyRecoveryQuery_LinkAlreadyUsed() {
        RecoverPassword recoverPassword = new RecoverPassword();
        recoverPassword.setLink("usedlink");
        recoverPassword.setUsed(true);
        recoverPassword.setCreateTime(new Date());

        when(recoveryJpaRepository.getFirstByLink("usedlink")).thenReturn(recoverPassword);

        assertThrows(InformaticsServerException.class, () -> {
            userManager.verifyRecoveryQuery("usedlink");
        });
    }

    @Test
    void testVerifyRecoveryQuery_LinkExpired() {
        RecoverPassword recoverPassword = new RecoverPassword();
        recoverPassword.setLink("expiredlink");
        recoverPassword.setUsed(false);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -61); // 61 minutes ago (expired)
        recoverPassword.setCreateTime(calendar.getTime());

        when(recoveryJpaRepository.getFirstByLink("expiredlink")).thenReturn(recoverPassword);

        assertThrows(InformaticsServerException.class, () -> {
            userManager.verifyRecoveryQuery("expiredlink");
        });
    }

    @Test
    void testRecoverPassword_Success() throws InformaticsServerException {
        RecoverPassword recoverPassword = new RecoverPassword();
        recoverPassword.setUserId(1L);
        recoverPassword.setLink("validlink");
        recoverPassword.setUsed(false);
        recoverPassword.setCreateTime(new Date());

        when(recoveryJpaRepository.getFirstByLink("validlink")).thenReturn(recoverPassword);
        when(userRepository.getReferenceById(1L)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(recoveryJpaRepository.save(any(RecoverPassword.class))).thenReturn(recoverPassword);

        userManager.recoverPassword("validlink", "newPassword123");

        verify(userRepository).getReferenceById(1L);
        verify(userRepository).save(argThat(user ->
                user.getPasswordSalt() != null &&
                user.getPassword() != null
        ));
        verify(recoveryJpaRepository).save(argThat(rp -> rp.isUsed()));
    }
}