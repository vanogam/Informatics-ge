package ge.freeuni.informatics.controller.user;

import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.server.user.IUserManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public void register(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam String firstName,
                         @RequestParam String lastName) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);
        userDTO.setPassword(password);
        log.info(userDTO.getUsername() + " " +
                userDTO.getFirstName() + " " +
                userDTO.getLastName() + " " +
                userDTO.getPassword());
    }
}
