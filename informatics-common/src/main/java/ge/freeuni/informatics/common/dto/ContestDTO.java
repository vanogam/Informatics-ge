package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ScoringType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ContestDTO {

    private Long id;

    private String name;

    private Date startDate;

    private Date endDate;

    private Long roomId;

    private ContestStatus status;

    private List<UserDTO> participants;

    private List<TaskDTO> tasks = new ArrayList<>();

    private TreeSet<ContestantResultDTO> standings;

    private List<ContestantResultDTO> upsolvingStandings;

    private ScoringType scoringType;

    private boolean upsolving;

    private boolean upsolvingAfterFinish;

    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public TreeSet<ContestantResultDTO> getStandings() {
        return standings;
    }

    public void setStandings(TreeSet<ContestantResultDTO> standings) {
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<ContestantResultDTO> getUpsolvingStandings() {
        return upsolvingStandings;
    }

    public void setUpsolvingStandings(List<ContestantResultDTO> upsolvingStandings) {
        this.upsolvingStandings = upsolvingStandings;
    }

    public static ContestDTO toDTO(Contest contest) {
        ContestDTO contestDTO = new ContestDTO();

        contestDTO.setId(contest.getId());
        contestDTO.setRoomId(contest.getRoomId());
        contestDTO.setStartDate(contest.getStartDate());
        contestDTO.setEndDate(contest.getEndDate());
        contestDTO.setName(contest.getName());
        if (contest.getParticipants() != null) {
            contestDTO.setParticipants(contest.getParticipants()
                    .stream()
                    .map(UserDTO::toDTO)
                    .collect(Collectors.toList()));
        }
        contestDTO.setStandings(contest.getStandings()
                .stream()
                .map(ContestantResultDTO::toDTO)
                .collect(Collectors.toCollection(TreeSet::new)));
        contestDTO.setTasks(TaskDTO.toDTOs(contest.getTasks()));
        contestDTO.setUpsolving(contest.isUpsolving());
        contestDTO.setUpsolvingAfterFinish(contest.isUpsolvingAfterFinished());
        contestDTO.setScoringType(contest.getScoringType());
        contestDTO.setVersion(contest.getVersion());
        contestDTO.setUpsolvingStandings(contest.getUpsolvingStandings()
                .stream().map(ContestantResultDTO::toDTO)
                .collect(Collectors.toList()));
        return contestDTO;
    }

    public static Contest fromDTO(ContestDTO contestDTO) {
        Contest contest = new Contest();

        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setRoomId(contestDTO.getRoomId());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setEndDate(contestDTO.getEndDate());
        if (contestDTO.getParticipants() != null) {
            contest.setParticipants(contestDTO.getParticipants()
                    .stream().map(UserDTO::fromDTO)
                    .collect(Collectors.toList())
            );
        }
        contest.setTasks(TaskDTO.fromDTOs(contestDTO.getTasks()));
        contest.setStandings(contestDTO.getStandings()
                .stream()
                .map(ContestantResultDTO::fromDTO)
                .toList()
        );
        contest.setUpsolving(contestDTO.isUpsolving());
        contest.setUpsolvingAfterFinished(contestDTO.isUpsolvingAfterFinish());
        contest.setScoringType(contestDTO.getScoringType());
        contest.setVersion(contestDTO.getVersion());
        contest.setUpsolvingStandings(contestDTO.getUpsolvingStandings().stream().map(ContestantResultDTO::fromDTO).toList());
        return contest;
    }
}
