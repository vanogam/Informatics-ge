package ge.freeuni.informatics.controller.user;

import ge.freeuni.informatics.controller.user.model.LoginResponse;
import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.controller.user.model.RegisterDTO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    public LoginResponse login(@RequestBody AuthenticationDetails authenticationDetails) {
        UserDTO userDTO = userManager.authenticate(authenticationDetails);
        LoginResponse response = new LoginResponse();
        if (userDTO != null) {
            response.setStatus("SUCCESS");
            response.setUsername(userDTO.getUsername());
        } else {
            response.setStatus("FAILURE");
        }
        return response;
    }
}
