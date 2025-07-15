package com.hamss2.KINO.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReqDto {
    private Long reportId;
    private Long reportedId;
    private int result;
}
