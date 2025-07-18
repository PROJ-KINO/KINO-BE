package com.hamss2.KINO.api.review.dto;

import java.util.List;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Getter
@ToString
public class PageResDto<T> {

    private final List<T> content;      // 현재 페이지의 데이터 리스트
    private final int pageNumber;       // 현재 페이지 위치
    private final int pageSize;         // 각 페이지 크기
    private final int totalPages;       // 전페 페이지 수
    private final Long totalElements;   // 전체 데이터 수
    private final boolean last;         // 마지막 페이지인지

    public PageResDto(Page<T> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.last = page.isLast();
    }

}