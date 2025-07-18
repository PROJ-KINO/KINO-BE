package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Setter
@Slf4j
@Table(name = "my_pick_movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPickMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long myPickMovieId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movieId", nullable = false)
    @JsonIgnore
    private Movie movie;

}
