package ge.freeuni.informatics.controller.servlet.customtest;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.server.customtest.CustomTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CustomTestController {

    private final CustomTestService customTestService;

    public CustomTestController(CustomTestService customTestService) {
        this.customTestService = customTestService;
    }

    public static class CustomTestRequest {
        private String code;
        private CodeLanguage language;
        private String input;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public CodeLanguage getLanguage() {
            return language;
        }

        public void setLanguage(CodeLanguage language) {
            this.language = language;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }
    }

    public static class CustomTestSubmitResponse extends InformaticsResponse {
        private String key;

        public CustomTestSubmitResponse(String code, String key) {
            super(code);
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static class CustomTestStatusResponse extends InformaticsResponse {
        private String status;
        private Integer timeMillis;
        private Integer memoryKb;
        private String outcome;
        private String message;

        public CustomTestStatusResponse(String code) {
            super(code);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getTimeMillis() {
            return timeMillis;
        }

        public void setTimeMillis(Integer timeMillis) {
            this.timeMillis = timeMillis;
        }

        public Integer getMemoryKb() {
            return memoryKb;
        }

        public void setMemoryKb(Integer memoryKb) {
            this.memoryKb = memoryKb;
        }

        public String getOutcome() {
            return outcome;
        }

        public void setOutcome(String outcome) {
            this.outcome = outcome;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public void setMessage(String message) {
            this.message = message;
        }
    }

    @PostMapping("/custom-test")
    public ResponseEntity<CustomTestSubmitResponse> submit(@RequestBody CustomTestRequest request) {
        try {
            String key = customTestService.createRun(
                    request.getCode(),
                    request.getLanguage() != null ? request.getLanguage() : CodeLanguage.CPP,
                    request.getInput()
            );
            return ResponseEntity.ok(new CustomTestSubmitResponse(null, key));
        } catch (InformaticsServerException e) {
            return ResponseEntity.badRequest().body(new CustomTestSubmitResponse(e.getCode(), null));
        }
    }

    @GetMapping("/custom-test/{key}")
    public ResponseEntity<CustomTestStatusResponse> getStatus(@PathVariable String key) {
        try {
            CustomTestRun run = customTestService.getRunByExternalKey(key);
            CustomTestStatusResponse resp = new CustomTestStatusResponse(null);
            resp.setStatus(run.getStatus());
            resp.setTimeMillis(run.getTimeMillis());
            resp.setMemoryKb(run.getMemoryKb());
            resp.setOutcome(run.getOutcome());
            resp.setMessage(run.getMessage());
            return ResponseEntity.ok(resp);
        } catch (InformaticsServerException e) {
            CustomTestStatusResponse resp = new CustomTestStatusResponse(e.getCode());
            return ResponseEntity.badRequest().body(resp);
        }
    }
}

