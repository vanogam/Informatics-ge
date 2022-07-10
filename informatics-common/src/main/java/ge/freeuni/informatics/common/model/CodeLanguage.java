package ge.freeuni.informatics.common.model;

public enum CodeLanguage {
    CPP("cpp", "gnu C++17");

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
