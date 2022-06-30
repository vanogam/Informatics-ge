package ge.freeuni.informatics.model.security;

import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.entity.user.User;

import javax.security.auth.Subject;
import java.security.Principal;

public class InformaticsPrincipal implements Principal {

    private UserDTO user;

    public InformaticsPrincipal(UserDTO user) {
        super();
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public boolean implies(Subject subject) {
        return Principal.super.implies(subject);
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}
