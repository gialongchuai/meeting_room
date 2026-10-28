package com.company.roombooking.room.specification;

import com.company.roombooking.room.dto.RoomLookupRequest;
import com.company.roombooking.room.entity.Equipment;
import com.company.roombooking.room.entity.Room;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> filter(RoomLookupRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getKeyword())) {
                String kw = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate codePred = cb.like(cb.lower(root.get("code")), kw);
                Predicate nameViPred = cb.like(cb.lower(root.get("nameVi")), kw);
                Predicate nameEnPred = cb.like(cb.lower(root.get("nameEn")), kw);
                predicates.add(cb.or(codePred, nameViPred, nameEnPred));
            }

            if (request.getMinCapacity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), request.getMinCapacity()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getEquipmentIds() != null && !request.getEquipmentIds().isEmpty()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Room> subRoot = subquery.correlate(root);
                Join<Room, Equipment> eqJoin = subRoot.join("equipments");
                subquery.select(cb.countDistinct(eqJoin.get("equipmentId")))
                        .where(eqJoin.get("equipmentId").in(request.getEquipmentIds()));
                predicates.add(cb.equal(subquery, (long) request.getEquipmentIds().size()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
