package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDTO {

    private long id;

    private String username;

    private String password;

    private String email;

    private String firstName;

    private String lastName;

    private Integer version;

    private List<String> roles = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public static UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setEmail(user.getEmail());
        userDTO.setLastName(user.getLastName());
        userDTO.setVersion(user.getVersion());
        userDTO.setRoles(Arrays.asList(user.getRoles().split(",")));

        return userDTO;
    }

    public static User fromDTO(UserDTO userDTO) {
        User user = new User();

        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setVersion(userDTO.getVersion());
        user.setRoles(userDTO.getRoles().stream().reduce("", (last, s) -> last + (!last.isEmpty() ? "," : "") + s));

        return user;
    }
}
