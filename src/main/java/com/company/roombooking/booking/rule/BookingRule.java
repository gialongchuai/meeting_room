package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;

public interface BookingRule {

    void validate(BookingContext context) throws BusinessException;
}
