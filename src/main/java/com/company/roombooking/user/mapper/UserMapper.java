package com.company.roombooking.user.mapper;

import com.company.roombooking.auth.dto.UserSummaryResponse;
import com.company.roombooking.user.dto.UserDetailResponse;
import com.company.roombooking.user.dto.UserResponse;
import com.company.roombooking.user.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    UserDetailResponse toDetailResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);
}
