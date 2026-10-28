package com.company.roombooking.room.mapper;

import com.company.roombooking.common.dto.LocalizedText;
import com.company.roombooking.room.dto.EquipmentDetailResponse;
import com.company.roombooking.room.dto.EquipmentResponse;
import com.company.roombooking.room.entity.Equipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {

    @Mapping(target = "name", expression = "java(toLocalizedName(equipment))")
    EquipmentResponse toResponse(Equipment equipment);

    List<EquipmentResponse> toResponseList(List<Equipment> equipments);

    @Mapping(target = "name", expression = "java(toLocalizedName(equipment))")
    EquipmentDetailResponse toDetailResponse(Equipment equipment);

    default LocalizedText toLocalizedName(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return LocalizedText.of(equipment.getNameVi(), equipment.getNameEn());
    }
}
