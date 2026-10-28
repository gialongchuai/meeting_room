package com.company.roombooking.room.repository;

import com.company.roombooking.room.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Equipment> findByEquipmentIdIn(Collection<Long> equipmentIds);
}
