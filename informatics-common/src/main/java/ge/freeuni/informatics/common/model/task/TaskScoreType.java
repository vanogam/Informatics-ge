package ge.freeuni.informatics.common.model.task;

public enum TaskScoreType {
    /**
     *  Sum of all test cases.
     *  When selected, task score parameters should be
     * one float, representing value of each test case;.
     */
    SUM("Sum"),
    /**
     *  Minimum of test case group.
     *  When selected, task score parameters should be
     * formatted like this: "[[m1, t1], [m2, t2], ...]"
     * where m is float - multiplier and t is integer - number of test cases.
     * Sum of all t's must be equal to the quantity of all test cases.
     */
    GROUP_MIN("GroupMin");

    private String code;

    TaskScoreType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
