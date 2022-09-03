package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.UserSimpleDTO;

import java.util.List;

public class RegistrantsResponse extends InformaticsResponse {

    List<UserSimpleDTO> registrants;

    public RegistrantsResponse(String status, String message, List<UserSimpleDTO> registrants) {
        super(status, message);
        this.registrants = registrants;
    }

    public List<UserSimpleDTO> getRegistrants() {
        return registrants;
    }

    public void setRegistrants(List<UserSimpleDTO> registrants) {
        this.registrants = registrants;
    }
}
