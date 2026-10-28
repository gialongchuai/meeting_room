package com.company.roombooking.room.mapper;

import com.company.roombooking.common.dto.LocalizedText;
import com.company.roombooking.room.dto.RoomDetailResponse;
import com.company.roombooking.room.dto.RoomResponse;
import com.company.roombooking.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EquipmentMapper.class})
public interface RoomMapper {

    @Mapping(target = "name", expression = "java(toLocalizedName(room))")
    @Mapping(target = "description", expression = "java(toLocalizedDescription(room))")
    RoomResponse toResponse(Room room);

    List<RoomResponse> toResponseList(List<Room> rooms);

    @Mapping(target = "name", expression = "java(toLocalizedName(room))")
    @Mapping(target = "description", expression = "java(toLocalizedDescription(room))")
    RoomDetailResponse toDetailResponse(Room room);

    default LocalizedText toLocalizedName(Room room) {
        if (room == null) {
            return null;
        }
        return LocalizedText.of(room.getNameVi(), room.getNameEn());
    }

    default LocalizedText toLocalizedDescription(Room room) {
        if (room == null || (room.getDescriptionVi() == null && room.getDescriptionEn() == null)) {
            return null;
        }
        return LocalizedText.of(room.getDescriptionVi(), room.getDescriptionEn());
    }
}
