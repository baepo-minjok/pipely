package com.example.backend.auth.user.model;


import com.example.backend.jenkins.info.model.JenkinsInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Users {

    @Id
    private String id;

    private String email;

    private String name;

    private String password;

    private String phoneNumber;

    private UserStatus status;

    private String roles;

    private String provider;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JenkinsInfo> jenkinsInfos = new ArrayList<>();

    public enum UserStatus {
        ACTIVE,
        DORMANT,
        WITHDRAWN,
        UNVERIFIED
    }
}
