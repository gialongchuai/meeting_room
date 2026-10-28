package com.company.roombooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "booking")
public class BookingProperties {

    private int minDurationMinutes = 15;
    private int maxDurationMinutes = 240;
    private int timeSlotStepMinutes = 15;
    private int maxLeadDays = 60;
    private String workingHoursStart = "08:00";
    private String workingHoursEnd = "18:00";
    private int quotaPerUser = 20;

    public LocalTime getParsedWorkingHoursStart() {
        return LocalTime.parse(workingHoursStart);
    }

    public LocalTime getParsedWorkingHoursEnd() {
        return LocalTime.parse(workingHoursEnd);
    }
}
