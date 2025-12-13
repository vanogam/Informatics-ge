package ge.freeuni.informatics.controller.model;

import java.util.List;

public record DeleteTestcasesRequest(
        List<String> testKeys
) {}

