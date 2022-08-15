package ge.freeuni.informatics.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;


public class CreateContestRequest {

    String name;

    @JsonFormat(pattern="dd/MM/yyyy HH:mm")
    LocalDateTime startDate;

    Integer durationInSeconds;

    Long roomId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
