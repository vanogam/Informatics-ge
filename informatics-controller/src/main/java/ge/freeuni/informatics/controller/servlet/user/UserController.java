package ge.freeuni.informatics.controller.servlet.user;

import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.common.dto.AuthenticationDetails;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.server.user.IUserManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
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
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerDTO.getUsername());
        userDTO.setFirstName(registerDTO.getFirstName());
        userDTO.setLastName(registerDTO.getLastName());
        userDTO.setPassword(registerDTO.getPassword());
        InformaticsResponse response = new InformaticsResponse();
        try {
            userManager.createUser(UserDTO.fromDTO(userDTO));
            response.setStatus("SUCCESS");
        } catch (Exception ex) {
            response.setStatus("FAIL");
        }
        return response;
    }

    @PostMapping("/login")
    @ResponseBody
    public LoginResponse login(@RequestBody AuthenticationDetails authenticationDetails, HttpServletRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            request.login(authenticationDetails.getUsername(), authenticationDetails.getPassword());
            response.setMessage(userManager.getAuthenticatedUser().getUsername());
            response.setStatus("SUCCESS");
        } catch (ServletException ex) {
            response.setStatus("FAIL");
        } catch (InformaticsServerException e) {
            throw new RuntimeException(e);
        }

        return response;

    }

    @GetMapping("/login")
    @ResponseBody
    public void login() {

    }

    @PostMapping("/logout")
    @ResponseBody
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.logout();
            response.sendRedirect("/");
        } catch (ServletException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @GetMapping("/get-user")
    public UserDTO getUser() {
        try {
            return userManager.getAuthenticatedUser();
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

    @GetMapping("/recover/request")
    public InformaticsResponse requestRecovery(@RequestBody AddRecoveryRequest request) {
        try {
            userManager.addPasswordRecoveryQuery(request.getUsername());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getMessage());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/recover/{link}")
    public InformaticsResponse recover(@PathVariable String link, @RequestBody RecoverPasswordRequest request) {
        try {
            userManager.recoverPassword(link, request.getNewPassword());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getMessage());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

}
