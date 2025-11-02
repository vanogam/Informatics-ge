package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.TestcaseDTO;
import ge.freeuni.informatics.common.model.task.Statement;

import java.util.List;

public class StatementResponse extends  InformaticsResponse {
        Statement statement;
        List<TestcaseDTO> publicTestcases;

    public StatementResponse(Statement statement, List<TestcaseDTO> publicTestcases) {
        this.statement = statement;
        this.publicTestcases = publicTestcases;
    }

    public StatementResponse(String message) {
        super(message);
    }

    public Statement getStatement() {
        return statement;
    }

    public List<TestcaseDTO> getPublicTestcases() {
        return publicTestcases;
    }
}
