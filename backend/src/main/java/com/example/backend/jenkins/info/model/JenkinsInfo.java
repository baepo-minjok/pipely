package com.example.backend.jenkins.info.model;

import com.example.backend.auth.user.model.Users;
import com.example.backend.converter.CryptoConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jenkins_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JenkinsInfo {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    @Column(name = "jenkins_id", nullable = false)
    private String jenkinsId;

    @NotBlank
    @Column(name = "secret_key", nullable = false)
    @Convert(converter = CryptoConverter.class)
    private String secretKey;

    @NotBlank
    @Column(name = "uri", nullable = false)
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
