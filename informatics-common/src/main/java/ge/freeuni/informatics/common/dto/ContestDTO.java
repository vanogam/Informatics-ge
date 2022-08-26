package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ScoringType;
import ge.freeuni.informatics.common.model.contest.Standings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContestDTO {

    private long id;

    private String name;

    private Date startDate;

    private Integer durationInSeconds;

    private Long roomId;

    private ContestStatus status;

    private List<Long> participants;

    private List<TaskDTO> tasks = new ArrayList<>();

    private Standings standings;

    private ScoringType scoringType;

    private boolean upsolving;

    private boolean upsolvingAfterFinish;

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

    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public Standings getStandings() {
        return standings;
    }

    public void setStandings(Standings standings) {
        this.standings = standings;
    }

    public ScoringType getScoringType() {
        return scoringType;
    }

    public void setScoringType(ScoringType scoringType) {
        this.scoringType = scoringType;
    }

    public boolean isUpsolving() {
        return upsolving;
    }

    public void setUpsolving(boolean upsolving) {
        this.upsolving = upsolving;
    }

    public boolean isUpsolvingAfterFinish() {
        return upsolvingAfterFinish;
    }

    public void setUpsolvingAfterFinish(boolean upsolvingAfterFinish) {
        this.upsolvingAfterFinish = upsolvingAfterFinish;
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
            contestDTO.setParticipants(contest.getParticipants());
        }
        contestDTO.setStandings(contest.getStandings());
        contestDTO.setTasks(TaskDTO.toDTOs(contest.getTasks()));
        contestDTO.setUpsolving(contest.isUpsolving());
        contestDTO.setUpsolvingAfterFinish(contestDTO.isUpsolvingAfterFinish());
        contestDTO.setScoringType(contest.getScoringType());
        return contestDTO;
    }

    public static Contest fromDTO(ContestDTO contestDTO) {
        Contest contest = new Contest();

        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setRoomId(contestDTO.getRoomId());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setDurationInSeconds(contestDTO.getDurationInSeconds());
        contest.setStatus(contestDTO.getStatus());
        if (contestDTO.getParticipants() != null) {
            contest.setParticipants(contestDTO.getParticipants());
        }
        contest.setTasks(TaskDTO.fromDTOs(contestDTO.getTasks()));
        contest.setStandings(contestDTO.getStandings());
        contest.setUpsolving(contestDTO.isUpsolving());
        contest.setUpsolvingAfterFinished(contestDTO.isUpsolvingAfterFinish());
        contest.setScoringType(contestDTO.getScoringType());
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
