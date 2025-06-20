package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.Language;

public record AddStatementRequest(
        String statement,
        Language language
) {
}