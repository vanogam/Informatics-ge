package ge.freeuni.informatics.common.dto;

public record TestcaseDTO (
    String key,
    boolean isPublic,
    String inputSnippet,
    String outputSnippet
) {
    public TestcaseDTO (String key, boolean isPublic, String inputSnippet, String outputSnippet) {
        this.key = key;
        this.isPublic = isPublic;
        this.inputSnippet = inputSnippet;
        this.outputSnippet = outputSnippet;
    }

    public TestcaseDTO (String key, boolean isPublic) {
        this(key, isPublic, null, null);
    }
}
