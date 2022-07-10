package ge.freeuni.informatics.common.security;

import ge.freeuni.informatics.common.model.user.User;

import javax.security.auth.Subject;
import java.security.Principal;

public class InformaticsPrincipal implements Principal {

    private User user;

    public InformaticsPrincipal(User user) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
