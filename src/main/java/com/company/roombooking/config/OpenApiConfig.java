package com.company.roombooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Meeting Room Booking Service API")
                        .version("1.0.0")
                        .description("""
                                ## Hệ thống Quản lý và Đặt phòng họp nội bộ
                                
                                ### Các quy ước quan trọng:
                                - **Xác thực:** Sử dụng Bearer JWT Token. Ngoại trừ `/auth/login` và `/auth/refresh`, tất cả các API yêu cầu header `Authorization: Bearer <accessToken>`.
                                - **Thời gian:** Mọi trường thời gian (`startTime`, `endTime`, `createdAt`, ...) đều sử dụng kiểu số nguyên `Long` đại diện cho Epoch Milliseconds (UTC).
                                - **Múi giờ làm việc:** Mặc định `Asia/Ho_Chi_Minh` (08:00 - 18:00, Thứ 2 - Thứ 6).
                                - **Đa ngôn ngữ:** Nội dung danh mục hệ thống trả về object `{ "vi": "...", "en": "..." }`. Lỗi được bản địa hóa qua header `Accept-Language` (`vi` hoặc `en`).
                                - **Phân trang:** Mọi danh sách trả về phân trang chuẩn (`page`, `size`, `totalElements`, `totalPages`), ngoại trừ lịch calendar (giới hạn tối đa 31 ngày).
                                - **Chống trùng lịch:** Cam kết zero-double-booking ở cả cấp độ phòng và cấp độ người dùng thông qua PostgreSQL Exclusion Constraints.
                                """)
                        .contact(new Contact().name("Đội ngũ Kỹ thuật").email("tech@company.com"))
                        .license(new License().name("Nội bộ doanh nghiệp")))
                .servers(List.of(
                        new Server().url("/").description("Môi trường hiện tại")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập Access Token JWT theo định dạng: Bearer <token>")));
    }
}
