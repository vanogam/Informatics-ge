package ge.freeuni.informatics.controller.servlet;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Catches {@link InformaticsServerException} for any controller that doesn't handle it itself,
 * mapping it to the status code {@link ServletUtils#getResponseCode} says it carries. Endpoints
 * whose error responses need extra fields (e.g. task/submission lists returning "FAIL" bodies of
 * their own shape) still catch it locally instead - this only covers the plain case.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InformaticsServerException.class)
    public ResponseEntity<InformaticsResponse> handleInformaticsServerException(InformaticsServerException ex) {
        return ResponseEntity
                .status(ServletUtils.getResponseCode(ex))
                .body(new InformaticsResponse(ex.getCode()));
    }
}
