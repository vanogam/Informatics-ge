package ge.freeuni.informatics.controller.servlet.user;

import ge.freeuni.informatics.controller.model.LoginResponse;
import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.exception.InformaticsServerException;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.controller.model.RegisterDTO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

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
    public void register(@RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerDTO.getUsername());
        userDTO.setFirstName(registerDTO.getFirstName());
        userDTO.setLastName(registerDTO.getLastName());
        userDTO.setPassword(registerDTO.getPassword());

        userManager.createUser(userDTO);
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
    public LoginResponse logout(HttpServletRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            request.logout();
            response.setStatus("SUCCESS");
        } catch (ServletException ex) {
            response.setStatus("FAIL");
        }

        return response;

    }

    @GetMapping("/get-user")
    public UserDTO getUser() {
        try {
            return userManager.getAuthenticatedUser();
        } catch (InformaticsServerException e) {
            return null;
        }
    }

}
