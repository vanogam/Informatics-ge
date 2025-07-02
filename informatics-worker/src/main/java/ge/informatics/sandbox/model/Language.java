package ge.informatics.sandbox.model;

import ge.informatics.sandbox.executors.CppExecutor;
import ge.informatics.sandbox.executors.Executor;
import ge.informatics.sandbox.executors.PythonExecutor;

public enum Language {
    JAVA("java", "openjdk:21", null),
    PYTHON("python", "python:3.9", new PythonExecutor()),
    C("c", "cpp-worker", null),
    CPP("cpp", "cpp-worker", new CppExecutor()),
    CSHARP("csharp", "mcr.microsoft.com/dotnet/sdk:6.0", null),
    RUBY("ruby", "ruby:latest", null),
    GO("go", "golang:latest", null),
    KOTLIN("kotlin", "openjdk:21", null),
    PHP("php", "php:latest", null),
    JAVASCRIPT("javascript", "node:latest", null);

    private final String name;
    private final String image;
    private final Executor executor;

    Language(String name, String image, Executor executor) {
        this.name = name;
        this.image = image;
        this.executor = executor;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public Executor getExecutor() {
        return executor;
    }
}