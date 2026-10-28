package com.company.roombooking.room.repository;

import com.company.roombooking.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Optional<Room> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
