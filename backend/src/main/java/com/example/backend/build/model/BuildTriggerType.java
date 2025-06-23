package com.example.backend.build.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BuildTriggerType {

    BUILD,TEST,DEPLOY;





    @JsonCreator
    public static BuildTriggerType fromString(String value) {
        try {
            return BuildTriggerType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 트리거입니다: " + value);
        }
    }

    }
