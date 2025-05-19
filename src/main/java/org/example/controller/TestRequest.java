package org.example.controller;

public class TestRequest {

    private String prompt;
    private String testType;
    private String framework;
    private String apiFramework;

    public String getPrompt() {
        return prompt;
    }

    public String getTestType() {
        return testType;
    }

    public String getFramework() {
        return framework;
    }

    public String getApiFramework() {
        return apiFramework;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void setApiFramework(String apiFramework) {
        this.apiFramework = apiFramework;
    }
}
