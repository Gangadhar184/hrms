package com.example.hrms.mappers;

import com.example.hrms.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageMapper {

    /**
     * Convert Spring Data Page to custom PageResponse
     */
    public <T> PageResponse<T> toPageResponse(Page<T> page) {
        if (page == null) {
            return PageResponse.<T>builder()
                    .content(List.of())
                    .currentPage(0)
                    .totalPages(0)
                    .totalElements(0)
                    .pageSize(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        return PageResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Convert Spring Data Page with entity mapping to PageResponse
     * This allows transforming entities to DTOs during page conversion
     */
    public <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        if (page == null) {
            return PageResponse.<R>builder()
                    .content(List.of())
                    .currentPage(0)
                    .totalPages(0)
                    .totalElements(0)
                    .pageSize(0)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        List<R> mappedContent = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return PageResponse.<R>builder()
                .content(mappedContent)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
