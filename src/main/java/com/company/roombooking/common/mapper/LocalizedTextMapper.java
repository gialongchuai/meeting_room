package com.company.roombooking.common.mapper;

import com.company.roombooking.common.dto.LocalizedText;
import org.springframework.stereotype.Component;

@Component
public class LocalizedTextMapper {

    public LocalizedText toLocalizedText(String vi, String en) {
        if (vi == null && en == null) {
            return null;
        }
        return LocalizedText.of(vi, en);
    }
}
