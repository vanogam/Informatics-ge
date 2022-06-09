package ge.freeuni.informatics.model.dto;

import ge.freeuni.informatics.model.entity.contest.Contest;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ContestDTO {

    long id;

    String name;

    Date startDate;

    Long durationInSeconds;

    Long spaceId;

    List<UserDTO> participants;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public List<UserDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDTO> participants) {
        this.participants = participants;
    }

    public static ContestDTO toDTO(Contest contest) {
        ContestDTO contestDTO = new ContestDTO();

        contestDTO.setId(contest.getId());
        contestDTO.setStartDate(contest.getStartDate());
        contestDTO.setDurationInSeconds(contest.getDurationInSeconds());
        contestDTO.setName(contest.getName());
        contestDTO.setParticipants(contest.getParticipants().stream().map(UserDTO::toDTO).collect(Collectors.toList()));

        return contestDTO;
    }

    public static Contest fromDTO(ContestDTO contestDTO) {
        Contest contest = new Contest();

        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setDurationInSeconds(contest.getDurationInSeconds());
        contest.setParticipants(contestDTO.getParticipants().stream().map(UserDTO::fromDTO).collect(Collectors.toList()));

        return contest;
    }
}
