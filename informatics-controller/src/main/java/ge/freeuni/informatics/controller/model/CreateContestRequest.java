package ge.freeuni.informatics.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ge.freeuni.informatics.common.model.contest.ScoringType;

import java.time.LocalDateTime;


public class CreateContestRequest {

    private String name;

    @JsonFormat(pattern="dd/MM/yyyy HH:mm")
    private LocalDateTime startDate;

    private Integer durationInSeconds;

    private Long roomId;

    private boolean upsolvingAfterFinish;

    private ScoringType scoringType;

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

    public boolean isUpsolvingAfterFinish() {
        return upsolvingAfterFinish;
    }

    public void setUpsolvingAfterFinish(boolean upsolvingAfterFinish) {
        this.upsolvingAfterFinish = upsolvingAfterFinish;
    }

    public ScoringType getScoringType() {
        return scoringType;
    }

    public void setScoringType(ScoringType scoringType) {
        this.scoringType = scoringType;
    }
}
