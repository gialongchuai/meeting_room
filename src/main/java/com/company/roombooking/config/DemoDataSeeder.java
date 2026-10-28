package com.company.roombooking.config;

import com.company.roombooking.room.entity.Equipment;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.room.entity.RoomStatus;
import com.company.roombooking.room.repository.EquipmentRepository;
import com.company.roombooking.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedDemoData || roomRepository.count() > 0) {
            return;
        }

        log.info("Bắt đầu khởi tạo dữ liệu 10 phòng họp mẫu...");

        Map<String, Equipment> eqMap = new HashMap<>();
        equipmentRepository.findAll().forEach(eq -> eqMap.put(eq.getCode(), eq));

        List<Room> demoRooms = List.of(
                createRoom("HN-1F-01", "Phòng Focus 1", "Focus Room 1",
                        "Phòng làm việc nhóm nhỏ hoặc phỏng vấn", "Small team or interview room",
                        4, "1F", getEquipments(eqMap, "TV_SCREEN", "WHITEBOARD")),

                createRoom("HN-1F-02", "Phòng Focus 2", "Focus Room 2",
                        "Phòng họp nhóm nhanh", "Quick sync room",
                        6, "1F", getEquipments(eqMap, "TV_SCREEN", "WHITEBOARD", "SPEAKERPHONE")),

                createRoom("HN-2F-01", "Phòng Sài Gòn", "Saigon Room",
                        "Phòng họp nhóm dự án tiêu chuẩn", "Standard project team meeting room",
                        8, "2F", getEquipments(eqMap, "PROJECTOR", "WHITEBOARD")),

                createRoom("HN-2F-02", "Phòng Đà Nẵng", "Danang Room",
                        "Phòng họp trực tuyến với chi nhánh", "Branch video conference room",
                        10, "2F", getEquipments(eqMap, "VIDEO_CONF", "WHITEBOARD", "TV_SCREEN")),

                createRoom("HN-2F-03", "Phòng Cần Thơ", "Cantho Room",
                        "Phòng thảo luận chuyên đề", "Topic discussion room",
                        12, "2F", getEquipments(eqMap, "VIDEO_CONF", "SPEAKERPHONE", "TV_SCREEN")),

                createRoom("HN-3F-01", "Phòng Hà Nội", "Hanoi Room",
                        "Phòng họp ban giám đốc và đối tác", "Board meeting and partner room",
                        16, "3F", getEquipments(eqMap, "VIDEO_CONF", "PROJECTOR", "SPEAKERPHONE", "WHITEBOARD")),

                createRoom("HN-3F-02", "Phòng Chiến Lược", "Strategy Room",
                        "Phòng họp lập kế hoạch và chiến lược", "Strategic planning room",
                        18, "3F", getEquipments(eqMap, "PROJECTOR", "WHITEBOARD", "SPEAKERPHONE")),

                createRoom("HN-4F-01", "Phòng Hội Nghị", "Executive Conference Room",
                        "Phòng hội nghị đa năng cao cấp", "Executive multipurpose conference room",
                        20, "4F", getEquipments(eqMap, "PROJECTOR", "VIDEO_CONF", "WHITEBOARD", "TV_SCREEN", "SPEAKERPHONE")),

                createRoom("HN-4F-02", "Phòng Đào Tạo A", "Training Hall A",
                        "Phòng đào tạo nội bộ và workshop", "Internal training and workshop room",
                        25, "4F", getEquipments(eqMap, "PROJECTOR", "SPEAKERPHONE", "WHITEBOARD")),

                createRoom("HN-4F-03", "Hội Trường Lớn", "Grand Town Hall",
                        "Hội trường lớn cho toàn thể nhân viên", "Large town hall for all hands meetings",
                        30, "4F", getEquipments(eqMap, "PROJECTOR", "VIDEO_CONF", "WHITEBOARD", "TV_SCREEN", "SPEAKERPHONE"))
        );

        roomRepository.saveAll(demoRooms);
        log.info("Khởi tạo thành công 10 phòng họp mẫu.");
    }

    private Room createRoom(String code, String nameVi, String nameEn,
                            String descVi, String descEn,
                            int capacity, String floor, Set<Equipment> equipments) {
        return Room.builder()
                .code(code)
                .nameVi(nameVi)
                .nameEn(nameEn)
                .descriptionVi(descVi)
                .descriptionEn(descEn)
                .capacity(capacity)
                .floor(floor)
                .status(RoomStatus.ACTIVE)
                .equipments(equipments)
                .build();
    }

    private Set<Equipment> getEquipments(Map<String, Equipment> map, String... codes) {
        Set<Equipment> set = new HashSet<>();
        for (String code : codes) {
            Equipment eq = map.get(code);
            if (eq != null) {
                set.add(eq);
            }
        }
        return set;
    }
}
