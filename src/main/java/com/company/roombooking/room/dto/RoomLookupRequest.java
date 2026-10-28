package com.company.roombooking.room.dto;

import com.company.roombooking.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tìm kiếm và phân trang danh sách phòng")
public class RoomLookupRequest {

    @Schema(description = "Từ khóa tìm kiếm theo mã hoặc tên phòng", example = "Hà Nội")
    private String keyword;

    @Min(value = 1, message = "Sức chứa tối thiểu phải >= 1")
    @Schema(description = "Sức chứa tối thiểu cần tìm", example = "8")
    private Integer minCapacity;

    @Schema(description = "Danh sách ID tiện ích yêu cầu phòng phải có ĐỦ TẤT CẢ", example = "[1, 2]")
    private List<Long> equipmentIds;

    @Schema(description = "Lọc theo trạng thái phòng (ACTIVE, MAINTENANCE, INACTIVE)", example = "ACTIVE")
    private RoomStatus status;

    @Min(value = 0, message = "Chỉ số trang phải >= 0")
    @Builder.Default
    @Schema(description = "Chỉ số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang phải >= 1")
    @Max(value = 100, message = "Kích thước trang tối đa là 100")
    @Builder.Default
    @Schema(description = "Số bản ghi trên mỗi trang (tối đa 100)", example = "10")
    private int size = 10;

    @Builder.Default
    @Schema(description = "Trường sắp xếp (roomId, code, nameVi, nameEn, capacity, floor, status, createdAt)", example = "code")
    private String sortBy = "code";

    @Builder.Default
    @Schema(description = "Hướng sắp xếp (ASC hoặc DESC)", example = "ASC")
    private String sortDir = "ASC";
}
