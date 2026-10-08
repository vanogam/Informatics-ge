package ge.freeuni.informatics.common.dto;

import java.util.List;

/**
 * Tells a viewer which of a task's editorial/solution are currently available to them, so the
 * UI can grey out the corresponding tab instead of fetching the content itself. {@code
 * solutionLanguages} is already filtered down to the languages that actually have code - a
 * contestant should never see a language in the combobox with nothing behind it.
 */
public record TaskMaterialsDTO(
        boolean editorialAvailable,
        List<String> solutionLanguages
) {
}
