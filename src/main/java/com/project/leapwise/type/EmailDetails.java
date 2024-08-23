package com.project.leapwise.type;

import lombok.Data;

import java.util.Map;

@Data
public class EmailDetails {
    private String recipient;
    private String subject;
    private String template;
    private Map<String, Object> variables;
}
