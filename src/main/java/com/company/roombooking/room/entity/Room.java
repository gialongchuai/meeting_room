package com.company.roombooking.room.entity;

import com.company.roombooking.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room")
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "name_vi", nullable = false, length = 100)
    private String nameVi;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "description_vi", length = 500)
    private String descriptionVi;

    @Column(name = "description_en", length = 500)
    private String descriptionEn;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "floor", length = 10)
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private RoomStatus status;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_equipment",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> equipments = new HashSet<>();
}
