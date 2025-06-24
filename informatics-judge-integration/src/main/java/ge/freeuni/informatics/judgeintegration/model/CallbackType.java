package ge.freeuni.informatics.judgeintegration.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CallbackType {
    COMPILATION_STARTED,
    COMPILATION_COMPLETED,
    COMPILATION_FAILED,
    TEST_COMPLETED,
    SYSTEM_ERROR;

    @JsonCreator
    public static CallbackType fromString(String value) {
        return CallbackType.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toString() {
        return name();
    }
}
