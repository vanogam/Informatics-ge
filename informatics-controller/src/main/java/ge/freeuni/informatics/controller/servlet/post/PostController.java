package ge.freeuni.informatics.controller.servlet.post;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.LanguageDTO;
import ge.freeuni.informatics.controller.model.PagingRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class PostController {

    @GetMapping(value = "/posts/{postId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    byte[] getPostImage(@PathVariable(required = false) Long postId) {
        if (language == null) {
            language = LanguageDTO.valueOf(defaultLanguage);
        }
        try {
            File file = taskManager.getStatement(task_id, Language.valueOf(language.name()));
            return Files.readAllBytes(file.toPath());
        } catch (InformaticsServerException | IOException ex) {
            log.error("Error during sending statement", ex);
            return null;
        }
    }
}
