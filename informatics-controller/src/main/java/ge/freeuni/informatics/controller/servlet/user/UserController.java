package ge.freeuni.informatics.controller.servlet.user;

import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.common.dto.AuthenticationDetails;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.server.user.IUserManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public InformaticsResponse register(@RequestBody RegisterDTO registerDTO) {
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
                    ));
            response.setStatus("SUCCESS");
        } catch (Exception ex) {
            response.setStatus("FAIL");
        }
        return response;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<LoginResponse> login(@RequestBody AuthenticationDetails authenticationDetails, HttpServletRequest request) {
        try {
            if (userManager.isLoggedIn()) {
                if (userManager.getAuthenticatedUser().getUsername().equals(authenticationDetails.getUsername())) {
                    return ResponseEntity.ok(new LoginResponse(userManager.getAuthenticatedUser().getUsername()));
                } else {
                    return ResponseEntity.badRequest().body(new LoginResponse(null, "loggedInWithDifferentUser"));
                }
            }
            request.login(authenticationDetails.getUsername(), authenticationDetails.getPassword());
            return ResponseEntity.ok(new LoginResponse(userManager.getAuthenticatedUser().getUsername()));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, ex.getCode()));
        } catch (ServletException e) {
            return ResponseEntity.badRequest().body(new LoginResponse(null, "incorrectCredentials"));
        }
    }

    @GetMapping("/login")
    public void login() {

    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.logout();
            response.sendRedirect("/");
        } catch (ServletException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/get-user")
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
            return new InformaticsResponse("FAIL", ex.getMessage());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/recover/request")
    public InformaticsResponse requestRecovery(@RequestBody AddRecoveryRequest request) {
        try {
            userManager.addPasswordRecoveryQuery(request.getUsername());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getMessage());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/recover/update-password/{link}")
    public InformaticsResponse recover(@PathVariable String link, @RequestBody RecoverPasswordRequest request) {
        try {
            userManager.recoverPassword(link, request.getNewPassword());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getMessage());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

}
