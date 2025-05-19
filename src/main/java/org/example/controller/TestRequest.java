package org.example.controller;


import lombok.*;

@Setter
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {

    private String prompt;
    private String testType;
    private String framework;
    private String apiFramework;
}
