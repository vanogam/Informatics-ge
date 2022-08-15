package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ContestDTO {

    long id;

    String name;

    Date startDate;

    Integer durationInSeconds;

    Long roomId;

    ContestStatus status;
    List<UserDTO> participants;

    List<TaskDTO> tasks = new ArrayList<>();

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

    public ContestStatus getStatus() {
        return status;
    }

    public void setStatus(ContestStatus status) {
        this.status = status;
    }

    public List<UserDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDTO> participants) {
        this.participants = participants;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public static ContestDTO toDTO(Contest contest) {
        ContestDTO contestDTO = new ContestDTO();

        contestDTO.setId(contest.getId());
        contestDTO.setRoomId(contest.getRoomId());
        contestDTO.setStartDate(contest.getStartDate());
        contestDTO.setDurationInSeconds(contest.getDurationInSeconds());
        contestDTO.setName(contest.getName());
        contestDTO.setStatus(contest.getStatus());
        if (contest.getParticipants() != null) {
            contestDTO.setParticipants(contest.getParticipants().stream().map(UserDTO::toDTO).collect(Collectors.toList()));
        }
        contestDTO.setTasks(TaskDTO.toDTOs(contest.getTasks()));

        return contestDTO;
    }

    public static Contest fromDTO(ContestDTO contestDTO) {
        Contest contest = new Contest();

        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setRoomId(contestDTO.getRoomId());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setDurationInSeconds(contest.getDurationInSeconds());
        contest.setStatus(contestDTO.getStatus());
        if (contestDTO.getParticipants() != null) {
            contest.setParticipants(contestDTO.getParticipants().stream().map(UserDTO::fromDTO).collect(Collectors.toList()));
        }
        contest.setTasks(TaskDTO.fromDTOs(contestDTO.getTasks()));

        return contest;
    }

    public static List<Contest> fromDTOs(List<ContestDTO> contestDTOs) {
        List<Contest> contests = new ArrayList<>();
        for (ContestDTO contestDTO : contestDTOs) {
            contests.add(fromDTO(contestDTO));
        }
        return contests;
    }

    public static List<ContestDTO> toDTOs(List<Contest> contests) {
        List<ContestDTO> contestsDTOs = new ArrayList<>();
        for (Contest contest : contests) {
            contestsDTOs.add(toDTO(contest));
        }
        return contestsDTOs;
    }
}
