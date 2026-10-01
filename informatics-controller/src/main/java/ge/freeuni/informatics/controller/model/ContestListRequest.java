package ge.freeuni.informatics.controller.model;

public class ContestListRequest extends PagingRequest {

    private Integer roomId;

    /** Case-insensitive substring match against the contest name; null/blank matches every contest. */
    private String name;

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
