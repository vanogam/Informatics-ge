package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.user.User;

import java.util.ArrayList;
import java.util.List;

public class UserSimpleDTO {


    private long id;

    private String username;

    public long getId() {
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

    public static UserSimpleDTO toSimpleDTO(User user) {
        UserSimpleDTO userSimpleDTO = new UserSimpleDTO();
        userSimpleDTO.setId(user.getId());
        userSimpleDTO.setUsername(user.getUsername());
        return userSimpleDTO;
    }

    public static List<UserSimpleDTO> toSimpleDTOs(List<User> users) {
        List<UserSimpleDTO> userSimpleDTOs = new ArrayList<>();
        for (User user : users) {
            userSimpleDTOs.add(toSimpleDTO(user));
        }
        return userSimpleDTOs;
    }
}
