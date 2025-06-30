package com.example.backend.jenkins.build.model;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum JobType {


    LATEST,
    HISTORY;



    @JsonCreator
    public static JobType fromString(String value) {
        try {
            return JobType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 JobType입니다: " + value);
        }
    }


}
