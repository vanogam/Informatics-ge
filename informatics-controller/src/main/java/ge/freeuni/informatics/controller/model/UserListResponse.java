package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.AdminUserDTO;

import java.util.List;

public class UserListResponse extends InformaticsResponse {

    List<AdminUserDTO> users;

    long totalCount;

    public UserListResponse() {}

    public UserListResponse(String message) {
        super(message);
    }

    public List<AdminUserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<AdminUserDTO> users) {
        this.users = users;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
