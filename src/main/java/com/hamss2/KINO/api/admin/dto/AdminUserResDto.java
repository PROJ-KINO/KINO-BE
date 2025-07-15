package com.hamss2.KINO.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResDto {
    private Long id;
    private String nickname;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
