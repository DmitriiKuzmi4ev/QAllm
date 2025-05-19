package org.example.controller;

public class TestRequest {

    private String prompt;
    private String testType;
    private String framework;
    private String apiFramework;
    private String language;

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }

    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }

    public String getApiFramework() { return apiFramework; }
    public void setApiFramework(String apiFramework) { this.apiFramework = apiFramework; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}