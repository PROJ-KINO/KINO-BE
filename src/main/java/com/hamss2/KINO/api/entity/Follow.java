package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Getter
@Setter
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"followerId", "followeeId"})
        }
)
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long followId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followerId", nullable = false)
    @JsonIgnore
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followeeId", nullable = false)
    @JsonIgnore
    private User followee;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
