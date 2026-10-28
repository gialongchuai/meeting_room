package com.company.roombooking.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Văn bản đa ngôn ngữ")
public class LocalizedText {

    @Schema(description = "Tiếng Việt", example = "Phòng họp Hà Nội")
    private String vi;

    @Schema(description = "Tiếng Anh", example = "Hanoi Meeting Room")
    private String en;

    public static LocalizedText of(String vi, String en) {
        return new LocalizedText(vi, en);
    }
}
