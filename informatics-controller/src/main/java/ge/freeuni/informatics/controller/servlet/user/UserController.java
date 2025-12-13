package ge.freeuni.informatics.controller.servlet.user;

import ge.freeuni.informatics.common.dto.AuthenticationDetails;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.user.IUserManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    final Logger log;
    final IUserManager userManager;

    @Autowired
    public UserController(IUserManager userManager, Logger log) {
        this.userManager = userManager;
        this.log = log;
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<InformaticsResponse> register(@RequestBody RegisterDTO registerDTO) {
        InformaticsResponse response = new InformaticsResponse();
        try {
            userManager.createUser(
                    new UserDTO(
                            0,
                            registerDTO.getUsername(),
                            registerDTO.getEmail(),
                            registerDTO.getFirstName(),
                            registerDTO.getLastName(),
                            0,
                            null
                    ), registerDTO.getPassword());
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            log.error("Registration failed for user: {}", registerDTO.getUsername(), ex);
            response.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody AuthenticationDetails authenticationDetails, HttpServletRequest request) {
        try {
            if (userManager.isLoggedIn()) {
                if (userManager.getAuthenticatedUser().username().equals(authenticationDetails.username())) {
                    return ResponseEntity.ok(new LoginResponse(userManager.getAuthenticatedUser().username()));
                } else {
                    return ResponseEntity.badRequest().body(new LoginResponse(null, "loggedInWithDifferentUser"));
                }
            }
            request.login(authenticationDetails.username(), authenticationDetails.password());
            return ResponseEntity.ok(new LoginResponse(userManager.getAuthenticatedUser().username()));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, ex.getCode()));
        } catch (ServletException e) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, "incorrectCredentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.logout();
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getUser() {
        try {
            return ResponseEntity.ok(userManager.getAuthenticatedUser());
        } catch (InformaticsServerException e) {
            return null;
        }
    }

    @GetMapping("/recover/verify/{link}")
    public InformaticsResponse verifyLink(@PathVariable String link) {
        try {
            userManager.verifyRecoveryQuery(link);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse(ex.getCode());
        }
        return new InformaticsResponse(null);
    }

    @PostMapping("/recover/request")
    public InformaticsResponse requestRecovery(@RequestBody AddRecoveryRequest request) {
        try {
            userManager.addPasswordRecoveryQuery(request.getUsername());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse(ex.getCode());
        }
        return new InformaticsResponse(null);
    }

    @PostMapping("/recover/update-password/{link}")
    public InformaticsResponse recover(@PathVariable String link, @RequestBody RecoverPasswordRequest request) {
        try {
            userManager.recoverPassword(link, request.getNewPassword());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse(ex.getCode());
        }
        return new InformaticsResponse(null);
    }

}
