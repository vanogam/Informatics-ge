package ge.freeuni.informatics.common.model;

public enum CodeLanguage {
    CPP("cpp", "gnu C++20"),
    PYTHON("py", "Python 3.11");

    private final String suffix;

    private final String description;

    CodeLanguage(String suffix, String description){
        this.suffix = suffix;
        this.description = description;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getDescription() {
        return description;
    }
}
