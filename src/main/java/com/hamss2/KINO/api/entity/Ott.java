package com.hamss2.KINO.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Ott {
    @Id
    private Long ottId;

    private String name;

    private String logoUrl;

    @OneToMany(mappedBy = "ott", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieOtt> movies;
}
