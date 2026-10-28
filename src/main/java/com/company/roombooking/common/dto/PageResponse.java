package com.company.roombooking.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Đối tượng phân trang chuẩn")
public class PageResponse<T> {

    @Schema(description = "Danh sách phần tử của trang hiện tại")
    private List<T> content;

    @Schema(description = "Chỉ số trang hiện tại (bắt đầu từ 0)", example = "0")
    private int page;

    @Schema(description = "Số phần tử trên một trang", example = "10")
    private int size;

    @Schema(description = "Tổng số phần tử", example = "42")
    private long totalElements;

    @Schema(description = "Tổng số trang", example = "5")
    private int totalPages;

    public static <T> PageResponse<T> of(Page<T> springPage) {
        return PageResponse.<T>builder()
                .content(springPage.getContent())
                .page(springPage.getNumber())
                .size(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .build();
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
