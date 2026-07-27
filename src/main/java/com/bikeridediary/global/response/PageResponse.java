package com.bikeridediary.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.function.Function;

// Page(Slice 겸용) → 앱 친화적 응답 래퍼.
// Page: 총 개수/페이지 수 포함.
// Slice: content + hasNext만 있고 count 안 함(성능 우수). totalElements/totalPages는 null로 내려감.
// null 필드는 JSON에서 제외되어 앱은 존재 여부로 판별.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        Long totalElements,    // Slice면 null
        Integer totalPages,    // Slice면 null
        boolean hasNext,
        boolean hasPrevious
) {

    // Page → PageResponse. 총 개수 포함.
    public static <E, R> PageResponse<R> of(Page<E> page, Function<E, R> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }

    // Slice → PageResponse. 총 개수 없이 hasNext만 담아 count 쿼리 절약.
    public static <E, R> PageResponse<R> ofSlice(Slice<E> slice, Function<E, R> mapper) {
        return new PageResponse<>(
                slice.getContent().stream().map(mapper).toList(),
                slice.getNumber(),
                slice.getSize(),
                null,
                null,
                slice.hasNext(),
                slice.hasPrevious()
        );
    }
}
