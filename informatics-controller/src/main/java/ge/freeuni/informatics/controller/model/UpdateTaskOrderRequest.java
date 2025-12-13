package ge.freeuni.informatics.controller.model;

import java.util.List;

public record UpdateTaskOrderRequest(
        List<Long> taskIds
) {}

