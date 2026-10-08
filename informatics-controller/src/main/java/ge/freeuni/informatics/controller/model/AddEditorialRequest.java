package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.Language;

public record AddEditorialRequest(
        String editorial,
        Language language
) {
}
